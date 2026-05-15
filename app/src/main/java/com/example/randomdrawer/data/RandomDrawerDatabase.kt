package com.example.randomdrawer.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DrawSpaceEntity::class,
        DrawerItemEntity::class,
        LastResultEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class RandomDrawerDatabase : RoomDatabase() {
    abstract fun dao(): RandomDrawerDao
}
