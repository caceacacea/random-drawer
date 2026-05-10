package com.example.randomdrawer.domain

import kotlin.random.Random

class RandomDrawUseCase(
    private val random: Random = Random.Default
) {
    fun draw(
        items: List<DrawerItem>,
        mode: DrawMode,
        requestedCount: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): DrawResult {
        if (items.isEmpty()) {
            return DrawResult(spaceId = 0L, items = emptyList(), createdAtMillis = nowMillis)
        }

        val count = when (mode) {
            DrawMode.SINGLE -> 1
            DrawMode.MULTIPLE -> requestedCount.coerceAtLeast(1).coerceAtMost(items.size)
        }

        return DrawResult(
            spaceId = items.first().spaceId,
            items = items.shuffled(random).take(count),
            createdAtMillis = nowMillis
        )
    }
}
