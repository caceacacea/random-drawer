package com.example.randomdrawer.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomDrawUseCaseTest {
    private val items = (1L..5L).map { id ->
        DrawerItem(
            id = id,
            spaceId = 10L,
            kind = ItemKind.TEXT,
            displayName = "Item $id",
            originalFileName = null,
            mimeType = null,
            cachedFilePath = null,
            createdAtMillis = id
        )
    }

    @Test
    fun singleDrawReturnsOneItemFromInput() {
        val result = RandomDrawUseCase(Random(1)).draw(items, DrawMode.SINGLE, requestedCount = 1)

        assertEquals(1, result.items.size)
        assertTrue(result.items.first() in items)
    }

    @Test
    fun multipleDrawReturnsUniqueItemsWithoutRepeats() {
        val result = RandomDrawUseCase(Random(2)).draw(items, DrawMode.MULTIPLE, requestedCount = 3)

        assertEquals(3, result.items.size)
        assertEquals(result.items.size, result.items.map { it.id }.toSet().size)
    }

    @Test
    fun multipleDrawCapsCountAtAvailableItems() {
        val result = RandomDrawUseCase(Random(3)).draw(items.take(2), DrawMode.MULTIPLE, requestedCount = 99)

        assertEquals(2, result.items.size)
        assertEquals(2, result.items.map { it.id }.toSet().size)
    }

    @Test
    fun emptyInputReturnsEmptyResult() {
        val result = RandomDrawUseCase(Random(4)).draw(
            emptyList(),
            DrawMode.MULTIPLE,
            requestedCount = 3,
            nowMillis = 42L
        )

        assertEquals(0L, result.spaceId)
        assertEquals(42L, result.createdAtMillis)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun multipleDrawWithZeroCountReturnsOneItemWhenInputExists() {
        val result = RandomDrawUseCase(Random(5)).draw(items, DrawMode.MULTIPLE, requestedCount = 0)

        assertEquals(1, result.items.size)
        assertTrue(result.items.first() in items)
    }

    @Test
    fun singleDrawExcludesStreakItemAfterRepeatLimitWhenAlternativeExists() {
        val result = RandomDrawUseCase(Random(6)).draw(
            items.take(2),
            DrawMode.SINGLE,
            requestedCount = 1,
            singleRepeatLimit = 2,
            lastSingleItemId = 1L,
            lastSingleStreakCount = 2
        )

        assertEquals(2L, result.items.single().id)
    }

    @Test
    fun singleDrawStillReturnsOnlyItemAfterRepeatLimit() {
        val result = RandomDrawUseCase(Random(7)).draw(
            items.take(1),
            DrawMode.SINGLE,
            requestedCount = 1,
            singleRepeatLimit = 2,
            lastSingleItemId = 1L,
            lastSingleStreakCount = 2
        )

        assertEquals(1L, result.items.single().id)
    }

    @Test
    fun multipleDrawAllowsRepeatsUpToConfiguredLimit() {
        val result = RandomDrawUseCase(Random(8)).draw(
            items.take(2),
            DrawMode.MULTIPLE,
            requestedCount = 5,
            multiRepeatLimit = 2
        )

        assertEquals(4, result.items.size)
        assertTrue(result.items.groupingBy { it.id }.eachCount().values.all { it <= 2 })
    }

    @Test
    fun multipleDrawWithZeroRepeatLimitUsesRequestedCount() {
        val result = RandomDrawUseCase(Random(9)).draw(
            items.take(1),
            DrawMode.MULTIPLE,
            requestedCount = 5,
            multiRepeatLimit = 0
        )

        assertEquals(5, result.items.size)
        assertEquals(listOf(1L, 1L, 1L, 1L, 1L), result.items.map { it.id })
    }
}
