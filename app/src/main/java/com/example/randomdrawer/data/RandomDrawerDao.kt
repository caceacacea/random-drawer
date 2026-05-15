package com.example.randomdrawer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RandomDrawerDao {
    @Query("SELECT * FROM draw_spaces ORDER BY updatedAtMillis DESC")
    fun observeSpaces(): Flow<List<DrawSpaceEntity>>

    @Query("SELECT * FROM draw_spaces WHERE id = :spaceId")
    suspend fun getSpace(spaceId: Long): DrawSpaceEntity?

    @Query("SELECT * FROM draw_spaces ORDER BY updatedAtMillis DESC")
    suspend fun getSpaces(): List<DrawSpaceEntity>

    @Insert
    suspend fun insertSpace(space: DrawSpaceEntity): Long

    @Query("DELETE FROM draw_spaces WHERE id = :spaceId")
    suspend fun deleteSpace(spaceId: Long): Int

    @Query("UPDATE draw_spaces SET title = :title, updatedAtMillis = :updatedAtMillis WHERE id = :spaceId")
    suspend fun updateSpaceTitle(spaceId: Long, title: String, updatedAtMillis: Long)

    @Query("UPDATE draw_spaces SET updatedAtMillis = :updatedAtMillis WHERE id = :spaceId")
    suspend fun touchSpace(spaceId: Long, updatedAtMillis: Long)

    @Query("UPDATE draw_spaces SET drawMode = :drawMode, drawCount = :drawCount WHERE id = :spaceId")
    suspend fun updateDrawSettings(spaceId: Long, drawMode: String, drawCount: Int)

    @Query("UPDATE draw_spaces SET singleRepeatLimit = :singleRepeatLimit, multiRepeatLimit = :multiRepeatLimit WHERE id = :spaceId")
    suspend fun updateRepeatSettings(spaceId: Long, singleRepeatLimit: Int, multiRepeatLimit: Int)

    @Query("UPDATE draw_spaces SET lastSingleItemId = :lastSingleItemId, lastSingleStreakCount = :lastSingleStreakCount WHERE id = :spaceId")
    suspend fun updateSingleDrawStreak(spaceId: Long, lastSingleItemId: Long?, lastSingleStreakCount: Int)

    @Query("SELECT * FROM drawer_items WHERE spaceId = :spaceId ORDER BY createdAtMillis DESC")
    fun observeItems(spaceId: Long): Flow<List<DrawerItemEntity>>

    @Query("SELECT * FROM drawer_items WHERE spaceId = :spaceId ORDER BY createdAtMillis DESC")
    suspend fun getItems(spaceId: Long): List<DrawerItemEntity>

    @Query("SELECT * FROM drawer_items WHERE id = :itemId")
    suspend fun getItem(itemId: Long): DrawerItemEntity?

    @Insert
    suspend fun insertItem(item: DrawerItemEntity): Long

    @Query("DELETE FROM drawer_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long): Int

    @Query("UPDATE drawer_items SET cachedFilePath = :cachedFilePath WHERE id = :itemId")
    suspend fun updateCachedFilePath(itemId: Long, cachedFilePath: String)

    @Query("UPDATE drawer_items SET cachedFilePath = NULL WHERE cachedFilePath IS NOT NULL")
    suspend fun clearAllCachedPaths(): Int

    @Query("SELECT cachedFilePath FROM drawer_items WHERE cachedFilePath IS NOT NULL")
    suspend fun getAllCachedPaths(): List<String>

    @Query("SELECT cachedFilePath FROM drawer_items WHERE spaceId = :spaceId AND cachedFilePath IS NOT NULL")
    suspend fun getCachedPathsForSpace(spaceId: Long): List<String>

    @Query("SELECT * FROM last_results WHERE spaceId = :spaceId")
    fun observeLastResult(spaceId: Long): Flow<LastResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLastResult(result: LastResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetting(setting: AppSettingEntity)

    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Query("SELECT * FROM app_settings WHERE key = :key")
    fun observeSetting(key: String): Flow<AppSettingEntity?>
}
