package com.jubayerhossain.bdixtester.feature.servers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.model.CatalogSourceType
import com.jubayerhossain.bdixtester.core.model.ScanMode
import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.core.model.ServerProtocol
import com.jubayerhossain.bdixtester.core.model.TestStatus
import com.jubayerhossain.bdixtester.core.util.ExternalBrowserHelper
import com.jubayerhossain.bdixtester.ui.components.CustomServerDialog
import com.jubayerhossain.bdixtester.ui.components.EmptyStateView
import com.jubayerhossain.bdixtester.ui.components.LtrContent
import com.jubayerhossain.bdixtester.ui.components.StatusBadge
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.MonoUrlStyle
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailsScreen(
    uiState: ServerDetailsUiState,
    onNavigateBack: () -> Unit,
    onRunSingleProbe: (ScanMode) -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    onStartSpeedTest: (String) -> Unit,
    onCancelSpeedTest: () -> Unit,
    onConfirmCrossHost: () -> Unit,
    onDismissCrossHost: () -> Unit,
    onSaveCustomEdit: (String, String, ServerCategory, String?, () -> Unit) -> Unit,
    onDeleteCustom: () -> Unit,
    onClearCustomEditError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val endpoint = uiState.endpoint
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var customSpeedInput by remember(endpoint?.id, endpoint?.speedTestUrl) {
        mutableStateOf(endpoint?.speedTestUrl.orEmpty())
    }

    Scaffold(
        modifier = modifier.testTag("server_details_screen"),
        topBar = {
            TopAppBar(
                title = { Text(endpoint?.name ?: stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (endpoint != null) {
                        val isFav = uiState.favoriteInfo != null
                        val isPinned = uiState.favoriteInfo?.isPinned == true
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("details_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = stringResource(
                                    if (isFav) R.string.action_remove_favorite else R.string.action_add_favorite
                                )
                            )
                        }
                        if (isFav) {
                            IconButton(
                                onClick = onTogglePin,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("details_pin_button")
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = stringResource(
                                        if (isPinned) R.string.action_unpin else R.string.action_pin
                                    )
                                )
                            }
                        }
                        if (endpoint.sourceType == CatalogSourceType.USER_CUSTOM) {
                            IconButton(
                                onClick = {
                                    onClearCustomEditError()
                                    showEditDialog = true
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.action_edit)
                                )
                            }
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = stringResource(R.string.action_delete)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (endpoint == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(BdixSpacing.lg),
                verticalArrangement = Arrangement.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.CloudOff,
                    title = stringResource(R.string.servers_empty_title),
                    description = stringResource(R.string.servers_empty_catalog_desc),
                    tag = "details_not_found_state"
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(BdixSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.lg)
        ) {
            item {
                DetailSectionCard(
                    title = stringResource(R.string.details_endpoint_section),
                    modifier = Modifier.widthIn(max = 720.dp)
                ) {
                    DetailRow(label = "Endpoint ID", value = endpoint.id, monospace = true)
                    DetailRow(label = "Name", value = endpoint.name)
                    DetailRow(label = "Original URL", value = endpoint.originalUrl, monospace = true)
                    DetailRow(label = "Parsed Host", value = endpoint.host, monospace = true)
                    DetailRow(
                        label = "Explicit Port",
                        value = endpoint.explicitPort?.toString() ?: "Default (${endpoint.protocol.displayLabel})",
                        monospace = true
                    )
                    DetailRow(label = "Category", value = endpoint.category.displayName)
                    DetailRow(label = "Protocol", value = endpoint.protocol.displayLabel)
                    if (endpoint.isPrivateIp) {
                        DetailRow(label = "Addressing", value = "RFC 1918 Private IPv4 (ISP/LAN-specific routing)")
                    }
                }
            }

            item {
                DetailSectionCard(
                    title = stringResource(R.string.details_verification_section),
                    modifier = Modifier.widthIn(max = 720.dp)
                ) {
                    DetailRow(label = "Catalog Source", value = endpoint.sourceType.displayLabel)
                    DetailRow(label = "Verification State", value = endpoint.verificationState.displayLabel)
                    endpoint.sourceAttribution?.let { DetailRow(label = "Attribution", value = it) }
                    DetailRow(
                        label = "Last Catalog Review",
                        value = endpoint.lastReviewedAtEpochMs?.let { formatTimestamp(it) } ?: "No review record"
                    )
                }
            }

            item {
                val latest = uiState.latestResult
                DetailSectionCard(
                    title = stringResource(R.string.details_latest_result_section),
                    modifier = Modifier.widthIn(max = 720.dp)
                ) {
                    StatusBadge(status = latest?.status ?: TestStatus.NOT_TESTED)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                    ) {
                        FilledTonalButton(
                            onClick = { onRunSingleProbe(ScanMode.QUICK_SCAN) },
                            enabled = !uiState.isSingleTesting,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("details_quick_probe_button")
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
                            Text(
                                text = stringResource(R.string.action_quick_scan),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        Button(
                            onClick = { onRunSingleProbe(ScanMode.FULL_SCAN) },
                            enabled = !uiState.isSingleTesting,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("details_full_probe_button")
                        ) {
                            Icon(imageVector = Icons.Default.Radar, contentDescription = null)
                            Text(
                                text = stringResource(R.string.action_full_scan),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }

                    if (latest == null) {
                        Text(
                            text = stringResource(R.string.details_not_tested_notice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        latest.dnsTimeMs?.let { DetailRow("DNS Resolution", "$it ms", monospace = true) }
                        latest.tcpConnectTimeMs?.let { DetailRow("TCP Connect", "$it ms", monospace = true) }
                        latest.tlsHandshakeTimeMs?.let { DetailRow("TLS Handshake", "$it ms", monospace = true) }
                        latest.httpResponseLatencyMs?.let { DetailRow("HTTP Response Latency", "$it ms", monospace = true) }
                        latest.ftpConnectLatencyMs?.let { DetailRow("FTP Control Latency", "$it ms", monospace = true) }
                        latest.httpStatusCode?.let { DetailRow("HTTP Status Code", "$it", monospace = true) }
                        latest.ftpGreetingCode?.let { DetailRow("FTP Greeting Code", "$it", monospace = true) }
                        latest.finalUrl?.let { DetailRow("Final Redirected URL", it, monospace = true) }
                        DetailRow("Error Classification", latest.errorClassification.displayLabel)
                        latest.failureMessage?.let { DetailRow("Diagnostic Detail", it) }
                        if (latest.isQuickScanInconclusive) {
                            Text(
                                text = "Quick Scan 2.5s budget expired before full stage completion; retry with Full Scan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (endpoint.protocol == ServerProtocol.HTTP || endpoint.protocol == ServerProtocol.HTTPS) {
                item {
                    val maxBytes = endpoint.speedTestMaxBytes ?: uiState.settings.speedTestMaxBytes
                    val maxMib = (maxBytes / (1024L * 1024L)).toInt().coerceAtLeast(1)
                    DetailSectionCard(
                        title = stringResource(R.string.details_speed_test_section),
                        modifier = Modifier.widthIn(max = 720.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.details_speed_test_disclaimer, maxMib),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LtrContent {
                            OutlinedTextField(
                                value = customSpeedInput,
                                onValueChange = { customSpeedInput = it },
                                label = { Text(stringResource(R.string.details_speed_test_url_input)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("details_speed_url_input")
                            )
                        }

                        if (uiState.isSpeedTestRunning) {
                            val prog = uiState.speedTestProgress
                            val fraction = if (prog != null && prog.maxBytes > 0L) {
                                (prog.transferredBytes.toFloat() / prog.maxBytes.toFloat()).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier.fillMaxWidth()
                            )
                            LtrContent {
                                Text(
                                    text = "${(prog?.transferredBytes ?: 0L) / 1024L} KiB / ${maxBytes / 1024L} KiB" +
                                        (prog?.liveMbps?.let { " · ${"%.2f".format(Locale.US, it)} Mbps" } ?: ""),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            OutlinedButton(
                                onClick = onCancelSpeedTest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("details_speed_cancel_button")
                            ) {
                                Text(stringResource(R.string.details_speed_test_cancel))
                            }
                        } else {
                            Button(
                                onClick = { onStartSpeedTest(customSpeedInput) },
                                enabled = customSpeedInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("details_speed_start_button")
                            ) {
                                Text(stringResource(R.string.details_speed_test_start))
                            }
                        }

                        uiState.lastSpeedTestSummary?.let { summary ->
                            DetailRow("Speed Test Outcome", summary.outcome.displayLabel)
                            DetailRow("Termination Reason", summary.terminationReason.name, monospace = true)
                            DetailRow("Transferred Bytes", "${summary.transferredBytes} bytes", monospace = true)
                            summary.downloadMbps?.let { mbps ->
                                val mibps = summary.downloadMibPerSec ?: 0.0
                                DetailRow(
                                    label = "Measured Rate",
                                    value = "${"%.2f".format(Locale.US, mbps)} Mbps (${"%.2f".format(Locale.US, mibps)} MiB/s)",
                                    monospace = true
                                )
                            }
                            DetailRow("Details", summary.message)
                        }
                    }
                }
            }

            item {
                val browserAllowed = ExternalBrowserHelper.isAllowedBrowserUrl(endpoint.originalUrl)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { ExternalBrowserHelper.openUrl(context, endpoint.originalUrl) },
                        enabled = browserAllowed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("details_visit_website_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                        Text(
                            text = stringResource(R.string.action_visit_website),
                            modifier = Modifier.padding(start = BdixSpacing.sm)
                        )
                    }
                    if (!browserAllowed) {
                        Text(
                            text = stringResource(R.string.details_open_browser_ftp_unsupported),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.endpointHistory.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.details_history_section),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 720.dp)
                    )
                }
                items(
                    items = uiState.endpointHistory.take(15),
                    key = { it.historyId }
                ) { entry ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 720.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(BdixSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = entry.status)
                                LtrContent {
                                    Text(
                                        text = formatTimestamp(entry.testedAtEpochMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            LtrContent {
                                Text(
                                    text = "${entry.scanMode.displayLabel} · latency=${entry.primaryLatencyMs ?: "-"} ms" +
                                        (entry.downloadMbps?.let { " · ${"%.2f".format(Locale.US, it)} Mbps" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.pendingCrossHostRedirect?.let { pending ->
        AlertDialog(
            onDismissRequest = onDismissCrossHost,
            title = { Text(stringResource(R.string.details_cross_host_title)) },
            text = { Text(stringResource(R.string.details_cross_host_body, pending.targetHost, pending.targetUrl)) },
            confirmButton = {
                Button(onClick = onConfirmCrossHost, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCrossHost, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showEditDialog && endpoint != null) {
        CustomServerDialog(
            initialEndpoint = endpoint,
            errorMessage = uiState.customEditError,
            onDismiss = {
                showEditDialog = false
                onClearCustomEditError()
            },
            onSave = { _, name, rawUrl, category, speedUrl ->
                onSaveCustomEdit(name, rawUrl, category, speedUrl) {
                    showEditDialog = false
                }
            }
        )
    }

    if (showDeleteDialog && endpoint != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.servers_dialog_delete_title)) },
            text = { Text(stringResource(R.string.servers_dialog_delete_body, endpoint.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteCustom()
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(BdixSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    monospace: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (monospace) {
            LtrContent {
                Text(
                    text = value,
                    style = MonoUrlStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTimestamp(epochMs: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.US)
        .format(Date(epochMs))
}
