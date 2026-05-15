package com.example.randomdrawer.data

import java.io.File
import java.io.InputStream

data class CachedFile(
    val path: String,
    val originalFileName: String
)

class FileCacheManager(
    filesDir: File
) {
    private val cacheRoot: File = File(filesDir, "random_drawer_cache")

    fun copyToCache(
        spaceId: Long,
        itemId: Long,
        originalFileName: String,
        input: InputStream
    ): CachedFile {
        val safeName = originalFileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val spaceDir = File(cacheRoot, spaceId.toString()).apply { mkdirs() }
        val target = File(spaceDir, "${itemId}_$safeName")

        input.use { source ->
            target.outputStream().use { destination ->
                source.copyTo(destination)
            }
        }

        return CachedFile(path = target.absolutePath, originalFileName = originalFileName)
    }

    fun deleteCachedFiles(paths: List<String>): Int {
        return paths.count { path ->
            val file = File(path)
            file.exists() && file.delete()
        }
    }
}
