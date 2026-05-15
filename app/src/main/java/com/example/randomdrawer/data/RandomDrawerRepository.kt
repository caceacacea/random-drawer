package com.example.randomdrawer.data

import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawSpace
import com.example.randomdrawer.domain.DrawerItem
import com.example.randomdrawer.domain.ItemKind
import com.example.randomdrawer.domain.ThemeMode
import com.example.randomdrawer.domain.TitleFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RandomDrawerRepository(
    private val dao: RandomDrawerDao,
    private val fileCacheManager: FileCacheManager
) {
    companion object {
        const val ThemeSettingKey = "theme"
    }

    suspend fun ensureInitialSpace(nowMillis: Long = System.currentTimeMillis()): Long {
        if (dao.getSetting(ThemeSettingKey) == null) {
            dao.upsertSetting(AppSettingEntity(ThemeSettingKey, ThemeMode.AMOLED.name))
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

    fun observeSpaces(): Flow<List<DrawSpace>> = dao.observeSpaces().map { spaces ->
        spaces.map { it.toDomain() }
    }

    fun observeItems(spaceId: Long): Flow<List<DrawerItem>> = dao.observeItems(spaceId).map { items ->
        items.map { it.toDomain() }
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

    suspend fun updateDrawSettings(spaceId: Long, drawMode: DrawMode, drawCount: Int) {
        dao.updateDrawSettings(spaceId, drawMode.name, drawCount.coerceAtLeast(1))
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
        drawCount = drawCount
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
