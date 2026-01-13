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

/**
 * Check if user has exceeded rate limits
 * Returns { allowed: boolean, reason?: string }
 */
export async function checkRateLimit(userId: string): Promise<{ allowed: boolean; reason?: string }> {
  const db = admin.firestore();
  const now = Date.now();
  const oneMinuteAgo = now - 60 * 1000;
  const oneDayAgo = now - 24 * 60 * 60 * 1000;
  const oneMonthAgo = now - 30 * 24 * 60 * 60 * 1000;

  try {
    // Use Promise.all for parallel queries (better performance)
    const [recentCalls, dailyCalls, monthlyCalls] = await Promise.all([
      db
        .collection("readings")
        .where("userId", "==", userId)
        .where("timestamp", ">", admin.firestore.Timestamp.fromMillis(oneMinuteAgo))
        .count()
        .get(),
      db
        .collection("readings")
        .where("userId", "==", userId)
        .where("timestamp", ">", admin.firestore.Timestamp.fromMillis(oneDayAgo))
        .count()
        .get(),
      db
        .collection("readings")
        .where("userId", "==", userId)
        .where("timestamp", ">", admin.firestore.Timestamp.fromMillis(oneMonthAgo))
        .count()
        .get(),
    ]);

    // Check per-minute limit
    if (recentCalls.data().count >= RATE_LIMIT_CONFIG.callsPerMinute) {
      return {
        allowed: false,
        reason: `Prekoračen limit od ${RATE_LIMIT_CONFIG.callsPerMinute} poziva u minuti. Molimo pričekajte.`,
      };
    }

    // Check per-day limit
    if (dailyCalls.data().count >= RATE_LIMIT_CONFIG.callsPerDay) {
      return {
        allowed: false,
        reason: `Prekoračen dnevni limit od ${RATE_LIMIT_CONFIG.callsPerDay} poziva. Pokušajte sutra.`,
      };
    }

    // Check per-month limit
    if (monthlyCalls.data().count >= RATE_LIMIT_CONFIG.callsPerMonth) {
      return {
        allowed: false,
        reason: `Prekoračen mjesečni limit od ${RATE_LIMIT_CONFIG.callsPerMonth} poziva.`,
      };
    }

    return { allowed: true };
  } catch (error: any) {
    console.error("Rate limit check error:", error);
    // On error, allow the request (fail open) but log the error
    // In production, you might want to fail closed for security
    return { allowed: true };
  }
}

/**
 * Check billing/quota limits
 * Returns { allowed: boolean, reason?: string }
 */
export async function checkQuotaLimit(): Promise<{ allowed: boolean; reason?: string }> {
  const db = admin.firestore();
  const now = Date.now();
  const oneDayAgo = now - 24 * 60 * 60 * 1000;

  try {
    // Check total daily calls across all users (billing protection)
    const dailyTotalCalls = await db
      .collection("readings")
      .where("timestamp", ">", admin.firestore.Timestamp.fromMillis(oneDayAgo))
      .count()
      .get();

    // Max 10,000 calls per day globally (adjust based on your billing plan)
    const MAX_DAILY_GLOBAL_CALLS = 10000;

    if (dailyTotalCalls.data().count >= MAX_DAILY_GLOBAL_CALLS) {
      return {
        allowed: false,
        reason: "Dnevni limit poziva je dostignut. Molimo pokušajte kasnije.",
      };
    }

    return { allowed: true };
  } catch (error: any) {
    console.error("Quota limit check error:", error);
    // Fail open on error
    return { allowed: true };
  }
}

