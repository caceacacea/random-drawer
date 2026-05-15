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
    val lastResult: DrawResult = DrawResult(0L, emptyList(), 0L),
    val themeMode: ThemeMode = ThemeMode.AMOLED,
    val drawerOpen: Boolean = false,
    val pendingPickedFile: PendingPickedFile? = null
) {
    val savedItemCount: Int = items.size
    val cachedFileCount: Int = items.count { it.hasCache }
    val cappedDrawCount: Int = drawCount.coerceAtMost(items.size).coerceAtLeast(1)

    fun draw(useCase: RandomDrawUseCase): RandomDrawerUiState {
        if (items.isEmpty()) {
            return copy(lastResult = DrawResult(selectedSpaceId ?: 0L, emptyList(), System.currentTimeMillis()))
        }

        return copy(lastResult = useCase.draw(items, drawMode, cappedDrawCount))
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
