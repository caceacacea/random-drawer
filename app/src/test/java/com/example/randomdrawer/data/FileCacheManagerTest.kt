package com.example.randomdrawer.data

import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileCacheManagerTest {
    @Test
    fun copiesContentIntoPrivateCacheDirectory() {
        val root = Files.createTempDirectory("random-drawer-cache").toFile()
        val manager = FileCacheManager(root)

        val cached = manager.copyToCache(
            spaceId = 7L,
            itemId = 9L,
            originalFileName = "IMG_4832.jpg",
            input = ByteArrayInputStream("image bytes".toByteArray())
        )

        assertTrue(File(cached.path).exists())
        assertTrue(cached.path.contains("random_drawer_cache"))
        assertEquals("IMG_4832.jpg", cached.originalFileName)
    }

    @Test
    fun deleteAllCacheDeletesFilesButDoesNotRequireDatabase() {
        val root = Files.createTempDirectory("random-drawer-cache").toFile()
        val manager = FileCacheManager(root)
        val cached = manager.copyToCache(1L, 2L, "clip.mp4", ByteArrayInputStream("video".toByteArray()))

        val deleted = manager.deleteCachedFiles(listOf(cached.path))

        assertEquals(1, deleted)
        assertFalse(File(cached.path).exists())
    }
}
