package com.example.randomdrawer.ui

import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawerItem
import com.example.randomdrawer.domain.DRAW_ANIMATION_DELAY_STEP_MILLIS
import com.example.randomdrawer.domain.DrawResult
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

    @Test
    fun drawCompletionClearsDrawingStateAndStoresResult() = runTest {
        val items = listOf(
            DrawerItem(1L, 1L, ItemKind.TEXT, "Item 1", null, null, null, 1L)
        )
        val drawing = RandomDrawerUiState(
            selectedSpaceId = 1L,
            items = items,
            isDrawing = true,
            drawPopupVisible = true
        )

        val completed = drawing.draw(RandomDrawUseCase(Random(2)))

        assertEquals(false, completed.isDrawing)
        assertEquals(true, completed.drawPopupVisible)
        assertEquals("Item 1", completed.lastResult.items.single().displayName)
        assertEquals(false, completed.dismissDrawPopup().drawPopupVisible)
    }

    @Test
    fun disabledAnimationStoresResultWithoutPopup() = runTest {
        val items = listOf(
            DrawerItem(1L, 1L, ItemKind.TEXT, "Item 1", null, null, null, 1L)
        )
        val state = RandomDrawerUiState(
            selectedSpaceId = 1L,
            items = items,
            animationsEnabled = false
        )

        val completed = state.draw(RandomDrawUseCase(Random(2)))

        assertEquals(false, completed.isDrawing)
        assertEquals(false, completed.drawPopupVisible)
        assertEquals("Item 1", completed.lastResult.items.single().displayName)
    }

    @Test
    fun animationDelayUsesQuarterSecondSteps() {
        assertEquals(250L, DRAW_ANIMATION_DELAY_STEP_MILLIS)
        assertEquals("1.25s", RandomDrawerUiState(animationDelayMillis = 1250L).animationDelayLabel)
    }

    @Test
    fun resultRevealCopyLabelsSingleAndMultipleResults() {
        val items = (1L..2L).map {
            DrawerItem(it, 1L, ItemKind.TEXT, "Item $it", null, null, null, it)
        }

        assertEquals("Selected", resultRevealHeader(DrawResult(1L, listOf(items.first()), 10L)))
        assertEquals("2 results selected", resultRevealHeader(DrawResult(1L, items, 10L)))
        assertEquals(1200L, RESULT_RING_PULSE_PERIOD_MILLIS)
    }
}
