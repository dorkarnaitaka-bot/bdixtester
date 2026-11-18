package com.jubayerhossain.bdixtester.feature.servers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.util.ServerDisplayClassifier
import com.jubayerhossain.bdixtester.data.catalog.BdOttPlatforms
import com.jubayerhossain.bdixtester.data.catalog.OttGenre
import com.jubayerhossain.bdixtester.data.catalog.OttPlatform
import com.jubayerhossain.bdixtester.data.catalog.OttRegion
import com.jubayerhossain.bdixtester.data.catalog.OttRegionFilter
import com.jubayerhossain.bdixtester.domain.model.ServerEndpoint
import com.jubayerhossain.bdixtester.ui.components.EmptyStateView
import com.jubayerhossain.bdixtester.ui.components.LtrContent
import com.jubayerhossain.bdixtester.ui.theme.BdixBorder
import com.jubayerhossain.bdixtester.ui.theme.BdixIconSize
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme
import com.jubayerhossain.bdixtester.ui.theme.LocalBdixSemanticColors
import com.jubayerhossain.bdixtester.ui.theme.MonoUrlStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    uiState: ServersUiState,
    onSelectStatusTab: (ServerStatusTab) -> Unit,
    onSelectBottomTab: (ServerBottomTab) -> Unit,
    onSelectOttRegion: (OttRegionFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddCustomServerClick: () -> Unit,
    onSyncCatalogClick: () -> Unit,
    onStartTestClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val semantic = LocalBdixSemanticColors.current
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val isOttMode = uiState.selectedBottomTab == ServerBottomTab.OTT

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("servers_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.servers_title),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    searchVisible = !searchVisible
                                    if (!searchVisible) onSearchQueryChange("")
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("servers_search_toggle")
                            ) {
                                Icon(
                                    imageVector = if (searchVisible) Icons.Rounded.Clear else Icons.Rounded.Search,
                                    contentDescription = stringResource(R.string.servers_bottom_search),
                                    tint = if (searchVisible) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            IconButton(
                                onClick = onSyncCatalogClick,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("servers_sync_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sync,
                                    contentDescription = stringResource(R.string.servers_sync_catalog_a11y)
                                )
                            }
                            IconButton(
                                onClick = onAddCustomServerClick,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("servers_add_custom_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = stringResource(R.string.action_add_server)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // 3 Status Tabs with live tabular count badges (shown for BDIX server categories)
                    AnimatedVisibility(
                        visible = !isOttMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = BdixSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusHeaderTab(
                                label = stringResource(R.string.servers_tab_working),
                                count = uiState.workingCount,
                                icon = Icons.Rounded.CheckCircle,
                                iconTint = semantic.reachable,
                                selected = uiState.selectedStatusTab == ServerStatusTab.WORKING,
                                onClick = { onSelectStatusTab(ServerStatusTab.WORKING) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("servers_tab_working")
                            )

                            StatusHeaderTab(
                                label = stringResource(R.string.servers_tab_not_tested),
                                count = uiState.notTestedCount,
                                icon = Icons.Rounded.Warning,
                                iconTint = semantic.restricted,
                                selected = uiState.selectedStatusTab == ServerStatusTab.NOT_TESTED,
                                onClick = { onSelectStatusTab(ServerStatusTab.NOT_TESTED) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("servers_tab_not_tested")
                            )

                            StatusHeaderTab(
                                label = stringResource(R.string.servers_tab_not_working),
                                count = uiState.notWorkingCount,
                                icon = Icons.Rounded.Cancel,
                                iconTint = semantic.unreachable,
                                selected = uiState.selectedStatusTab == ServerStatusTab.NOT_WORKING,
                                onClick = { onSelectStatusTab(ServerStatusTab.NOT_WORKING) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("servers_tab_not_working")
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = BdixBorder.hairline,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = searchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = BdixSpacing.md, vertical = BdixSpacing.sm)
                        .testTag("servers_search_input"),
                    placeholder = { Text(stringResource(R.string.servers_search_placeholder)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = stringResource(R.string.action_clear_filters)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Single-row Category Selector (All, OTT, TV, FTP, Movie)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .testTag("servers_category_bar"),
                contentPadding = PaddingValues(horizontal = BdixSpacing.md, vertical = BdixSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
            ) {
                val categories = listOf(
                    Triple(ServerBottomTab.ALL, R.string.servers_bottom_all, Icons.AutoMirrored.Rounded.FormatListBulleted),
                    Triple(ServerBottomTab.OTT, R.string.servers_bottom_ott, Icons.Rounded.SmartDisplay),
                    Triple(ServerBottomTab.TV, R.string.servers_bottom_tv, Icons.Rounded.LiveTv),
                    Triple(ServerBottomTab.FTP, R.string.servers_bottom_ftp, Icons.Rounded.FolderShared),
                    Triple(ServerBottomTab.MOVIE, R.string.servers_bottom_movie, Icons.Rounded.Movie)
                )
                items(categories) { (tab, labelRes, icon) ->
                    val selected = uiState.selectedBottomTab == tab
                    FilterChip(
                        selected = selected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectBottomTab(tab)
                        },
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(BdixIconSize.sm)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("servers_category_${tab.name.lowercase()}")
                    )
                }
            }

            if (isOttMode) {
                // Compact Region Filter Pills for Curated BD OTT Directory (26 verified platforms)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .testTag("ott_region_filter_bar"),
                    contentPadding = PaddingValues(start = BdixSpacing.md, end = BdixSpacing.md, bottom = BdixSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    val regionFilters = listOf(
                        OttRegionFilter.ALL to R.string.ott_filter_all to BdOttPlatforms.all.size,
                        OttRegionFilter.BANGLADESH to R.string.ott_filter_bd to BdOttPlatforms.bangladeshCount,
                        OttRegionFilter.GLOBAL_IN_BD to R.string.ott_filter_global to BdOttPlatforms.globalCount
                    )
                    items(regionFilters) { (filterAndRes, count) ->
                        val (filter, labelRes) = filterAndRes
                        val selected = uiState.selectedOttRegion == filter
                        FilterChip(
                            selected = selected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectOttRegion(filter)
                            },
                            label = {
                                Text(
                                    text = stringResource(labelRes, count),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("ott_region_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useTwoColumns = maxWidth >= 700.dp

                if (isOttMode) {
                    OttDirectoryList(
                        platforms = uiState.filteredOttPlatforms,
                        useTwoColumns = useTwoColumns,
                        hasQueryOrFilter = uiState.searchQuery.isNotBlank() || uiState.selectedOttRegion != OttRegionFilter.ALL,
                        onClearFilters = {
                            searchVisible = false
                            onSearchQueryChange("")
                            onSelectOttRegion(OttRegionFilter.ALL)
                        },
                        onOpenUrl = onOpenUrl
                    )
                } else if (uiState.filteredServers.isEmpty()) {
                    val hasActiveFilters = uiState.searchQuery.isNotBlank() || uiState.selectedBottomTab != ServerBottomTab.ALL
                    val isWorkingEmpty = !hasActiveFilters && uiState.selectedStatusTab == ServerStatusTab.WORKING
                    val emptyDesc = when {
                        hasActiveFilters -> stringResource(R.string.servers_empty_filter_desc)
                        uiState.selectedStatusTab == ServerStatusTab.WORKING ->
                            stringResource(R.string.servers_empty_working_desc)
                        uiState.selectedStatusTab == ServerStatusTab.NOT_WORKING ->
                            stringResource(R.string.servers_empty_not_working_desc)
                        else -> stringResource(R.string.servers_empty_catalog_desc)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = BdixSpacing.md, vertical = BdixSpacing.md),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        EmptyStateView(
                            icon = Icons.Rounded.Dns,
                            title = stringResource(R.string.servers_empty_title),
                            description = emptyDesc,
                            actionLabel = when {
                                hasActiveFilters -> stringResource(R.string.action_clear_filters)
                                isWorkingEmpty -> stringResource(R.string.home_action_start_test)
                                else -> null
                            },
                            onActionClick = when {
                                hasActiveFilters -> {
                                    {
                                        searchVisible = false
                                        onClearFilters()
                                    }
                                }
                                isWorkingEmpty -> onStartTestClick
                                else -> null
                            },
                            modifier = Modifier.widthIn(max = 560.dp),
                            tag = "servers_empty_state"
                        )
                    }
                } else if (useTwoColumns) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = BdixSpacing.md,
                            end = BdixSpacing.md,
                            top = BdixSpacing.xs,
                            bottom = BdixSpacing.xl
                        ),
                        horizontalArrangement = Arrangement.spacedBy(BdixSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(BdixSpacing.md)
                    ) {
                        items(
                            items = uiState.filteredServers,
                            key = { it.id },
                            span = { GridItemSpan(1) }
                        ) { endpoint ->
                            val result = uiState.latestResults[endpoint.id]
                            val statusTab = ServersViewModel.statusTabFor(result?.status)
                            ServerListCard(
                                endpoint = endpoint,
                                statusTab = statusTab,
                                latencyMs = result?.primaryLatencyMs,
                                isFavorite = uiState.favorites.containsKey(endpoint.id),
                                onToggleFavorite = { onToggleFavorite(endpoint.id) },
                                onUrlClick = { onOpenUrl(endpoint.originalUrl) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp),
                        contentPadding = PaddingValues(
                            start = BdixSpacing.md,
                            end = BdixSpacing.md,
                            top = BdixSpacing.xs,
                            bottom = BdixSpacing.xl
                        ),
                        verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                    ) {
                        items(
                            items = uiState.filteredServers,
                            key = { it.id }
                        ) { endpoint ->
                            val result = uiState.latestResults[endpoint.id]
                            val statusTab = ServersViewModel.statusTabFor(result?.status)
                            ServerListCard(
                                endpoint = endpoint,
                                statusTab = statusTab,
                                latencyMs = result?.primaryLatencyMs,
                                isFavorite = uiState.favorites.containsKey(endpoint.id),
                                onToggleFavorite = { onToggleFavorite(endpoint.id) },
                                onUrlClick = { onOpenUrl(endpoint.originalUrl) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OttDirectoryList(
    platforms: List<OttPlatform>,
    useTwoColumns: Boolean,
    hasQueryOrFilter: Boolean,
    onClearFilters: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    if (platforms.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = BdixSpacing.md, vertical = BdixSpacing.md),
            contentAlignment = Alignment.TopCenter
        ) {
            EmptyStateView(
                icon = Icons.Rounded.SmartDisplay,
                title = stringResource(R.string.ott_empty_title),
                description = stringResource(R.string.ott_empty_desc),
                actionLabel = if (hasQueryOrFilter) stringResource(R.string.action_clear_filters) else null,
                onActionClick = if (hasQueryOrFilter) onClearFilters else null,
                modifier = Modifier.widthIn(max = 560.dp),
                tag = "ott_empty_state"
            )
        }
        return
    }

    if (useTwoColumns) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .testTag("ott_directory_list"),
            contentPadding = PaddingValues(
                start = BdixSpacing.md,
                end = BdixSpacing.md,
                top = BdixSpacing.xs,
                bottom = BdixSpacing.xl
            ),
            horizontalArrangement = Arrangement.spacedBy(BdixSpacing.md),
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.md)
        ) {
            items(
                items = platforms,
                key = { it.id },
                span = { GridItemSpan(1) }
            ) { platform ->
                OttPlatformCard(
                    platform = platform,
                    onOpenUrl = { onOpenUrl(platform.url) }
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("ott_directory_list"),
            contentPadding = PaddingValues(
                start = BdixSpacing.md,
                end = BdixSpacing.md,
                top = BdixSpacing.xs,
                bottom = BdixSpacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
        ) {
            items(
                items = platforms,
                key = { it.id }
            ) { platform ->
                OttPlatformCard(
                    platform = platform,
                    onOpenUrl = { onOpenUrl(platform.url) }
                )
            }
        }
    }
}

@Composable
private fun OttPlatformCard(
    platform: OttPlatform,
    onOpenUrl: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val urlA11y = stringResource(R.string.servers_open_url_a11y, platform.name)
    val genreIcon = when (platform.genre) {
        OttGenre.ORIGINALS_MOVIES -> Icons.Rounded.SmartDisplay
        OttGenre.LIVE_TV_AGGREGATOR -> Icons.Rounded.LiveTv
        OttGenre.LIVE_SPORTS -> Icons.Rounded.SportsSoccer
        OttGenre.KIDS_FAMILY -> Icons.Rounded.ChildCare
        OttGenre.ANIME_DRAMA -> Icons.Rounded.AutoAwesome
    }
    val isBangladesh = platform.region == OttRegion.BANGLADESH

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = urlA11y,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenUrl()
                }
            )
            .semantics { contentDescription = urlA11y }
            .testTag("ott_card_${platform.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(BdixBorder.hairline, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BdixSpacing.lg, vertical = BdixSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(BdixSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = genreIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                    ) {
                        Text(
                            text = platform.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            color = if (isBangladesh) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (isBangladesh) R.string.ott_badge_bd else R.string.ott_badge_global
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isBangladesh) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = platform.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                thickness = BdixBorder.hairline,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = BdixSpacing.lg, vertical = BdixSpacing.sm)
                    .testTag("ott_url_${platform.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LtrContent {
                    Text(
                        text = platform.url,
                        style = MonoUrlStyle,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = BdixSpacing.sm)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(BdixIconSize.sm)
                )
            }
        }
    }
}

@Composable
private fun StatusHeaderTab(
    label: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(role = Role.Tab, onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(BdixIconSize.xs)
            )
            LtrContent {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .height(2.5.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = MaterialTheme.shapes.extraSmall
                )
        )
    }
}

@Composable
private fun ServerListCard(
    endpoint: ServerEndpoint,
    statusTab: ServerStatusTab,
    latencyMs: Long?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onUrlClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val semantic = LocalBdixSemanticColors.current
    val (statusIcon, statusTint, statusDesc) = when (statusTab) {
        ServerStatusTab.WORKING -> Triple(
            Icons.Rounded.CheckCircle,
            semantic.reachable,
            stringResource(R.string.servers_tab_working)
        )
        ServerStatusTab.NOT_TESTED -> Triple(
            Icons.Rounded.Warning,
            semantic.restricted,
            stringResource(R.string.servers_tab_not_tested)
        )
        ServerStatusTab.NOT_WORKING -> Triple(
            Icons.Rounded.Cancel,
            semantic.unreachable,
            stringResource(R.string.servers_tab_not_working)
        )
    }

    val urlA11y = stringResource(R.string.servers_open_url_a11y, endpoint.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${endpoint.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(BdixBorder.hairline, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tapping anywhere on the card launches the external browser directly (no metadata page)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClickLabel = urlA11y,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUrlClick()
                        }
                    )
                    .padding(
                        start = BdixSpacing.lg,
                        end = BdixSpacing.sm,
                        top = BdixSpacing.md,
                        bottom = BdixSpacing.sm
                    ),
                horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = endpoint.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                    ) {
                        Text(
                            text = ServerDisplayClassifier.displayLabel(endpoint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (latencyMs != null) {
                            Surface(
                                color = if (statusTab == ServerStatusTab.WORKING) {
                                    semantic.reachableContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                LtrContent {
                                    Text(
                                        text = stringResource(R.string.servers_latency_chip, latencyMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (statusTab == ServerStatusTab.WORKING) {
                                            semantic.reachable
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleFavorite()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("server_bookmark_${endpoint.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.action_remove_favorite else R.string.action_add_favorite
                        ),
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(BdixIconSize.md)
                    )
                }

                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusDesc,
                    tint = statusTint,
                    modifier = Modifier
                        .padding(end = BdixSpacing.sm)
                        .size(BdixIconSize.lg)
                )
            }

            HorizontalDivider(
                thickness = BdixBorder.hairline,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Full-width >= 48dp External Browser URL Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = urlA11y,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUrlClick()
                        }
                    )
                    .semantics { contentDescription = urlA11y }
                    .padding(horizontal = BdixSpacing.lg, vertical = BdixSpacing.sm)
                    .testTag("server_url_link_${endpoint.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LtrContent {
                    Text(
                        text = endpoint.originalUrl,
                        style = MonoUrlStyle,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = BdixSpacing.sm)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(BdixIconSize.sm)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ServersScreenPreview() {
    BdixTesterTheme {
        ServersScreen(
            uiState = ServersUiState(),
            onSelectStatusTab = {},
            onSelectBottomTab = {},
            onSelectOttRegion = {},
            onSearchQueryChange = {},
            onClearFilters = {},
            onToggleFavorite = {},
            onAddCustomServerClick = {},
            onSyncCatalogClick = {},
            onStartTestClick = {},
            onOpenUrl = {}
        )
    }
}
