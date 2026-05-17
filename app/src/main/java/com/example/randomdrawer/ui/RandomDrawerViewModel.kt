package com.example.randomdrawer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.randomdrawer.data.RandomDrawerRepository
import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.RandomDrawUseCase
import com.example.randomdrawer.domain.ThemeMode
import java.io.InputStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RandomDrawerViewModel(
    private val repository: RandomDrawerRepository,
    private val randomDrawUseCase: RandomDrawUseCase = RandomDrawUseCase(),
    private val drawAnimationMillis: Long = 950L
) : ViewModel() {
    private val mutableState = MutableStateFlow(RandomDrawerUiState())
    val state: StateFlow<RandomDrawerUiState> = mutableState.asStateFlow()
    private var itemCollectionJob: Job? = null
    private var lastResultCollectionJob: Job? = null

    fun initialize() {
        viewModelScope.launch {
            val initialSpaceId = repository.ensureInitialSpace()
            selectSpace(initialSpaceId)
        }
        viewModelScope.launch {
            repository.observeSpaces().collect { spaces ->
                val selectedSpace = spaces.firstOrNull { it.id == mutableState.value.selectedSpaceId }
                    ?: spaces.firstOrNull()
                mutableState.value = mutableState.value.copy(
                    spaces = spaces,
                    selectedSpaceId = selectedSpace?.id,
                    selectedSpaceTitle = selectedSpace?.title ?: "New draw",
                    drawMode = selectedSpace?.drawMode ?: DrawMode.SINGLE,
                    drawCount = selectedSpace?.drawCount ?: 2,
                    singleRepeatLimit = selectedSpace?.singleRepeatLimit ?: 0,
                    multiRepeatLimit = selectedSpace?.multiRepeatLimit ?: 1,
                    lastSingleItemId = selectedSpace?.lastSingleItemId,
                    lastSingleStreakCount = selectedSpace?.lastSingleStreakCount ?: 0
                )
            }
        }
        viewModelScope.launch {
            repository.observeTheme().collect { theme ->
                mutableState.value = mutableState.value.copy(themeMode = theme)
            }
        }
    }

    fun selectSpace(spaceId: Long) {
        val selectedSpace = mutableState.value.spaces.firstOrNull { it.id == spaceId }
        mutableState.value = mutableState.value.copy(
            selectedSpaceId = spaceId,
            selectedSpaceTitle = selectedSpace?.title ?: mutableState.value.selectedSpaceTitle,
            drawMode = selectedSpace?.drawMode ?: mutableState.value.drawMode,
            drawCount = selectedSpace?.drawCount ?: mutableState.value.drawCount,
            singleRepeatLimit = selectedSpace?.singleRepeatLimit ?: mutableState.value.singleRepeatLimit,
            multiRepeatLimit = selectedSpace?.multiRepeatLimit ?: mutableState.value.multiRepeatLimit,
            lastSingleItemId = selectedSpace?.lastSingleItemId,
            lastSingleStreakCount = selectedSpace?.lastSingleStreakCount ?: 0,
            drawerOpen = false
        )
        itemCollectionJob?.cancel()
        itemCollectionJob = viewModelScope.launch {
            repository.observeItems(spaceId).collect { items ->
                mutableState.value = mutableState.value.copy(items = items)
            }
        }
        lastResultCollectionJob?.cancel()
        lastResultCollectionJob = viewModelScope.launch {
            repository.observeLastResult(spaceId).collect { result ->
                mutableState.value = mutableState.value.copy(lastResult = result)
            }
        }
    }

    fun createNewSpace() {
        viewModelScope.launch {
            val spaceId = repository.createSpace()
            selectSpace(spaceId)
        }
    }

    fun renameSpace(spaceId: Long, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            repository.renameSpace(spaceId, trimmed)
        }
    }

    fun deleteSpace(spaceId: Long) {
        viewModelScope.launch {
            repository.deleteSpace(spaceId)
            if (mutableState.value.selectedSpaceId == spaceId) {
                val fallbackId = repository.getSpaces().firstOrNull()?.id ?: repository.createSpace()
                selectSpace(fallbackId)
            }
        }
    }

    fun addText(text: String) {
        val spaceId = mutableState.value.selectedSpaceId ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            repository.addTextItem(spaceId, trimmed)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    fun deleteAllCache() {
        viewModelScope.launch {
            repository.deleteAllCache()
        }
    }

    fun addCachedFile(
        spaceId: Long,
        displayName: String,
        originalFileName: String,
        mimeType: String?,
        input: InputStream
    ) {
        viewModelScope.launch {
            repository.addCachedFileItem(spaceId, displayName, originalFileName, mimeType, input)
        }
    }

    fun setPendingPickedFile(uriString: String, originalFileName: String, mimeType: String?) {
        mutableState.value = mutableState.value.copy(
            pendingPickedFile = PendingPickedFile(
                uriString = uriString,
                originalFileName = originalFileName,
                mimeType = mimeType
            )
        )
    }

    fun clearPendingPickedFile() {
        mutableState.value = mutableState.value.copy(pendingPickedFile = null)
    }

    fun drawRandom() {
        if (mutableState.value.isDrawing) return
        mutableState.value = mutableState.value.copy(isDrawing = true, drawPopupVisible = true)

        viewModelScope.launch {
            delay(drawAnimationMillis)
            val current = mutableState.value.copy(isDrawing = false)
            val next = current.draw(randomDrawUseCase)
            mutableState.value = next
            val result = next.lastResult
            if (result.items.isNotEmpty()) {
                repository.saveLastResult(result)
                if (current.drawMode == DrawMode.SINGLE) {
                    val itemId = result.items.single().id
                    val streakCount = if (itemId == current.lastSingleItemId) {
                        current.lastSingleStreakCount + 1
                    } else {
                        1
                    }
                    mutableState.value = mutableState.value.copy(
                        lastSingleItemId = itemId,
                        lastSingleStreakCount = streakCount
                    )
                    repository.updateSingleDrawStreak(current.selectedSpaceId ?: result.spaceId, itemId, streakCount)
                }
            }
        }
    }

    fun dismissDrawPopup() {
        mutableState.value = mutableState.value.dismissDrawPopup()
    }

    fun setDrawMode(mode: DrawMode) {
        val current = mutableState.value
        mutableState.value = current.copy(drawMode = mode)
        current.selectedSpaceId?.let { spaceId ->
            viewModelScope.launch { repository.updateDrawSettings(spaceId, mode, current.drawCount) }
        }
    }

    fun setDrawCount(count: Int) {
        val current = mutableState.value
        val next = count.coerceAtLeast(1)
        mutableState.value = current.copy(drawCount = next)
        current.selectedSpaceId?.let { spaceId ->
            viewModelScope.launch { repository.updateDrawSettings(spaceId, current.drawMode, next) }
        }
    }

    fun setSingleRepeatLimit(limit: Int) {
        val current = mutableState.value
        val next = limit.coerceAtLeast(0)
        mutableState.value = current.copy(singleRepeatLimit = next)
        current.selectedSpaceId?.let { spaceId ->
            viewModelScope.launch { repository.updateRepeatSettings(spaceId, next, current.multiRepeatLimit) }
        }
    }

    fun setMultiRepeatLimit(limit: Int) {
        val current = mutableState.value
        val next = limit.coerceAtLeast(0)
        mutableState.value = current.copy(multiRepeatLimit = next)
        current.selectedSpaceId?.let { spaceId ->
            viewModelScope.launch { repository.updateRepeatSettings(spaceId, current.singleRepeatLimit, next) }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val next = if (mutableState.value.themeMode == ThemeMode.AMOLED) ThemeMode.LIGHT else ThemeMode.AMOLED
            repository.setTheme(next)
        }
    }

    fun toggleDrawer() {
        mutableState.value = mutableState.value.copy(drawerOpen = !mutableState.value.drawerOpen)
    }

    fun toggleResultExpanded() {
        val next = mutableState.value.toggleResultExpanded()
        mutableState.value = next
        val result = next.lastResult
        if (result.items.isNotEmpty()) {
            viewModelScope.launch { repository.saveLastResult(result) }
        }
    }
}
