package com.example.randomdrawer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.randomdrawer.domain.DrawMode

@Entity(tableName = "draw_spaces")
data class DrawSpaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val drawMode: String = DrawMode.SINGLE.name,
    val drawCount: Int = 2,
    val singleRepeatLimit: Int = 0,
    val multiRepeatLimit: Int = 1,
    val lastSingleItemId: Long? = null,
    val lastSingleStreakCount: Int = 0
)

@Entity(
    tableName = "drawer_items",
    foreignKeys = [
        ForeignKey(
            entity = DrawSpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("spaceId")]
)
data class DrawerItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spaceId: Long,
    val kind: String,
    val displayName: String,
    val originalFileName: String? = null,
    val mimeType: String? = null,
    val cachedFilePath: String? = null,
    val createdAtMillis: Long
)

@Entity(
    tableName = "last_results",
    foreignKeys = [
        ForeignKey(
            entity = DrawSpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("spaceId")]
)
data class LastResultEntity(
    @PrimaryKey val spaceId: Long,
    val itemIdsCsv: String,
    val createdAtMillis: Long,
    val expanded: Boolean
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
