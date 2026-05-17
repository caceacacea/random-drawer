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

const val DEFAULT_DRAW_ANIMATION_DELAY_MILLIS = 1000L
const val MIN_DRAW_ANIMATION_DELAY_MILLIS = 500L
const val MAX_DRAW_ANIMATION_DELAY_MILLIS = 5000L
const val DRAW_ANIMATION_DELAY_STEP_MILLIS = 250L

fun normalizeDrawAnimationDelayMillis(delayMillis: Long): Long {
    return delayMillis.coerceIn(MIN_DRAW_ANIMATION_DELAY_MILLIS, MAX_DRAW_ANIMATION_DELAY_MILLIS)
}

data class DrawSpace(
    val id: Long,
    val title: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val drawMode: DrawMode,
    val drawCount: Int,
    val singleRepeatLimit: Int = 0,
    val multiRepeatLimit: Int = 1,
    val lastSingleItemId: Long? = null,
    val lastSingleStreakCount: Int = 0
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
