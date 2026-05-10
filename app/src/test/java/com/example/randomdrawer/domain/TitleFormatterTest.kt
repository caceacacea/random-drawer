package com.example.randomdrawer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleFormatterTest {
    @Test
    fun usesShortTextForTextItemTitle() {
        assertEquals(
            "Weekend plan ideas",
            TitleFormatter.fromFirstItem("Weekend plan ideas\nwith more notes")
        )
    }

    @Test
    fun truncatesLongTitle() {
        assertEquals(
            "This title is long enough...",
            TitleFormatter.fromFirstItem("This title is long enough that it needs trimming")
        )
    }

    @Test
    fun usesNewDrawForBlankTitle() {
        assertEquals("New draw", TitleFormatter.fromFirstItem("   "))
    }
}
