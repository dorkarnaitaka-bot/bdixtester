package com.jubayerhossain.bdixtester.feature.favorites

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.export.ExportDataset
import com.jubayerhossain.bdixtester.core.export.ExportFormat
import com.jubayerhossain.bdixtester.core.model.ServerProtocol
import com.jubayerhossain.bdixtester.core.model.TestStatus
import com.jubayerhossain.bdixtester.core.util.ExternalBrowserHelper
import com.jubayerhossain.bdixtester.domain.model.ServerTestResult
import com.jubayerhossain.bdixtester.ui.components.EmptyStateView
import com.jubayerhossain.bdixtester.ui.components.ExportMenuButton
import com.jubayerhossain.bdixtester.ui.components.LtrContent
import com.jubayerhossain.bdixtester.ui.components.ServerCard
import com.jubayerhossain.bdixtester.ui.components.StatusBadge
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme
import com.jubayerhossain.bdixtester.ui.theme.MonoUrlStyle
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectStatusFilter: (TestStatus?) -> Unit,
    onSelectProtocolFilter: (ServerProtocol?) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSetPinned: (String, Boolean) -> Unit,
    onOpenServerDetails: (String) -> Unit,
    onDeleteHistoryEntry: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onExportFavorites: (ExportFormat) -> String,
    onExportHistory: (ExportFormat) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .padding(horizontal = BdixSpacing.lg, vertical = BdixSpacing.md),
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.nav_favorites),
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExportMenuButton(
                        dataset = if (selectedTab == 0) ExportDataset.FAVORITES else ExportDataset.HISTORY,
                        onGeneratePayload = { format ->
                            if (selectedTab == 0) onExportFavorites(format) else onExportHistory(format)
                        },
                        buttonTestTag = "saved_export_button"
                    )
                    if (selectedTab == 1 && uiState.historyItems.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("history_clear_all_button")
                        ) {
                            Text(stringResource(R.string.history_clear_all))
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${stringResource(R.string.favorites_tab_favorites)} (${uiState.favoriteItems.size})") },
                    modifier = Modifier.testTag("tab_favorites")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${stringResource(R.string.favorites_tab_history)} (${uiState.historyItems.size})") },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saved_search_input"),
                placeholder = { Text(stringResource(R.string.servers_search_placeholder)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_filters))
                        }
                    }
                },
                singleLine = true
            )

            if (selectedTab == 1) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)) {
                    items(listOf(TestStatus.REACHABLE, TestStatus.RESTRICTED, TestStatus.UNREACHABLE, TestStatus.TIMED_OUT)) { st ->
                        FilterChip(
                            selected = uiState.historyStatusFilter == st,
                            onClick = { onSelectStatusFilter(if (uiState.historyStatusFilter == st) null else st) },
                            label = { Text(st.displayLabel) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                    items(ServerProtocol.entries) { proto ->
                        FilterChip(
                            selected = uiState.historyProtocolFilter == proto,
                            onClick = { onSelectProtocolFilter(if (uiState.historyProtocolFilter == proto) null else proto) },
                            label = { Text(proto.displayLabel) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = BdixSpacing.lg, end = BdixSpacing.lg, bottom = BdixSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.md)
        ) {
            if (selectedTab == 0) {
                if (uiState.favoriteItems.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.BookmarkBorder,
                            title = stringResource(R.string.favorites_empty_title),
                            description = stringResource(R.string.favorites_empty_desc),
                            tag = "favorites_empty_state"
                        )
                    }
                } else {
                    items(
                        items = uiState.favoriteItems,
                        key = { it.endpoint.id }
                    ) { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ServerCard(
                                endpoint = item.endpoint,
                                result = item.latestResult,
                                isFavorite = true,
                                isPinned = item.favorite.isPinned,
                                onToggleFavorite = { onToggleFavorite(item.endpoint.id) },
                                onCardClick = { onOpenServerDetails(item.endpoint.id) },
                                onVisitWebsiteClick = {
                                    ExternalBrowserHelper.openUrl(context, item.endpoint.originalUrl)
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onSetPinned(item.endpoint.id, !item.favorite.isPinned) },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("pin_toggle_${item.endpoint.id}")
                                ) {
                                    Icon(
                                        imageVector = if (item.favorite.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                        contentDescription = null
                                    )
                                    Text(
                                        text = stringResource(
                                            if (item.favorite.isPinned) R.string.action_unpin else R.string.action_pin
                                        ),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (uiState.historyItems.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.history_empty_title),
                            description = stringResource(R.string.history_empty_desc),
                            tag = "history_empty_state"
                        )
                    }
                } else {
                    items(
                        items = uiState.historyItems,
                        key = { it.historyId }
                    ) { entry ->
                        HistoryRowCard(
                            entry = entry,
                            onOpenDetails = { onOpenServerDetails(entry.serverId) },
                            onDelete = { onDeleteHistoryEntry(entry.historyId) }
                        )
                    }
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.history_clear_confirm_title)) },
            text = { Text(stringResource(R.string.history_clear_confirm_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllHistory()
                        showClearHistoryDialog = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearHistoryDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun HistoryRowCard(
    entry: ServerTestResult,
    onOpenDetails: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onOpenDetails,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${entry.historyId}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(BdixSpacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.serverNameSnapshot.ifBlank { entry.serverId },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LtrContent {
                        Text(
                            text = entry.urlSnapshot,
                            style = MonoUrlStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("history_delete_${entry.historyId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.action_delete)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = entry.status)
                LtrContent {
                    Text(
                        text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US)
                            .format(Date(entry.testedAtEpochMs)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LtrContent {
                Text(
                    text = buildString {
                        append("${entry.scanMode.displayLabel} · ${entry.protocol.displayLabel}")
                        entry.primaryLatencyMs?.let { append(" · ${it} ms") }
                        entry.downloadMbps?.let { append(" · ${"%.2f".format(Locale.US, it)} Mbps") }
                        entry.networkProfileLabel?.let { append(" · [$it]") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    BdixTesterTheme {
        FavoritesScreen(
            uiState = FavoritesUiState(),
            onSearchQueryChange = {},
            onSelectStatusFilter = {},
            onSelectProtocolFilter = {},
            onToggleFavorite = {},
            onSetPinned = { _, _ -> },
            onOpenServerDetails = {},
            onDeleteHistoryEntry = {},
            onClearAllHistory = {},
            onExportFavorites = { "" },
            onExportHistory = { "" }
        )
    }
}
