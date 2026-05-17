package com.example.randomdrawer.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawResult
import com.example.randomdrawer.domain.ItemKind
import java.io.ByteArrayInputStream
import java.io.File
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
    fun storesAnimationSettingsWithClampedDelay() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))

        repository.setAnimationsEnabled(false)
        repository.setAnimationDelayMillis(100L)

        assertEquals(false, repository.observeAnimationsEnabled().first())
        assertEquals(500L, repository.observeAnimationDelayMillis().first())

        repository.setAnimationDelayMillis(6000L)

        assertEquals(5000L, repository.observeAnimationDelayMillis().first())
    }

    @Test
    fun storesRepeatLimitsAndSingleDrawStreak() = runTest {
        val spaceId = dao.insertSpace(DrawSpaceEntity(title = "First", createdAtMillis = 1L, updatedAtMillis = 1L))

        dao.updateRepeatSettings(spaceId, singleRepeatLimit = 3, multiRepeatLimit = 2)
        dao.updateSingleDrawStreak(spaceId, lastSingleItemId = 9L, lastSingleStreakCount = 3)

        val space = dao.observeSpaces().first().single()

        assertEquals(3, space.singleRepeatLimit)
        assertEquals(2, space.multiRepeatLimit)
        assertEquals(9L, space.lastSingleItemId)
        assertEquals(3, space.lastSingleStreakCount)
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

    @Test
    fun ensureInitialSpaceCreatesDefaultSpaceAndAmoledTheme() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))

        val selectedId = repository.ensureInitialSpace()
        val spaces = dao.observeSpaces().first()
        val theme = dao.getSetting(RandomDrawerRepository.ThemeSettingKey)

        assertEquals(selectedId, spaces.single().id)
        assertEquals("New draw", spaces.single().title)
        assertEquals("AMOLED", theme?.value)
    }

    @Test
    fun firstAddedTextRenamesDefaultSpace() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val spaceId = repository.ensureInitialSpace()

        repository.addTextItem(spaceId, "Weekend plan ideas", nowMillis = 10L)
        val space = dao.getSpace(spaceId)

        assertEquals("Weekend plan ideas", space?.title)
    }

    @Test
    fun renameSpacePersistsCustomName() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val spaceId = repository.ensureInitialSpace()

        repository.renameSpace(spaceId, "Trip ideas", nowMillis = 10L)
        val space = dao.getSpace(spaceId)

        assertEquals("Trip ideas", space?.title)
        assertEquals(10L, space?.updatedAtMillis)
    }

    @Test
    fun createSpaceAddsBlankNewDrawSpace() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val firstSpaceId = repository.ensureInitialSpace(nowMillis = 1L)

        val secondSpaceId = repository.createSpace(nowMillis = 2L)
        val spaces = dao.observeSpaces().first()

        assertEquals(listOf(secondSpaceId, firstSpaceId), spaces.map { it.id })
        assertEquals("New draw", spaces.first().title)
    }

    @Test
    fun deleteAllCacheClearsPathsButKeepsFileEntries() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val spaceId = repository.ensureInitialSpace()
        dao.insertItem(
            DrawerItemEntity(
                spaceId = spaceId,
                kind = ItemKind.FILE.name,
                displayName = "my birthday",
                originalFileName = "IMG_4832.jpg",
                mimeType = "image/jpeg",
                cachedFilePath = File(context.filesDir, "random_drawer_cache/fake.jpg").absolutePath,
                createdAtMillis = 10L
            )
        )

        repository.deleteAllCache()
        val items = dao.getItems(spaceId)

        assertEquals(1, items.size)
        assertEquals(null, items.single().cachedFilePath)
        assertEquals("my birthday", items.single().displayName)
    }

    @Test
    fun deleteItemRemovesOnlyThatEntryAndCachedFile() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val spaceId = repository.ensureInitialSpace()
        val cachedFile = File(context.filesDir, "random_drawer_cache/delete-me.txt").apply {
            parentFile?.mkdirs()
            writeText("cached")
        }
        val fileItemId = dao.insertItem(
            DrawerItemEntity(
                spaceId = spaceId,
                kind = ItemKind.FILE.name,
                displayName = "Delete me",
                originalFileName = "delete-me.txt",
                cachedFilePath = cachedFile.absolutePath,
                createdAtMillis = 10L
            )
        )
        dao.insertItem(
            DrawerItemEntity(
                spaceId = spaceId,
                kind = ItemKind.TEXT.name,
                displayName = "Keep me",
                createdAtMillis = 11L
            )
        )

        repository.deleteItem(fileItemId)
        val remaining = dao.getItems(spaceId)

        assertEquals(listOf("Keep me"), remaining.map { it.displayName })
        assertEquals(false, cachedFile.exists())
    }

    @Test
    fun deleteSpaceRemovesEntriesAndCachedFiles() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val firstSpaceId = repository.ensureInitialSpace()
        val secondSpaceId = repository.createSpace(nowMillis = 2L)
        val cachedFile = File(context.filesDir, "random_drawer_cache/delete-space.txt").apply {
            parentFile?.mkdirs()
            writeText("cached")
        }
        dao.insertItem(
            DrawerItemEntity(
                spaceId = secondSpaceId,
                kind = ItemKind.FILE.name,
                displayName = "Space file",
                originalFileName = "delete-space.txt",
                cachedFilePath = cachedFile.absolutePath,
                createdAtMillis = 10L
            )
        )

        repository.deleteSpace(secondSpaceId)
        val spaces = dao.observeSpaces().first()

        assertEquals(listOf(firstSpaceId), spaces.map { it.id })
        assertEquals(emptyList<DrawerItemEntity>(), dao.getItems(secondSpaceId))
        assertEquals(false, cachedFile.exists())
    }

    @Test
    fun addCachedFileItemCopiesFileAndStoresMetadata() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val spaceId = repository.ensureInitialSpace()

        repository.addCachedFileItem(
            spaceId = spaceId,
            displayName = "my birthday",
            originalFileName = "IMG_4832.jpg",
            mimeType = "image/jpeg",
            input = ByteArrayInputStream("image bytes".toByteArray()),
            nowMillis = 10L
        )
        val item = dao.getItems(spaceId).single()

        assertEquals(ItemKind.FILE.name, item.kind)
        assertEquals("my birthday", item.displayName)
        assertEquals("IMG_4832.jpg", item.originalFileName)
        assertEquals("image/jpeg", item.mimeType)
        assertEquals("image bytes", File(item.cachedFilePath!!).readText())
    }

    @Test
    fun lastResultRestoresDrawnOrderAndCurrentCacheState() = runTest {
        val repository = RandomDrawerRepository(dao, FileCacheManager(context.filesDir))
        val spaceId = repository.ensureInitialSpace()
        dao.insertItem(
            DrawerItemEntity(
                spaceId = spaceId,
                kind = ItemKind.FILE.name,
                displayName = "my birthday",
                originalFileName = "IMG_4832.jpg",
                mimeType = "image/jpeg",
                cachedFilePath = File(context.filesDir, "random_drawer_cache/fake.jpg").absolutePath,
                createdAtMillis = 10L
            )
        )
        dao.insertItem(
            DrawerItemEntity(
                spaceId = spaceId,
                kind = ItemKind.TEXT.name,
                displayName = "Weekend plan ideas",
                createdAtMillis = 20L
            )
        )
        val items = repository.observeItems(spaceId).first()
        val fileItem = items.single { it.displayName == "my birthday" }
        val textItem = items.single { it.displayName == "Weekend plan ideas" }

        repository.saveLastResult(DrawResult(spaceId, listOf(fileItem, textItem), createdAtMillis = 30L, expanded = true))
        repository.deleteAllCache()
        val restored = repository.observeLastResult(spaceId).first()

        assertEquals(listOf("my birthday", "Weekend plan ideas"), restored.items.map { it.displayName })
        assertEquals(true, restored.expanded)
        assertEquals(null, restored.items.first().cachedFilePath)
    }
}
