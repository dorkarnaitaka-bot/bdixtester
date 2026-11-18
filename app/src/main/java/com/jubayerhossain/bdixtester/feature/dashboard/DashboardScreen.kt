package com.jubayerhossain.bdixtester.feature.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.model.ScanMode
import com.jubayerhossain.bdixtester.core.network.NetworkSnapshot
import com.jubayerhossain.bdixtester.core.network.TransportType
import com.jubayerhossain.bdixtester.ui.components.LtrContent
import com.jubayerhossain.bdixtester.ui.theme.BdixBorder
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme
import com.jubayerhossain.bdixtester.ui.theme.LocalBdixSemanticColors
import com.jubayerhossain.bdixtester.ui.theme.MonoHeroNumberStyle
import com.jubayerhossain.bdixtester.ui.theme.MonoHeroSubNumberStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onViewServers: () -> Unit,
    onStartScan: (ScanMode) -> Unit,
    onStopScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val semantic = LocalBdixSemanticColors.current
    var showWifiWarningDialog by remember { mutableStateOf(false) }

    val progress = uiState.scanProgress
    val isScanning = progress.isScanning
    val totalCount = if (isScanning && progress.totalCount > 0) progress.totalCount else uiState.totalServers
    val workingCount = if (isScanning) progress.reachableCount else uiState.reachableCount

    // Subtle completion haptic when a scan finishes naturally
    var wasScanning by remember { mutableStateOf(false) }
    LaunchedEffect(isScanning) {
        if (wasScanning && !isScanning && uiState.testedCount > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasScanning = isScanning
    }

    val primaryBorderBrush = remember(semantic) {
        Brush.horizontalGradient(
            colors = listOf(
                semantic.ringGradientStart,
                semantic.ringGradientMid,
                semantic.ringGradientEnd
            )
        )
    }

    val triggerStartScan = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (uiState.networkSnapshot.hasWifiTransport) {
            onStartScan(uiState.selectedScanMode)
        } else {
            showWifiWarningDialog = true
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen")
    ) {
        val isWideLayout = maxWidth >= 600.dp

        if (isWideLayout) {
            // Adaptive Landscape / Tablet 2-Column Split
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = BdixSpacing.xl, vertical = BdixSpacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val ringDiameter = (this@BoxWithConstraints.maxHeight * 0.62f).coerceIn(200.dp, 280.dp)
                        HeroTesterRing(
                            ringSize = ringDiameter,
                            isScanning = isScanning,
                            isStarting = showWifiWarningDialog,
                            progressFraction = progress.progressFraction,
                            workingCount = workingCount,
                            totalCount = totalCount,
                            onViewClick = onViewServers
                        )
                        CurrentlyTestingLine(
                            visible = isScanning && !progress.currentServerName.isNullOrBlank(),
                            serverName = progress.currentServerName.orEmpty(),
                            modifier = Modifier.padding(top = BdixSpacing.md)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(BdixBorder.hairline)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = BdixSpacing.xl, vertical = BdixSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PrimaryTestActionButton(
                        isScanning = isScanning,
                        primaryBorderBrush = primaryBorderBrush,
                        onStartClick = triggerStartScan,
                        onStopClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStopScan()
                        }
                    )
                }
            }
        } else {
            // Compact Portrait Layout (Scroll-safe for large system font scales)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(
                                top = BdixSpacing.xl,
                                bottom = BdixSpacing.xxl,
                                start = BdixSpacing.lg,
                                end = BdixSpacing.lg
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val ringDiameter = (this@BoxWithConstraints.maxWidth * 0.68f).coerceIn(216.dp, 272.dp)
                        HeroTesterRing(
                            ringSize = ringDiameter,
                            isScanning = isScanning,
                            isStarting = showWifiWarningDialog,
                            progressFraction = progress.progressFraction,
                            workingCount = workingCount,
                            totalCount = totalCount,
                            onViewClick = onViewServers
                        )
                        CurrentlyTestingLine(
                            visible = isScanning && !progress.currentServerName.isNullOrBlank(),
                            serverName = progress.currentServerName.orEmpty(),
                            modifier = Modifier.padding(top = BdixSpacing.md)
                        )
                    }
                }

                HorizontalDivider(
                    thickness = BdixBorder.hairline,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = BdixSpacing.xl, vertical = BdixSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryTestActionButton(
                        isScanning = isScanning,
                        primaryBorderBrush = primaryBorderBrush,
                        onStartClick = triggerStartScan,
                        onStopClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStopScan()
                        }
                    )
                }
            }
        }
    }

    if (showWifiWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWifiWarningDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = stringResource(R.string.wifi_warning_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.wifi_warning_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWifiWarningDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Gracefully ignore if settings activity is unavailable
                        }
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("wifi_warning_settings_button")
                ) {
                    Text(
                        text = stringResource(R.string.wifi_warning_settings),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWifiWarningDialog = false
                        onStartScan(uiState.selectedScanMode)
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("wifi_warning_continue_button")
                ) {
                    Text(
                        text = stringResource(R.string.wifi_warning_continue),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

@Composable
private fun PrimaryTestActionButton(
    isScanning: Boolean,
    primaryBorderBrush: Brush,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 360.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            Surface(
                onClick = onStopClick,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                border = BorderStroke(BdixBorder.hero, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("home_stop_test_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.home_action_stop),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            Surface(
                onClick = onStartClick,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(BdixBorder.hero, primaryBorderBrush),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("home_start_test_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.home_action_start_test),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentlyTestingLine(
    visible: Boolean,
    serverName: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        LtrContent {
            Text(
                text = stringResource(R.string.home_currently_testing, serverName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = BdixSpacing.md)
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .testTag("home_currently_testing_label")
            )
        }
    }
}

@Composable
private fun HeroTesterRing(
    ringSize: Dp,
    isScanning: Boolean,
    isStarting: Boolean,
    progressFraction: Float,
    workingCount: Int,
    totalCount: Int,
    onViewClick: () -> Unit
) {
    val semantic = LocalBdixSemanticColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "scan_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ring_rotation")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    val ringColors = listOf(
        semantic.ringGradientStart,
        semantic.ringGradientMid,
        semantic.ringGradientEnd,
        semantic.ringGradientStart
    )

    val ringA11yDescription = if (isScanning) {
        stringResource(
            R.string.home_ring_a11y_scanning,
            "%.1f".format(Locale.US, animatedProgress * 100f),
            workingCount
        )
    } else {
        stringResource(R.string.home_ring_a11y_idle, workingCount, totalCount)
    }

    Box(
        modifier = Modifier
            .size(ringSize)
            .testTag("home_tester_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerStrokePx = 11.dp.toPx()
            val innerStrokePx = 5.dp.toPx()
            val outerInset = outerStrokePx / 2f
            val outerArcSize = Size(size.width - outerStrokePx, size.height - outerStrokePx)
            val innerInset = outerStrokePx + 1.5.dp.toPx()
            val innerArcSize = Size(
                (size.width - innerInset * 2f).coerceAtLeast(0f),
                (size.height - innerInset * 2f).coerceAtLeast(0f)
            )
            val sweepBrush = Brush.sweepGradient(colors = ringColors)
            val innerSoftBrush = Brush.sweepGradient(
                colors = ringColors.map { it.copy(alpha = 0.35f) }
            )

            drawArc(
                color = semantic.ringGradientMid.copy(alpha = 0.14f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(outerInset, outerInset),
                size = outerArcSize,
                style = Stroke(width = outerStrokePx + 8.dp.toPx())
            )

            if (isScanning || isStarting) {
                drawArc(
                    color = semantic.ringTrack,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(outerInset, outerInset),
                    size = outerArcSize,
                    style = Stroke(width = outerStrokePx)
                )
                rotate(degrees = spinAngle) {
                    drawArc(
                        brush = sweepBrush,
                        startAngle = -90f,
                        sweepAngle = if (isStarting) 270f else (80f + 280f * animatedProgress).coerceAtMost(360f),
                        useCenter = false,
                        topLeft = Offset(outerInset, outerInset),
                        size = outerArcSize,
                        style = Stroke(width = outerStrokePx, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = innerSoftBrush,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(innerInset, innerInset),
                        size = innerArcSize,
                        style = Stroke(width = innerStrokePx)
                    )
                }
            } else {
                drawArc(
                    brush = sweepBrush,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(outerInset, outerInset),
                    size = outerArcSize,
                    style = Stroke(width = outerStrokePx, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = innerSoftBrush,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(innerInset, innerInset),
                    size = innerArcSize,
                    style = Stroke(width = innerStrokePx)
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = BdixSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.xs)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BdixSpacing.xs),
                modifier = Modifier.semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = ringA11yDescription
                }
            ) {
                when {
                    isStarting -> {
                        Text(
                            text = stringResource(R.string.home_starting_title),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.home_checking_internet),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    isScanning -> {
                        LtrContent {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.2f".format(Locale.US, animatedProgress * 100f),
                                    style = MonoHeroNumberStyle,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "%",
                                    style = MonoHeroSubNumberStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.home_testing_wait),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.home_working_servers_count_live, workingCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = semantic.reachable,
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        LtrContent {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = workingCount.toString(),
                                    style = MonoHeroNumberStyle,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "/$totalCount",
                                    style = MonoHeroSubNumberStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.home_working_servers_found),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(BdixSpacing.xs))

            OutlinedButton(
                onClick = onViewClick,
                shape = RoundedCornerShape(50),
                border = BorderStroke(BdixBorder.accent, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("home_view_servers_button")
            ) {
                Text(
                    text = stringResource(R.string.home_action_view),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    BdixTesterTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                totalServers = 369,
                testedCount = 369,
                reachableCount = 41,
                failedCount = 328,
                networkSnapshot = NetworkSnapshot(
                    isConnected = true,
                    transportType = TransportType.WIFI,
                    hasWifiTransport = true,
                    localIpAddress = "192.168.0.104"
                )
            ),
            onViewServers = {},
            onStartScan = {},
            onStopScan = {}
        )
    }
}
