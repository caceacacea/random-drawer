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
        val result = RandomDrawUseCase(Random(4)).draw(emptyList(), DrawMode.MULTIPLE, requestedCount = 3)

        assertTrue(result.items.isEmpty())
    }
}
