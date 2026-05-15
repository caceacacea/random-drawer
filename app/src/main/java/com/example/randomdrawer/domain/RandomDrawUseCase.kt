package com.example.randomdrawer.domain

import kotlin.random.Random

class RandomDrawUseCase(
    private val random: Random = Random.Default
) {
    fun draw(
        items: List<DrawerItem>,
        mode: DrawMode,
        requestedCount: Int,
        singleRepeatLimit: Int = 0,
        lastSingleItemId: Long? = null,
        lastSingleStreakCount: Int = 0,
        multiRepeatLimit: Int = 1,
        nowMillis: Long = System.currentTimeMillis()
    ): DrawResult {
        if (items.isEmpty()) {
            return DrawResult(spaceId = 0L, items = emptyList(), createdAtMillis = nowMillis)
        }

        val resultItems = when (mode) {
            DrawMode.SINGLE -> listOf(drawSingle(items, singleRepeatLimit, lastSingleItemId, lastSingleStreakCount))
            DrawMode.MULTIPLE -> drawMultiple(items, requestedCount, multiRepeatLimit)
        }

        return DrawResult(
            spaceId = items.first().spaceId,
            items = resultItems,
            createdAtMillis = nowMillis
        )
    }

    private fun drawSingle(
        items: List<DrawerItem>,
        repeatLimit: Int,
        lastSingleItemId: Long?,
        lastSingleStreakCount: Int
    ): DrawerItem {
        val eligibleItems = if (
            repeatLimit > 0 &&
            lastSingleItemId != null &&
            lastSingleStreakCount >= repeatLimit &&
            items.size > 1
        ) {
            items.filterNot { it.id == lastSingleItemId }
        } else {
            items
        }

        return eligibleItems.shuffled(random).first()
    }

    private fun drawMultiple(
        items: List<DrawerItem>,
        requestedCount: Int,
        repeatLimit: Int
    ): List<DrawerItem> {
        val count = requestedCount.coerceAtLeast(1)
        if (repeatLimit == 0) {
            return List(count) { items.random(random) }
        }

        val repeatedPool = items.flatMap { item ->
            List(repeatLimit.coerceAtLeast(1)) { item }
        }
        return repeatedPool.shuffled(random).take(count.coerceAtMost(repeatedPool.size))
    }
}
