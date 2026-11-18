package com.jubayerhossain.bdixtester.feature.servers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.export.ExportDataset
import com.jubayerhossain.bdixtester.core.export.ExportFormat
import com.jubayerhossain.bdixtester.core.model.CatalogSourceType
import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.core.model.ServerProtocol
import com.jubayerhossain.bdixtester.core.model.TestStatus
import com.jubayerhossain.bdixtester.core.util.ExternalBrowserHelper
import com.jubayerhossain.bdixtester.domain.model.ServerEndpoint
import com.jubayerhossain.bdixtester.ui.components.CustomServerDialog
import com.jubayerhossain.bdixtester.ui.components.EmptyStateView
import com.jubayerhossain.bdixtester.ui.components.ExportMenuButton
import com.jubayerhossain.bdixtester.ui.components.ServerCard
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme

@Composable
fun ServersScreen(
    uiState: ServersUiState,
    onSelectCategory: (ServerCategory) -> Unit,
    onSelectProtocol: (ServerProtocol?) -> Unit,
    onSelectStatus: (TestStatus?) -> Unit,
    onSelectSourceType: (CatalogSourceType?) -> Unit,
    onToggleFeaturedOnly: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (ServerSortOrder) -> Unit,
    onClearFilters: () -> Unit,
    onServerClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onTestSingleServer: (ServerEndpoint) -> Unit,
    onSaveCustomServer: (
        existingId: String?,
        name: String,
        rawUrl: String,
        category: ServerCategory,
        speedTestUrl: String?,
        onSuccess: () -> Unit
    ) -> Unit,
    onDeleteCustomServer: (String) -> Unit,
    onClearCustomServerError: () -> Unit,
    onExport: (ExportDataset, ExportFormat) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingEndpoint by remember { mutableStateOf<ServerEndpoint?>(null) }
    var deleteCandidate by remember { mutableStateOf<ServerEndpoint?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("servers_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEndpoint = null
                    onClearCustomServerError()
                    showAddEditDialog = true
                },
                modifier = Modifier.testTag("servers_add_custom_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add_server)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                        text = "${stringResource(R.string.nav_servers)} (${uiState.filteredServers.size})",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExportMenuButton(
                            dataset = if (uiState.selectedSourceType == CatalogSourceType.USER_CUSTOM) {
                                ExportDataset.CUSTOM_SERVERS
                            } else {
                                ExportDataset.CATALOG
                            },
                            onGeneratePayload = { format ->
                                val dataset = if (uiState.selectedSourceType == CatalogSourceType.USER_CUSTOM) {
                                    ExportDataset.CUSTOM_SERVERS
                                } else {
                                    ExportDataset.CATALOG
                                }
                                onExport(dataset, format)
                            },
                            buttonTestTag = "servers_export_button"
                        )

                        Box {
                            OutlinedButton(
                                onClick = { sortMenuExpanded = true },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("servers_sort_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null
                                )
                                Text(
                                    text = uiState.sortOrder.displayLabel,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                ServerSortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.displayLabel) },
                                        onClick = {
                                            onSortOrderChange(order)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("servers_search_input"),
                    placeholder = { Text(stringResource(R.string.servers_search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.action_clear_filters)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    items(ServerCategory.entries) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category.displayName) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("servers_category_chip_${category.name.lowercase()}")
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    items(CatalogSourceType.entries) { src ->
                        FilterChip(
                            selected = uiState.selectedSourceType == src,
                            onClick = {
                                onSelectSourceType(if (uiState.selectedSourceType == src) null else src)
                            },
                            label = { Text(src.displayLabel) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("servers_source_chip_${src.name.lowercase()}")
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.featuredOnly,
                            onClick = onToggleFeaturedOnly,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.servers_filter_featured)) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("servers_featured_filter")
                        )
                    }
                    items(ServerProtocol.entries) { protocol ->
                        FilterChip(
                            selected = uiState.selectedProtocol == protocol,
                            onClick = {
                                onSelectProtocol(
                                    if (uiState.selectedProtocol == protocol) null else protocol
                                )
                            },
                            label = { Text(protocol.displayLabel) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("servers_protocol_chip_${protocol.name.lowercase()}")
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    val statusChoices = listOf(
                        TestStatus.REACHABLE,
                        TestStatus.RESTRICTED,
                        TestStatus.UNREACHABLE,
                        TestStatus.TIMED_OUT,
                        TestStatus.NOT_TESTED
                    )
                    items(statusChoices) { status ->
                        FilterChip(
                            selected = uiState.selectedStatus == status,
                            onClick = {
                                onSelectStatus(if (uiState.selectedStatus == status) null else status)
                            },
                            label = { Text(status.displayLabel) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("servers_status_chip_${status.name.lowercase()}")
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(
                    start = BdixSpacing.lg,
                    end = BdixSpacing.lg,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(BdixSpacing.md)
            ) {
                if (uiState.filteredServers.isEmpty()) {
                    item {
                        val isCatalogEmpty = uiState.totalCatalogCount == 0
                        EmptyStateView(
                            icon = Icons.Default.Dns,
                            title = stringResource(R.string.servers_empty_title),
                            description = if (isCatalogEmpty) {
                                stringResource(R.string.servers_empty_catalog_desc)
                            } else {
                                stringResource(R.string.servers_empty_filter_desc)
                            },
                            actionLabel = if (!isCatalogEmpty) {
                                stringResource(R.string.action_clear_filters)
                            } else {
                                null
                            },
                            onActionClick = if (!isCatalogEmpty) onClearFilters else null,
                            tag = "servers_empty_state"
                        )
                    }
                } else {
                    items(
                        items = uiState.filteredServers,
                        key = { it.id }
                    ) { endpoint ->
                        val fav = uiState.favorites[endpoint.id]
                        val isCustom = endpoint.sourceType == CatalogSourceType.USER_CUSTOM
                        ServerCard(
                            endpoint = endpoint,
                            result = uiState.latestResults[endpoint.id],
                            isFavorite = fav != null,
                            isPinned = fav?.isPinned == true,
                            onToggleFavorite = { onToggleFavorite(endpoint.id) },
                            onTestClick = { onTestSingleServer(endpoint) },
                            onEditCustomClick = if (isCustom) {
                                {
                                    editingEndpoint = endpoint
                                    onClearCustomServerError()
                                    showAddEditDialog = true
                                }
                            } else {
                                null
                            },
                            onDeleteCustomClick = if (isCustom) {
                                { deleteCandidate = endpoint }
                            } else {
                                null
                            },
                            onCardClick = { onServerClick(endpoint.id) },
                            onVisitWebsiteClick = {
                                ExternalBrowserHelper.openUrl(context, endpoint.originalUrl)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        CustomServerDialog(
            initialEndpoint = editingEndpoint,
            errorMessage = uiState.customServerError,
            onDismiss = {
                showAddEditDialog = false
                editingEndpoint = null
                onClearCustomServerError()
            },
            onSave = { existingId, name, rawUrl, category, speedUrl ->
                onSaveCustomServer(existingId, name, rawUrl, category, speedUrl) {
                    showAddEditDialog = false
                    editingEndpoint = null
                }
            }
        )
    }

    deleteCandidate?.let { candidate ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(stringResource(R.string.servers_dialog_delete_title)) },
            text = { Text(stringResource(R.string.servers_dialog_delete_body, candidate.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomServer(candidate.id)
                        deleteCandidate = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteCandidate = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ServersScreenEmptyPreview() {
    BdixTesterTheme {
        ServersScreen(
            uiState = ServersUiState(),
            onSelectCategory = {},
            onSelectProtocol = {},
            onSelectStatus = {},
            onSelectSourceType = {},
            onToggleFeaturedOnly = {},
            onSearchQueryChange = {},
            onSortOrderChange = {},
            onClearFilters = {},
            onServerClick = {},
            onToggleFavorite = {},
            onTestSingleServer = {},
            onSaveCustomServer = { _, _, _, _, _, _ -> },
            onDeleteCustomServer = {},
            onClearCustomServerError = {},
            onExport = { _, _ -> "" }
        )
    }
}
