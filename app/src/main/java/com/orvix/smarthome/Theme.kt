package com.orvix.smarthome

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, DARK, LIGHT }

val Accent = Color(0xFFFFE600)

data class GlassColors(
    val background: Brush,
    val card: Color,
    val cardBorder: Color,
    val text: Color,
    val subText: Color,
    val chip: Color,
    val chipSelected: Color,
    val chipSelectedText: Color
)

fun glassColors(dark: Boolean): GlassColors =
    if (dark) GlassColors(
        background = Brush.verticalGradient(listOf(Color(0xFF2B2F1E), Color(0xFF16181B), Color(0xFF0E0F11))),
        card = Color(0x26FFFFFF),
        cardBorder = Color(0x40FFFFFF),
        text = Color.White,
        subText = Color(0xB3FFFFFF),
        chip = Color(0x26FFFFFF),
        chipSelected = Color(0xFFF2F2F2),
        chipSelectedText = Color(0xFF111111)
    ) else GlassColors(
        background = Brush.verticalGradient(listOf(Color(0xFFE6EBF4), Color(0xFFF7F4FB), Color(0xFFDCE2EE))),
        card = Color(0x99FFFFFF),
        cardBorder = Color(0x33000000),
        text = Color(0xFF14161A),
        subText = Color(0xB314161A),
        chip = Color(0x66FFFFFF),
        chipSelected = Color(0xFF14161A),
        chipSelectedText = Color.White
    )

val LocalGlass = staticCompositionLocalOf { glassColors(true) }

@Composable
fun OrvixTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val scheme = if (dark) darkColorScheme(primary = Accent) else lightColorScheme(primary = Color(0xFF6B5F00))
    CompositionLocalProvider(LocalGlass provides glassColors(dark)) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
