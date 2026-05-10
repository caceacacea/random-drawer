package com.example.randomdrawer.domain

object TitleFormatter {
    private const val MaxTitleLength = 28

    fun fromFirstItem(value: String): String {
        val firstLine = value
            .lineSequence()
            .firstOrNull()
            ?.trim()
            .orEmpty()

        if (firstLine.isBlank()) return "New draw"

        return if (firstLine.length <= MaxTitleLength) {
            firstLine
        } else {
            firstLine.take(MaxTitleLength - 3).trimEnd() + "..."
        }
    }
}
