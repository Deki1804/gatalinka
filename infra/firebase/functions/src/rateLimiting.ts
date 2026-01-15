import * as admin from "firebase-admin";

/**
 * Rate limiting configuration
 */
const RATE_LIMIT_CONFIG = {
  // Max calls per user per minute
  callsPerMinute: 10,
  // Max calls per user per day
  callsPerDay: 50,
  // Max calls per user per month
  callsPerMonth: 500,
};

const QUOTA_LIMIT_CONFIG = {
  // Max calls per day globally (billing protection)
  maxDailyGlobalCalls: 10000,
};

type LimitCheckResult = { allowed: boolean; reason?: string };

function pad2(n: number): string {
  return String(n).padStart(2, "0");
}

function utcBucketKeys(now: Date): { minuteKey: string; dayKey: string; monthKey: string } {
  const y = now.getUTCFullYear();
  const m = pad2(now.getUTCMonth() + 1);
  const d = pad2(now.getUTCDate());
  const hh = pad2(now.getUTCHours());
  const mm = pad2(now.getUTCMinutes());
  const dayKey = `${y}-${m}-${d}`;
  const monthKey = `${y}-${m}`;
  const minuteKey = `${dayKey}T${hh}:${mm}Z`;
  return { minuteKey, dayKey, monthKey };
}

/**
 * Check if user has exceeded rate limits
 * Returns { allowed: boolean, reason?: string }
 */
export async function checkRateLimit(userId: string): Promise<{ allowed: boolean; reason?: string }> {
  try {
    const db = admin.firestore();
    const now = new Date();
    const { minuteKey, dayKey, monthKey } = utcBucketKeys(now);

    const rootRef = db.collection("rate_limits").doc(userId);
    const minuteRef = rootRef.collection("minute").doc(minuteKey);
    const dayRef = rootRef.collection("day").doc(dayKey);
    const monthRef = rootRef.collection("month").doc(monthKey);

    const result: LimitCheckResult = await db.runTransaction(async (tx) => {
      const [minuteSnap, daySnap, monthSnap] = await Promise.all([
        tx.get(minuteRef),
        tx.get(dayRef),
        tx.get(monthRef),
      ]);

      const minuteCount = (minuteSnap.data()?.count as number | undefined) ?? 0;
      const dayCount = (daySnap.data()?.count as number | undefined) ?? 0;
      const monthCount = (monthSnap.data()?.count as number | undefined) ?? 0;

      if (minuteCount + 1 > RATE_LIMIT_CONFIG.callsPerMinute) {
        return {
          allowed: false,
          reason: `Prekoračen limit od ${RATE_LIMIT_CONFIG.callsPerMinute} poziva u minuti. Molimo pričekajte.`,
        };
      }
      if (dayCount + 1 > RATE_LIMIT_CONFIG.callsPerDay) {
        return {
          allowed: false,
          reason: `Prekoračen dnevni limit od ${RATE_LIMIT_CONFIG.callsPerDay} poziva. Pokušajte sutra.`,
        };
      }
      if (monthCount + 1 > RATE_LIMIT_CONFIG.callsPerMonth) {
        return {
          allowed: false,
          reason: `Prekoračen mjesečni limit od ${RATE_LIMIT_CONFIG.callsPerMonth} poziva.`,
        };
      }

      const serverNow = admin.firestore.FieldValue.serverTimestamp();
      tx.set(
        minuteRef,
        { count: minuteCount + 1, updatedAt: serverNow, createdAt: minuteSnap.exists ? minuteSnap.data()?.createdAt : serverNow },
        { merge: true }
      );
      tx.set(
        dayRef,
        { count: dayCount + 1, updatedAt: serverNow, createdAt: daySnap.exists ? daySnap.data()?.createdAt : serverNow },
        { merge: true }
      );
      tx.set(
        monthRef,
        { count: monthCount + 1, updatedAt: serverNow, createdAt: monthSnap.exists ? monthSnap.data()?.createdAt : serverNow },
        { merge: true }
      );

      return { allowed: true };
    });

    return result;
  } catch (error: any) {
    // Fail-closed: if rate limiting infra is unavailable, block the request.
    console.error("Rate limit check error");
    return {
      allowed: false,
      reason: "Trenutno nije moguće provjeriti limite. Molimo pokušajte kasnije.",
    };
  }
}

/**
 * Check billing/quota limits
 * Returns { allowed: boolean, reason?: string }
 */
export async function checkQuotaLimit(): Promise<{ allowed: boolean; reason?: string }> {
  try {
    const db = admin.firestore();
    const now = new Date();
    const { dayKey } = utcBucketKeys(now);

    const quotaRef = db.collection("quota_limits").doc(`daily-${dayKey}`);

    const result: LimitCheckResult = await db.runTransaction(async (tx) => {
      const snap = await tx.get(quotaRef);
      const currentCount = (snap.data()?.count as number | undefined) ?? 0;

      if (currentCount + 1 > QUOTA_LIMIT_CONFIG.maxDailyGlobalCalls) {
        return {
          allowed: false,
          reason: "Dnevni limit poziva je dostignut. Molimo pokušajte kasnije.",
        };
      }

      const serverNow = admin.firestore.FieldValue.serverTimestamp();
      tx.set(
        quotaRef,
        {
          count: currentCount + 1,
          day: dayKey,
          updatedAt: serverNow,
          createdAt: snap.exists ? snap.data()?.createdAt : serverNow,
        },
        { merge: true }
      );

      return { allowed: true };
    });

    return result;
  } catch (error: any) {
    // Fail-closed: if quota infra is unavailable, block the request.
    console.error("Quota limit check error");
    return {
      allowed: false,
      reason: "Trenutno nije moguće provjeriti limite. Molimo pokušajte kasnije.",
    };
  }
}

