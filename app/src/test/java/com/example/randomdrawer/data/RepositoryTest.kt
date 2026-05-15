package com.example.randomdrawer.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.ItemKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val db = Room.inMemoryDatabaseBuilder(context, RandomDrawerDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val dao = db.dao()

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun createsSpaceAndStoresItemsForThatSpaceOnly() = runTest {
        val firstSpace = dao.insertSpace(DrawSpaceEntity(title = "First", createdAtMillis = 1L, updatedAtMillis = 1L))
        val secondSpace = dao.insertSpace(DrawSpaceEntity(title = "Second", createdAtMillis = 2L, updatedAtMillis = 2L))

        dao.insertItem(DrawerItemEntity(spaceId = firstSpace, kind = ItemKind.TEXT.name, displayName = "Text one", createdAtMillis = 3L))
        dao.insertItem(DrawerItemEntity(spaceId = secondSpace, kind = ItemKind.TEXT.name, displayName = "Text two", createdAtMillis = 4L))

        val firstItems = dao.observeItems(firstSpace).first()

        assertEquals(1, firstItems.size)
        assertEquals("Text one", firstItems.single().displayName)
    }

    @Test
    fun storesDrawModeDrawCountAndThemeSetting() = runTest {
        val spaceId = dao.insertSpace(DrawSpaceEntity(title = "First", createdAtMillis = 1L, updatedAtMillis = 1L))

        dao.updateDrawSettings(spaceId, DrawMode.MULTIPLE.name, 4)
        dao.upsertSetting(AppSettingEntity(key = "theme", value = "LIGHT"))

        val space = dao.observeSpaces().first().single()
        val theme = dao.getSetting("theme")

        assertEquals(DrawMode.MULTIPLE.name, space.drawMode)
        assertEquals(4, space.drawCount)
        assertEquals("LIGHT", theme?.value)
    }

    @Test
    fun storesLastResultExpansionStateForSpace() = runTest {
        val spaceId = dao.insertSpace(DrawSpaceEntity(title = "First", createdAtMillis = 1L, updatedAtMillis = 1L))

        dao.upsertLastResult(LastResultEntity(spaceId = spaceId, itemIdsCsv = "7,9", createdAtMillis = 5L, expanded = true))

        val result = dao.observeLastResult(spaceId).first()

        assertEquals("7,9", result?.itemIdsCsv)
        assertEquals(true, result?.expanded)
    }

    @Test
    fun clearsCachePathsAcrossAllSpacesWithoutRemovingEntries() = runTest {
        val firstSpace = dao.insertSpace(DrawSpaceEntity(title = "First", createdAtMillis = 1L, updatedAtMillis = 1L))
        val secondSpace = dao.insertSpace(DrawSpaceEntity(title = "Second", createdAtMillis = 2L, updatedAtMillis = 2L))
        dao.insertItem(
            DrawerItemEntity(
                spaceId = firstSpace,
                kind = ItemKind.FILE.name,
                displayName = "First file",
                cachedFilePath = "/cache/first.jpg",
                createdAtMillis = 3L
            )
        )
        dao.insertItem(
            DrawerItemEntity(
                spaceId = secondSpace,
                kind = ItemKind.FILE.name,
                displayName = "Second file",
                cachedFilePath = "/cache/second.jpg",
                createdAtMillis = 4L
            )
        )

        val clearedCount = dao.clearAllCachedPaths()

        assertEquals(2, clearedCount)
        assertEquals(emptyList<String>(), dao.getAllCachedPaths())
        assertEquals(1, dao.observeItems(firstSpace).first().size)
        assertEquals(1, dao.observeItems(secondSpace).first().size)
    }
}
