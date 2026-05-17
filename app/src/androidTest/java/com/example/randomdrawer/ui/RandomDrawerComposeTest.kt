package com.example.randomdrawer.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
}
