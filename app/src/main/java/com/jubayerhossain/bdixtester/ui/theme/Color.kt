package com.jubayerhossain.bdixtester.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core brand & diagnostic palette
val NavyDeep = Color(0xFF0B1D2E)
val NavySurface = Color(0xFF10253A)
val CyanPrimaryLight = Color(0xFF0284C7)
val CyanPrimaryContainerLight = Color(0xFFD8F0FF)
val CyanPrimaryDark = Color(0xFF38BDF8)
val CyanPrimaryContainerDark = Color(0xFF0B4666)

val TealSecondaryLight = Color(0xFF0F766E)
val TealSecondaryContainerLight = Color(0xFFCCFBF1)
val TealSecondaryDark = Color(0xFF2DD4BF)
val TealSecondaryContainerDark = Color(0xFF134E4A)

val SlateTertiaryLight = Color(0xFF475569)
val SlateTertiaryContainerLight = Color(0xFFE2E8F0)
val SlateTertiaryDark = Color(0xFF94A3B8)
val SlateTertiaryContainerDark = Color(0xFF334155)

val LightBackground = Color(0xFFF6F9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEAF0F6)
val LightOutline = Color(0xFFCBD5E1)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)

val DarkBackground = Color(0xFF09111A)
val DarkSurface = Color(0xFF0F1B29)
val DarkSurfaceVariant = Color(0xFF1A2A3B)
val DarkOutline = Color(0xFF2D4156)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)

@Immutable
data class BdixSemanticColors(
    val reachable: Color,
    val reachableContainer: Color,
    val restricted: Color,
    val restrictedContainer: Color,
    val unreachable: Color,
    val unreachableContainer: Color,
    val neutral: Color,
    val neutralContainer: Color
)

val LightSemanticColors = BdixSemanticColors(
    reachable = Color(0xFF047857),
    reachableContainer = Color(0xFFD1FAE5),
    restricted = Color(0xFFB45309),
    restrictedContainer = Color(0xFFFEF3C7),
    unreachable = Color(0xFFB91C1C),
    unreachableContainer = Color(0xFFFEE2E2),
    neutral = Color(0xFF475569),
    neutralContainer = Color(0xFFE2E8F0)
)

val DarkSemanticColors = BdixSemanticColors(
    reachable = Color(0xFF34D399),
    reachableContainer = Color(0xFF064E3B),
    restricted = Color(0xFFFBBF24),
    restrictedContainer = Color(0xFF78350F),
    unreachable = Color(0xFFF87171),
    unreachableContainer = Color(0xFF7F1D1D),
    neutral = Color(0xFF94A3B8),
    neutralContainer = Color(0xFF1E293B)
)

val LocalBdixSemanticColors = staticCompositionLocalOf { LightSemanticColors }
