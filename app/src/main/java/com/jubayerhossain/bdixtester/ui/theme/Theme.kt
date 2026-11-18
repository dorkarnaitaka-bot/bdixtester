package com.jubayerhossain.bdixtester.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jubayerhossain.bdixtester.core.model.ThemePreference

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = CyanPrimaryContainerLight,
    onPrimaryContainer = NavyDeep,
    secondary = TealSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = TealSecondaryContainerLight,
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary = SlateTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SlateTertiaryContainerLight,
    onTertiaryContainer = LightOnSurface,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = NavyDeep,
    primaryContainer = CyanPrimaryContainerDark,
    onPrimaryContainer = CyanPrimaryContainerLight,
    secondary = TealSecondaryDark,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = TealSecondaryContainerLight,
    tertiary = SlateTertiaryDark,
    onTertiary = NavyDeep,
    tertiaryContainer = SlateTertiaryContainerDark,
    onTertiaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun BdixTesterTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val semanticColors = if (useDarkTheme) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalBdixSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BdixTypography,
            shapes = BdixShapes,
            content = content
        )
    }
}
