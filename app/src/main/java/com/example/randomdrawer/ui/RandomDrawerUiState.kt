package com.example.randomdrawer.ui

import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawResult
import com.example.randomdrawer.domain.DrawSpace
import com.example.randomdrawer.domain.DrawerItem
import com.example.randomdrawer.domain.RandomDrawUseCase
import com.example.randomdrawer.domain.ThemeMode

data class RandomDrawerUiState(
    val spaces: List<DrawSpace> = emptyList(),
    val selectedSpaceId: Long? = null,
    val selectedSpaceTitle: String = "New draw",
    val items: List<DrawerItem> = emptyList(),
    val drawMode: DrawMode = DrawMode.SINGLE,
    val drawCount: Int = 2,
    val singleRepeatLimit: Int = 0,
    val multiRepeatLimit: Int = 1,
    val lastSingleItemId: Long? = null,
    val lastSingleStreakCount: Int = 0,
    val lastResult: DrawResult = DrawResult(0L, emptyList(), 0L),
    val isDrawing: Boolean = false,
    val drawPopupVisible: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.AMOLED,
    val drawerOpen: Boolean = false,
    val pendingPickedFile: PendingPickedFile? = null
) {
    val savedItemCount: Int = items.size
    val cachedFileCount: Int = items.count { it.hasCache }
    val cappedDrawCount: Int = when {
        drawMode != DrawMode.MULTIPLE -> 1
        items.isEmpty() -> 1
        multiRepeatLimit == 0 -> drawCount.coerceAtLeast(1)
        else -> drawCount.coerceAtLeast(1).coerceAtMost(items.size * multiRepeatLimit.coerceAtLeast(1))
    }

    fun draw(useCase: RandomDrawUseCase): RandomDrawerUiState {
        if (items.isEmpty()) {
            return copy(
                lastResult = DrawResult(selectedSpaceId ?: 0L, emptyList(), System.currentTimeMillis()),
                isDrawing = false,
                drawPopupVisible = true
            )
        }

        return copy(
            lastResult = useCase.draw(
                items = items,
                mode = drawMode,
                requestedCount = cappedDrawCount,
                singleRepeatLimit = singleRepeatLimit,
                lastSingleItemId = lastSingleItemId,
                lastSingleStreakCount = lastSingleStreakCount,
                multiRepeatLimit = multiRepeatLimit
            ),
            isDrawing = false,
            drawPopupVisible = true
        )
    }

    fun dismissDrawPopup(): RandomDrawerUiState {
        if (isDrawing) return this
        return copy(drawPopupVisible = false)
    }

    fun toggleResultExpanded(): RandomDrawerUiState {
        return copy(lastResult = lastResult.copy(expanded = !lastResult.expanded))
    }
}

data class PendingPickedFile(
    val uriString: String,
    val originalFileName: String,
    val mimeType: String?
)
