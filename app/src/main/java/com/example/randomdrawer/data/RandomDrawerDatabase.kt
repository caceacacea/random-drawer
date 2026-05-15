package com.example.randomdrawer.data

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DrawSpaceEntity::class,
        DrawerItemEntity::class,
        LastResultEntity::class,
        AppSettingEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class RandomDrawerDatabase : RoomDatabase() {
    abstract fun dao(): RandomDrawerDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE draw_spaces ADD COLUMN singleRepeatLimit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE draw_spaces ADD COLUMN multiRepeatLimit INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE draw_spaces ADD COLUMN lastSingleItemId INTEGER")
                db.execSQL("ALTER TABLE draw_spaces ADD COLUMN lastSingleStreakCount INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
