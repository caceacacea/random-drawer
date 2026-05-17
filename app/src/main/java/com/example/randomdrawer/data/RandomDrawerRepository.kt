package com.example.randomdrawer.data

import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawResult
import com.example.randomdrawer.domain.DrawSpace
import com.example.randomdrawer.domain.DrawerItem
import com.example.randomdrawer.domain.DEFAULT_DRAW_ANIMATION_DELAY_MILLIS
import com.example.randomdrawer.domain.ItemKind
import com.example.randomdrawer.domain.ThemeMode
import com.example.randomdrawer.domain.TitleFormatter
import com.example.randomdrawer.domain.normalizeDrawAnimationDelayMillis
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RandomDrawerRepository(
    private val dao: RandomDrawerDao,
    private val fileCacheManager: FileCacheManager
) {
    companion object {
        const val ThemeSettingKey = "theme"
        const val AnimationsEnabledSettingKey = "animations_enabled"
        const val AnimationDelayMillisSettingKey = "animation_delay_millis"
    }

    suspend fun ensureInitialSpace(nowMillis: Long = System.currentTimeMillis()): Long {
        if (dao.getSetting(ThemeSettingKey) == null) {
            dao.upsertSetting(AppSettingEntity(ThemeSettingKey, ThemeMode.AMOLED.name))
        }
        if (dao.getSetting(AnimationsEnabledSettingKey) == null) {
            dao.upsertSetting(AppSettingEntity(AnimationsEnabledSettingKey, true.toString()))
        }
        if (dao.getSetting(AnimationDelayMillisSettingKey) == null) {
            dao.upsertSetting(AppSettingEntity(AnimationDelayMillisSettingKey, DEFAULT_DRAW_ANIMATION_DELAY_MILLIS.toString()))
        }

        val spaces = dao.observeSpaces().first()
        if (spaces.isNotEmpty()) return spaces.first().id

        return dao.insertSpace(
            DrawSpaceEntity(
                title = "New draw",
                createdAtMillis = nowMillis,
                updatedAtMillis = nowMillis
            )
        )
    }

    suspend fun createSpace(nowMillis: Long = System.currentTimeMillis()): Long {
        return dao.insertSpace(
            DrawSpaceEntity(
                title = "New draw",
                createdAtMillis = nowMillis,
                updatedAtMillis = nowMillis
            )
        )
    }

    suspend fun renameSpace(spaceId: Long, title: String, nowMillis: Long = System.currentTimeMillis()) {
        val trimmed = title.trim()
        if (trimmed.isNotEmpty()) {
            dao.updateSpaceTitle(spaceId, trimmed, nowMillis)
        }
    }

    suspend fun deleteSpace(spaceId: Long): Int {
        val paths = dao.getCachedPathsForSpace(spaceId)
        fileCacheManager.deleteCachedFiles(paths)
        return dao.deleteSpace(spaceId)
    }

    suspend fun getSpaces(): List<DrawSpace> {
        return dao.getSpaces().map { it.toDomain() }
    }

    fun observeSpaces(): Flow<List<DrawSpace>> = dao.observeSpaces().map { spaces ->
        spaces.map { it.toDomain() }
    }

    fun observeItems(spaceId: Long): Flow<List<DrawerItem>> = dao.observeItems(spaceId).map { items ->
        items.map { it.toDomain() }
    }

    fun observeLastResult(spaceId: Long): Flow<DrawResult> {
        return combine(dao.observeItems(spaceId), dao.observeLastResult(spaceId)) { itemEntities, resultEntity ->
            val itemsById = itemEntities.map { it.toDomain() }.associateBy { it.id }
            val resultItems = resultEntity
                ?.itemIdsCsv
                ?.split(",")
                ?.mapNotNull { value -> value.toLongOrNull()?.let(itemsById::get) }
                .orEmpty()

            DrawResult(
                spaceId = spaceId,
                items = resultItems,
                createdAtMillis = resultEntity?.createdAtMillis ?: 0L,
                expanded = resultEntity?.expanded ?: false
            )
        }
    }

    fun observeTheme(): Flow<ThemeMode> = dao.observeSetting(ThemeSettingKey).map { setting ->
        when (setting?.value) {
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            else -> ThemeMode.AMOLED
        }
    }

    suspend fun setTheme(themeMode: ThemeMode) {
        dao.upsertSetting(AppSettingEntity(ThemeSettingKey, themeMode.name))
    }

    fun observeAnimationsEnabled(): Flow<Boolean> = dao.observeSetting(AnimationsEnabledSettingKey).map { setting ->
        setting?.value?.toBooleanStrictOrNull() ?: true
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        dao.upsertSetting(AppSettingEntity(AnimationsEnabledSettingKey, enabled.toString()))
    }

    fun observeAnimationDelayMillis(): Flow<Long> = dao.observeSetting(AnimationDelayMillisSettingKey).map { setting ->
        normalizeDrawAnimationDelayMillis(setting?.value?.toLongOrNull() ?: DEFAULT_DRAW_ANIMATION_DELAY_MILLIS)
    }

    suspend fun setAnimationDelayMillis(delayMillis: Long) {
        dao.upsertSetting(
            AppSettingEntity(
                AnimationDelayMillisSettingKey,
                normalizeDrawAnimationDelayMillis(delayMillis).toString()
            )
        )
    }

    suspend fun addTextItem(spaceId: Long, text: String, nowMillis: Long = System.currentTimeMillis()) {
        dao.insertItem(
            DrawerItemEntity(
                spaceId = spaceId,
                kind = ItemKind.TEXT.name,
                displayName = text,
                createdAtMillis = nowMillis
            )
        )
        renameDefaultSpaceFromFirstItem(spaceId, text, nowMillis)
        dao.touchSpace(spaceId, nowMillis)
    }

    suspend fun addFileMetadata(
        spaceId: Long,
        displayName: String,
        originalFileName: String,
        mimeType: String?,
        cachedFilePath: String?,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val itemId = dao.insertItem(
            DrawerItemEntity(
                spaceId = spaceId,
                kind = ItemKind.FILE.name,
                displayName = displayName,
                originalFileName = originalFileName,
                mimeType = mimeType,
                cachedFilePath = cachedFilePath,
                createdAtMillis = nowMillis
            )
        )
        renameDefaultSpaceFromFirstItem(spaceId, displayName, nowMillis)
        dao.touchSpace(spaceId, nowMillis)
        return itemId
    }

    suspend fun addCachedFileItem(
        spaceId: Long,
        displayName: String,
        originalFileName: String,
        mimeType: String?,
        input: InputStream,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val itemId = addFileMetadata(
            spaceId = spaceId,
            displayName = displayName,
            originalFileName = originalFileName,
            mimeType = mimeType,
            cachedFilePath = null,
            nowMillis = nowMillis
        )
        val cached = fileCacheManager.copyToCache(spaceId, itemId, originalFileName, input)
        dao.updateCachedFilePath(itemId, cached.path)
    }

    suspend fun deleteItem(itemId: Long): Int {
        val item = dao.getItem(itemId) ?: return 0
        item.cachedFilePath?.let { fileCacheManager.deleteCachedFiles(listOf(it)) }
        return dao.deleteItem(itemId)
    }

    suspend fun updateDrawSettings(spaceId: Long, drawMode: DrawMode, drawCount: Int) {
        dao.updateDrawSettings(spaceId, drawMode.name, drawCount.coerceAtLeast(1))
    }

    suspend fun updateRepeatSettings(spaceId: Long, singleRepeatLimit: Int, multiRepeatLimit: Int) {
        dao.updateRepeatSettings(
            spaceId = spaceId,
            singleRepeatLimit = singleRepeatLimit.coerceAtLeast(0),
            multiRepeatLimit = multiRepeatLimit.coerceAtLeast(0)
        )
    }

    suspend fun updateSingleDrawStreak(spaceId: Long, lastSingleItemId: Long?, lastSingleStreakCount: Int) {
        dao.updateSingleDrawStreak(
            spaceId = spaceId,
            lastSingleItemId = lastSingleItemId,
            lastSingleStreakCount = lastSingleStreakCount.coerceAtLeast(0)
        )
    }

    suspend fun saveLastResult(result: DrawResult) {
        dao.upsertLastResult(
            LastResultEntity(
                spaceId = result.spaceId,
                itemIdsCsv = result.items.joinToString(",") { it.id.toString() },
                createdAtMillis = result.createdAtMillis,
                expanded = result.expanded
            )
        )
    }

    suspend fun deleteAllCache(): Int {
        val paths = dao.getAllCachedPaths()
        val deleted = fileCacheManager.deleteCachedFiles(paths)
        dao.clearAllCachedPaths()
        return deleted
    }

    private suspend fun renameDefaultSpaceFromFirstItem(spaceId: Long, value: String, nowMillis: Long) {
        val space = dao.getSpace(spaceId) ?: return
        if (space.title == "New draw") {
            dao.updateSpaceTitle(spaceId, TitleFormatter.fromFirstItem(value), nowMillis)
        }
    }
}

private fun DrawSpaceEntity.toDomain(): DrawSpace {
    return DrawSpace(
        id = id,
        title = title,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        drawMode = DrawMode.valueOf(drawMode),
        drawCount = drawCount,
        singleRepeatLimit = singleRepeatLimit,
        multiRepeatLimit = multiRepeatLimit,
        lastSingleItemId = lastSingleItemId,
        lastSingleStreakCount = lastSingleStreakCount
    )
}

private fun DrawerItemEntity.toDomain(): DrawerItem {
    return DrawerItem(
        id = id,
        spaceId = spaceId,
        kind = ItemKind.valueOf(kind),
        displayName = displayName,
        originalFileName = originalFileName,
        mimeType = mimeType,
        cachedFilePath = cachedFilePath,
        createdAtMillis = createdAtMillis
    )
}
