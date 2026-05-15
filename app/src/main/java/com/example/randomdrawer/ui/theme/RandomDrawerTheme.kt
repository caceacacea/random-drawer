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

private val AccentGreen = Color(0xFF1D9A86)

private val AmoledScheme = darkColorScheme(
    primary = AccentGreen,
    background = Color.Black,
    surface = Color(0xFF0A0C0B),
    surfaceVariant = Color(0xFF101412),
    onPrimary = Color.White,
    onBackground = Color(0xFFF4FAF7),
    onSurface = Color(0xFFF4FAF7),
    onSurfaceVariant = Color(0xFFB7C3BD)
)

private val LightScheme = lightColorScheme(
    primary = AccentGreen,
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
