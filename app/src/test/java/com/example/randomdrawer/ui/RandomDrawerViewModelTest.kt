package com.example.randomdrawer.ui

import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawerItem
import com.example.randomdrawer.domain.ItemKind
import com.example.randomdrawer.domain.RandomDrawUseCase
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RandomDrawerViewModelTest {
    @Test
    fun multipleDrawStoresExpandedResultToggle() = runTest {
        val items = (1L..3L).map {
            DrawerItem(it, 1L, ItemKind.TEXT, "Item $it", null, null, null, it)
        }
        val state = RandomDrawerUiState(
            selectedSpaceId = 1L,
            items = items,
            drawMode = DrawMode.MULTIPLE,
            drawCount = 3
        )

        val updated = state.draw(RandomDrawUseCase(Random(1))).toggleResultExpanded()

        assertEquals(3, updated.lastResult.items.size)
        assertEquals(true, updated.lastResult.expanded)
    }

    @Test
    fun cappedDrawCountAllowsConfiguredMultiRepeats() = runTest {
        val items = (1L..2L).map {
            DrawerItem(it, 1L, ItemKind.TEXT, "Item $it", null, null, null, it)
        }

        val limited = RandomDrawerUiState(
            selectedSpaceId = 1L,
            items = items,
            drawMode = DrawMode.MULTIPLE,
            drawCount = 5,
            multiRepeatLimit = 2
        )
        val unlimited = limited.copy(multiRepeatLimit = 0)

        assertEquals(4, limited.cappedDrawCount)
        assertEquals(5, unlimited.cappedDrawCount)
    }
}
