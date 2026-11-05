package com.main.sugarbreak.data.repository

internal fun <T, K, V : Comparable<V>> mergeByNewest(
    local: List<T>,
    remote: List<T>,
    keyOf: (T) -> K,
    updatedAt: (T) -> V
): List<T> = (local + remote)
    .groupBy(keyOf)
    .values
    .map { matching -> matching.reduce { winner, candidate ->
        if (updatedAt(candidate) > updatedAt(winner)) candidate else winner
    } }

internal fun resolveOnboardingCompletion(
    localCompletion: Boolean?,
    remoteCompletion: Boolean?,
    hasRemoteChallenges: Boolean
): Boolean = when {
    localCompletion == true -> true
    remoteCompletion != null -> remoteCompletion
    hasRemoteChallenges -> true
    else -> localCompletion ?: false
}

internal fun resolveTimestampedValue(
    localValue: String?,
    localUpdatedAt: Long,
    remoteValue: String?,
    remoteUpdatedAt: Long
): Pair<String, Long>? = when {
    localValue == null && remoteValue == null -> null
    remoteValue == null -> localValue?.let { it to localUpdatedAt }
    localValue == null || remoteUpdatedAt > localUpdatedAt -> remoteValue to remoteUpdatedAt
    else -> localValue to localUpdatedAt
}

internal fun resolveChallengeBehavior(
    localValue: String?,
    localUpdatedAt: Long,
    remoteValue: String?,
    remoteUpdatedAt: Long
): Pair<String, Long>? = resolveTimestampedValue(localValue, localUpdatedAt, remoteValue, remoteUpdatedAt)

internal fun isCheckInSuppressedByPendingReset(
    challengeSyncId: String?,
    createdAt: java.time.LocalDateTime?,
    pendingResetCutoffs: Map<String, java.time.LocalDateTime>
): Boolean {
    val cutoff = challengeSyncId?.let(pendingResetCutoffs::get) ?: return false
    return createdAt == null || !createdAt.isAfter(cutoff)
}
