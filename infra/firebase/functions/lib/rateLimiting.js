"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.checkRateLimit = checkRateLimit;
exports.checkQuotaLimit = checkQuotaLimit;
const admin = __importStar(require("firebase-admin"));
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
function pad2(n) {
    return String(n).padStart(2, "0");
}
function utcBucketKeys(now) {
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
async function checkRateLimit(userId) {
    try {
        const db = admin.firestore();
        const now = new Date();
        const { minuteKey, dayKey, monthKey } = utcBucketKeys(now);
        const rootRef = db.collection("rate_limits").doc(userId);
        const minuteRef = rootRef.collection("minute").doc(minuteKey);
        const dayRef = rootRef.collection("day").doc(dayKey);
        const monthRef = rootRef.collection("month").doc(monthKey);
        const result = await db.runTransaction(async (tx) => {
            const [minuteSnap, daySnap, monthSnap] = await Promise.all([
                tx.get(minuteRef),
                tx.get(dayRef),
                tx.get(monthRef),
            ]);
            const minuteCount = minuteSnap.data()?.count ?? 0;
            const dayCount = daySnap.data()?.count ?? 0;
            const monthCount = monthSnap.data()?.count ?? 0;
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
            tx.set(minuteRef, { count: minuteCount + 1, updatedAt: serverNow, createdAt: minuteSnap.exists ? minuteSnap.data()?.createdAt : serverNow }, { merge: true });
            tx.set(dayRef, { count: dayCount + 1, updatedAt: serverNow, createdAt: daySnap.exists ? daySnap.data()?.createdAt : serverNow }, { merge: true });
            tx.set(monthRef, { count: monthCount + 1, updatedAt: serverNow, createdAt: monthSnap.exists ? monthSnap.data()?.createdAt : serverNow }, { merge: true });
            return { allowed: true };
        });
        return result;
    }
    catch (error) {
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
async function checkQuotaLimit() {
    try {
        const db = admin.firestore();
        const now = new Date();
        const { dayKey } = utcBucketKeys(now);
        const quotaRef = db.collection("quota_limits").doc(`daily-${dayKey}`);
        const result = await db.runTransaction(async (tx) => {
            const snap = await tx.get(quotaRef);
            const currentCount = snap.data()?.count ?? 0;
            if (currentCount + 1 > QUOTA_LIMIT_CONFIG.maxDailyGlobalCalls) {
                return {
                    allowed: false,
                    reason: "Dnevni limit poziva je dostignut. Molimo pokušajte kasnije.",
                };
            }
            const serverNow = admin.firestore.FieldValue.serverTimestamp();
            tx.set(quotaRef, {
                count: currentCount + 1,
                day: dayKey,
                updatedAt: serverNow,
                createdAt: snap.exists ? snap.data()?.createdAt : serverNow,
            }, { merge: true });
            return { allowed: true };
        });
        return result;
    }
    catch (error) {
        // Fail-closed: if quota infra is unavailable, block the request.
        console.error("Quota limit check error");
        return {
            allowed: false,
            reason: "Trenutno nije moguće provjeriti limite. Molimo pokušajte kasnije.",
        };
    }
}
//# sourceMappingURL=rateLimiting.js.map