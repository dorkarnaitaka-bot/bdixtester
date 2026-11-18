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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.jubayerhossain.bdixtester.core.model.ThemePreference
import com.jubayerhossain.bdixtester.ui.components.LocalReducedMotion
import com.jubayerhossain.bdixtester.ui.components.rememberReducedMotionEnabled

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = CyanPrimaryContainerLight,
    onPrimaryContainer = NavyDeep,
    secondary = TealSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = TealSecondaryContainerLight,
    onSecondaryContainer = NavyDeep,
    tertiary = SlateTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SlateTertiaryContainerLight,
    onTertiaryContainer = LightOnSurface,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = NavyDeep,
    primaryContainer = CyanPrimaryContainerDark,
    onPrimaryContainer = CyanPrimaryContainerLight,
    secondary = TealSecondaryDark,
    onSecondary = NavyDeep,
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = CyanPrimaryContainerLight,
    tertiary = SlateTertiaryDark,
    onTertiary = NavyDeep,
    tertiaryContainer = SlateTertiaryContainerDark,
    onTertiaryContainer = DarkOnSurface,
    error = ErrorDark,
    onError = Color(0xFF3F0B0B),
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun BdixTesterTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = false,
    reduceAnimations: Boolean = false,
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
    val currentLocale = LocalConfiguration.current.locales.get(0)
    val activeTypography = if (currentLocale?.language == "bn") {
        BdixTypographyBengali
    } else {
        BdixTypography
    }
    val effectiveReducedMotion = rememberReducedMotionEnabled(userReduceAnimations = reduceAnimations)

    CompositionLocalProvider(
        LocalBdixSemanticColors provides semanticColors,
        LocalReducedMotion provides effectiveReducedMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = activeTypography,
            shapes = BdixShapes,
            content = content
        )
    }
}
