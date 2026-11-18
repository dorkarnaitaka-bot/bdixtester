package com.jubayerhossain.bdixtester.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Modern Network Pro brand & diagnostic palette
val NavyDeep = Color(0xFF041E2D)
val NavySurface = Color(0xFF0D1722)

// Primary Cyan-Sky diagnostic accents
val CyanPrimaryLight = Color(0xFF0277B5)
val CyanPrimaryContainerLight = Color(0xFFD6EEFF)
val CyanPrimaryDark = Color(0xFF38BDF8)
val CyanPrimaryContainerDark = Color(0xFF0C3B57)

// Secondary Sky-Cyan cohesive container accents (eliminates green-vs-cyan clash)
val TealSecondaryLight = Color(0xFF0369A1)
val TealSecondaryContainerLight = Color(0xFFE0F2FE)
val TealSecondaryDark = Color(0xFF7DD3FC)
val TealSecondaryContainerDark = Color(0xFF10344A)

// Tertiary Neutral Slate
val SlateTertiaryLight = Color(0xFF475A6E)
val SlateTertiaryContainerLight = Color(0xFFE1EAF2)
val SlateTertiaryDark = Color(0xFF9FB0C4)
val SlateTertiaryContainerDark = Color(0xFF26384B)

// Error / Destructive semantic palette
val ErrorLight = Color(0xFFDC2626)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF7F1D1D)
val ErrorDark = Color(0xFFF87171)
val ErrorContainerDark = Color(0xFF5C1919)
val OnErrorContainerDark = Color(0xFFFECACA)

// Light Theme M3 Tonal Surface Hierarchy
val LightBackground = Color(0xFFF4F7FA)
val LightSurface = Color(0xFFF4F7FA)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFFFFFFF)
val LightSurfaceContainer = Color(0xFFEBF1F7)
val LightSurfaceContainerHigh = Color(0xFFE1EAF2)
val LightSurfaceContainerHighest = Color(0xFFD6E2EE)
val LightSurfaceVariant = Color(0xFFE5EEF6)
val LightOutline = Color(0xFFC2D0DF)
val LightOutlineVariant = Color(0xFFDCE5EF)
val LightOnSurface = Color(0xFF0B1521)
val LightOnSurfaceVariant = Color(0xFF475A6E)

// Dark Theme M3 Tonal Surface Hierarchy (Deep Ocean Slate, avoiding pure #000000)
val DarkBackground = Color(0xFF081018)
val DarkSurface = Color(0xFF081018)
val DarkSurfaceContainerLowest = Color(0xFF050B11)
val DarkSurfaceContainerLow = Color(0xFF0D1722)
val DarkSurfaceContainer = Color(0xFF12202E)
val DarkSurfaceContainerHigh = Color(0xFF182A3C)
val DarkSurfaceContainerHighest = Color(0xFF20364C)
val DarkSurfaceVariant = Color(0xFF162636)
val DarkOutline = Color(0xFF334A61)
val DarkOutlineVariant = Color(0xFF233649)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF9FB0C4)

@Immutable
data class BdixSemanticColors(
    val reachable: Color,
    val reachableContainer: Color,
    val restricted: Color,
    val restrictedContainer: Color,
    val unreachable: Color,
    val unreachableContainer: Color,
    val neutral: Color,
    val neutralContainer: Color,
    val ringGradientStart: Color,
    val ringGradientMid: Color,
    val ringGradientEnd: Color,
    val ringTrack: Color
)

val LightSemanticColors = BdixSemanticColors(
    reachable = Color(0xFF059669),
    reachableContainer = Color(0xFFD1FAE5),
    restricted = Color(0xFFD97706),
    restrictedContainer = Color(0xFFFEF3C7),
    unreachable = Color(0xFFDC2626),
    unreachableContainer = Color(0xFFFEE2E2),
    neutral = Color(0xFF475A6E),
    neutralContainer = Color(0xFFE2EBF3),
    ringGradientStart = Color(0xFF06B6D4),
    ringGradientMid = Color(0xFF0284C7),
    ringGradientEnd = Color(0xFF2563EB),
    ringTrack = Color(0xFFDCE6F0)
)

val DarkSemanticColors = BdixSemanticColors(
    reachable = Color(0xFF34D399),
    reachableContainer = Color(0xFF064E3B),
    restricted = Color(0xFFFBBF24),
    restrictedContainer = Color(0xFF5C3409),
    unreachable = Color(0xFFF87171),
    unreachableContainer = Color(0xFF5C1919),
    neutral = Color(0xFF9FB0C4),
    neutralContainer = Color(0xFF1A2B3D),
    ringGradientStart = Color(0xFF22D3EE),
    ringGradientMid = Color(0xFF38BDF8),
    ringGradientEnd = Color(0xFF3B82F6),
    ringTrack = Color(0xFF182A3C)
)

val LocalBdixSemanticColors = staticCompositionLocalOf { DarkSemanticColors }
