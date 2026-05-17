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

private val AccentGreen = Color(0xFF22D6C9)

private val AmoledScheme = darkColorScheme(
    primary = AccentGreen,
    background = Color.Black,
    surface = Color(0xFF070707),
    surfaceVariant = Color(0xFF191B1D),
    onPrimary = Color(0xFF041313),
    onBackground = Color(0xFFF5F8F8),
    onSurface = Color(0xFFF5F8F8),
    onSurfaceVariant = Color(0xFFC0C8CA)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF008C86),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDEFF1),
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
