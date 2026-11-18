package com.jubayerhossain.bdixtester.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.model.ScanMode
import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.core.network.NetworkSnapshot
import com.jubayerhossain.bdixtester.core.network.TransportType
import com.jubayerhossain.bdixtester.core.util.ExternalBrowserHelper
import com.jubayerhossain.bdixtester.ui.components.EmptyStateView
import com.jubayerhossain.bdixtester.ui.components.LtrContent
import com.jubayerhossain.bdixtester.ui.components.MetricTile
import com.jubayerhossain.bdixtester.ui.components.ServerCard
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme
import com.jubayerhossain.bdixtester.ui.theme.LocalBdixSemanticColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onOpenScan: (ScanMode) -> Unit,
    onOpenCategory: (ServerCategory) -> Unit,
    onOpenServerDetails: (String) -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val semantic = LocalBdixSemanticColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(BdixSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BdixSpacing.lg)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.dashboard_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(BdixSpacing.xs))

                ConnectivityCard(
                    snapshot = uiState.networkSnapshot,
                    manualProfileLabel = uiState.manualNetworkLabel
                )
            }
        }

        item {
            val audit = uiState.auditSummary
            val hasDiscrepancy = audit.discrepancyCount != 0
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .testTag("catalog_audit_banner"),
                shape = MaterialTheme.shapes.medium,
                color = if (hasDiscrepancy) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(BdixSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.md),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (hasDiscrepancy) Icons.Default.Info else Icons.Default.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.dashboard_catalog_notice_title),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = if (hasDiscrepancy) {
                                stringResource(
                                    R.string.dashboard_catalog_discrepancy_body,
                                    audit.originalEndpointCount,
                                    audit.uniqueEndpointCount,
                                    audit.exactDuplicateCount,
                                    audit.expectedSourceCount
                                )
                            } else {
                                stringResource(
                                    R.string.dashboard_catalog_verified_body,
                                    uiState.builtInCount,
                                    uiState.remoteCount,
                                    uiState.customCount
                                )
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.catalogSyncState.catalogSequence > 0L) {
                            LtrContent {
                                Text(
                                    text = "Remote Sync Seq #${uiState.catalogSyncState.catalogSequence} · ${uiState.catalogSyncState.lastSyncStatus.displayLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_catalog_stats_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    MetricTile(
                        label = stringResource(R.string.dashboard_stat_total_servers),
                        value = uiState.totalServers.toString(),
                        icon = Icons.Default.Dns,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = stringResource(R.string.dashboard_stat_categories),
                        value = uiState.categoryCounts.count { it.value > 0 }.toString(),
                        icon = Icons.Default.Category,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    MetricTile(
                        label = stringResource(R.string.dashboard_stat_tested),
                        value = uiState.testedCount.toString(),
                        icon = Icons.Default.NetworkCheck,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = stringResource(R.string.dashboard_stat_reachable),
                        value = uiState.reachableCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = semantic.reachable,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    MetricTile(
                        label = stringResource(R.string.dashboard_stat_restricted),
                        value = uiState.restrictedOrErrorCount.toString(),
                        icon = Icons.Default.Lock,
                        accentColor = semantic.restricted,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = stringResource(R.string.dashboard_stat_unreachable),
                        value = uiState.unreachableCount.toString(),
                        icon = Icons.Default.ErrorOutline,
                        accentColor = semantic.unreachable,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_scan_modes_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(BdixSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.action_quick_scan),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = stringResource(R.string.dashboard_quick_scan_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(
                                onClick = { onOpenScan(ScanMode.QUICK_SCAN) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("dashboard_quick_scan_button")
                            ) {
                                Text(stringResource(R.string.action_quick_scan))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(BdixSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = stringResource(R.string.action_full_scan),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = stringResource(R.string.dashboard_full_scan_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { onOpenScan(ScanMode.FULL_SCAN) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("dashboard_full_scan_button")
                            ) {
                                Text(stringResource(R.string.action_full_scan))
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_categories_title),
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(BdixSpacing.xs)
                ) {
                    ServerCategory.entries.forEach { category ->
                        val count = if (category == ServerCategory.ALL) {
                            uiState.totalServers
                        } else {
                            uiState.categoryCounts[category] ?: 0
                        }
                        if (category == ServerCategory.ALL || count > 0 || category == ServerCategory.UNCATEGORIZED) {
                            AssistChip(
                                onClick = { onOpenCategory(category) },
                                label = { Text("${category.displayName} ($count)") },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("dashboard_category_chip_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        if (uiState.featuredServers.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp),
                    verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_featured_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    uiState.featuredServers.forEach { endpoint ->
                        val fav = uiState.favorites[endpoint.id]
                        ServerCard(
                            endpoint = endpoint,
                            result = null,
                            isFavorite = fav != null,
                            isPinned = fav?.isPinned == true,
                            onToggleFavorite = { onToggleFavorite(endpoint.id) },
                            onCardClick = { onOpenServerDetails(endpoint.id) },
                            onVisitWebsiteClick = {
                                ExternalBrowserHelper.openUrl(context, endpoint.originalUrl)
                            }
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_recent_title),
                    style = MaterialTheme.typography.titleMedium
                )
                if (uiState.recentlyTested.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.CloudOff,
                        title = stringResource(R.string.label_not_yet_tested),
                        description = stringResource(R.string.dashboard_recent_empty),
                        tag = "dashboard_recent_empty"
                    )
                } else {
                    uiState.recentlyTested.forEach { (endpoint, result) ->
                        val fav = uiState.favorites[endpoint.id]
                        ServerCard(
                            endpoint = endpoint,
                            result = result,
                            isFavorite = fav != null,
                            isPinned = fav?.isPinned == true,
                            onToggleFavorite = { onToggleFavorite(endpoint.id) },
                            onCardClick = { onOpenServerDetails(endpoint.id) },
                            onVisitWebsiteClick = {
                                ExternalBrowserHelper.openUrl(context, endpoint.originalUrl)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectivityCard(
    snapshot: NetworkSnapshot,
    manualProfileLabel: String
) {
    val semantic = LocalBdixSemanticColors.current
    val icon = if (snapshot.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff
    val tint = if (snapshot.isConnected) semantic.reachable else semantic.neutral
    val title = if (snapshot.isConnected) {
        "${stringResource(R.string.dashboard_network_connected)} (${snapshot.transportType.displayLabel})"
    } else {
        stringResource(R.string.dashboard_network_offline)
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BdixSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(BdixSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (manualProfileLabel.isNotBlank()) {
                    Text(
                        text = "Profile: $manualProfileLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.dashboard_network_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                networkSnapshot = NetworkSnapshot(isConnected = true, transportType = TransportType.WIFI)
            ),
            onOpenScan = {},
            onOpenCategory = {},
            onOpenServerDetails = {}
        )
    }
}
