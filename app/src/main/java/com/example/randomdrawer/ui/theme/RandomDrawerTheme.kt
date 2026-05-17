package com.example.randomdrawer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.randomdrawer.domain.ThemeMode

private val AccentGreen = Color(0xFF36D9CF)

private val AmoledScheme = darkColorScheme(
    primary = AccentGreen,
    background = Color.Black,
    surface = Color(0xFF090D0F),
    surfaceVariant = Color(0xFF141C1F),
    onPrimary = Color(0xFF041313),
    onBackground = Color(0xFFF2FBFA),
    onSurface = Color(0xFFF2FBFA),
    onSurfaceVariant = Color(0xFFA9B8BA)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF168E87),
    background = Color(0xFFF8FAF9),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF1EE),
    onPrimary = Color.White,
    onBackground = Color(0xFF202422),
    onSurface = Color(0xFF202422),
    onSurfaceVariant = Color(0xFF5E6762)
)

private val SoftTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.Medium),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.Medium),
        bodyMedium = bodyMedium.copy(fontWeight = FontWeight.Normal)
    )
}

@Composable
fun RandomDrawerTheme(
    themeMode: ThemeMode = ThemeMode.AMOLED,
    content: @Composable () -> Unit
) {
    val scheme: ColorScheme = if (themeMode == ThemeMode.LIGHT) LightScheme else AmoledScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = SoftTypography,
        content = content
    )
}
