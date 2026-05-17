package com.example.randomdrawer.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.example.randomdrawer.domain.DrawSpace
import com.example.randomdrawer.ui.theme.RandomDrawerTheme
import org.junit.Rule
import org.junit.Test

class RandomDrawerComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mainScreenShowsCoreActions() {
        composeRule.setContent {
            RandomDrawerTheme {
                RandomDrawerApp(
                    state = RandomDrawerUiState(),
                    onToggleDrawer = {},
                    onNewSpace = {},
                    onSelectSpace = {},
                    onRenameSpace = { _, _ -> },
                    onDeleteSpace = {},
                    onToggleTheme = {},
                    onDeleteAllCache = {},
                    onSetDrawMode = {},
                    onSetDrawCount = {},
                    onSetSingleRepeatLimit = {},
                    onSetMultiRepeatLimit = {},
                    onSetAnimationsEnabled = {},
                    onSetAnimationDelayMillis = {},
                    onDraw = {},
                    onDismissDrawPopup = {},
                    onToggleResultExpanded = {},
                    onAddText = { _ -> },
                    onAddFile = {},
                    onAddFileWithName = {},
                    onDeleteItem = {},
                    onConfirmFileWithName = { _, _ -> },
                    onCancelFileWithName = {},
                    onOpenFile = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithText("Random Drawer").assertIsDisplayed()
        composeRule.onNodeWithText("Draw Random").assertIsDisplayed()
        composeRule.onNodeWithText("Add Text").assertIsDisplayed()
        composeRule.onNodeWithText("Add File").assertIsDisplayed()
    }

    @Test
    fun drawerShowsAnimationSettings() {
        val spaces = (1L..12L).map {
            DrawSpace(
                id = it,
                title = "Space $it",
                createdAtMillis = it,
                updatedAtMillis = it,
                drawMode = com.example.randomdrawer.domain.DrawMode.SINGLE,
                drawCount = 1
            )
        }

        composeRule.setContent {
            RandomDrawerTheme {
                RandomDrawerApp(
                    state = RandomDrawerUiState(spaces = spaces, drawerOpen = true),
                    onToggleDrawer = {},
                    onNewSpace = {},
                    onSelectSpace = {},
                    onRenameSpace = { _, _ -> },
                    onDeleteSpace = {},
                    onToggleTheme = {},
                    onDeleteAllCache = {},
                    onSetDrawMode = {},
                    onSetDrawCount = {},
                    onSetSingleRepeatLimit = {},
                    onSetMultiRepeatLimit = {},
                    onSetAnimationsEnabled = {},
                    onSetAnimationDelayMillis = {},
                    onDraw = {},
                    onDismissDrawPopup = {},
                    onToggleResultExpanded = {},
                    onAddText = { _ -> },
                    onAddFile = {},
                    onAddFileWithName = {},
                    onDeleteItem = {},
                    onConfirmFileWithName = { _, _ -> },
                    onCancelFileWithName = {},
                    onOpenFile = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("drawerSettingsScroll").performScrollToNode(hasText("Animation delay"))
        composeRule.onNodeWithText("Animation delay").assertIsDisplayed()
    }
}
