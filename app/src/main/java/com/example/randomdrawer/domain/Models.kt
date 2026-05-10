package com.example.randomdrawer.domain

enum class ItemKind {
    TEXT,
    FILE
}

enum class DrawMode {
    SINGLE,
    MULTIPLE
}

enum class ThemeMode {
    AMOLED,
    LIGHT
}

data class DrawSpace(
    val id: Long,
    val title: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val drawMode: DrawMode,
    val drawCount: Int
)

data class DrawerItem(
    val id: Long,
    val spaceId: Long,
    val kind: ItemKind,
    val displayName: String,
    val originalFileName: String?,
    val mimeType: String?,
    val cachedFilePath: String?,
    val createdAtMillis: Long
) {
    val hasCache: Boolean = kind == ItemKind.FILE && cachedFilePath != null
}

data class DrawResult(
    val spaceId: Long,
    val items: List<DrawerItem>,
    val createdAtMillis: Long,
    val expanded: Boolean = false
)
