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
