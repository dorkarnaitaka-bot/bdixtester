package com.jubayerhossain.bdixtester.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.di.AppContainer
import com.jubayerhossain.bdixtester.core.util.BrowserLaunchResult
import com.jubayerhossain.bdixtester.core.util.ExternalBrowserHelper
import com.jubayerhossain.bdixtester.feature.dashboard.DashboardScreen
import com.jubayerhossain.bdixtester.feature.dashboard.DashboardViewModel
import com.jubayerhossain.bdixtester.feature.favorites.FavoritesViewModel
import com.jubayerhossain.bdixtester.feature.favorites.HistoryScreen
import com.jubayerhossain.bdixtester.feature.favorites.SavedServersScreen
import com.jubayerhossain.bdixtester.feature.scan.ScanViewModel
import com.jubayerhossain.bdixtester.feature.servers.ServersScreen
import com.jubayerhossain.bdixtester.feature.servers.ServersViewModel
import com.jubayerhossain.bdixtester.feature.settings.SettingsScreen
import com.jubayerhossain.bdixtester.feature.settings.SettingsViewModel
import com.jubayerhossain.bdixtester.ui.components.BdixMotionSpecs
import com.jubayerhossain.bdixtester.ui.components.CustomServerDialog
import com.jubayerhossain.bdixtester.ui.components.LocalReducedMotion
import com.jubayerhossain.bdixtester.ui.theme.BdixIconSize
import kotlinx.coroutines.launch

@Composable
fun BdixTesterAppRoot(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelRoutes = remember { TopLevelDestination.entries.map { it.route }.toSet() }
    val isTopLevelDestination = currentRoute == null || currentRoute in topLevelRoutes

    var showAddServerDialog by remember { mutableStateOf(false) }

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(
            catalogRepository = appContainer.catalogRepository,
            connectivityObserver = appContainer.connectivityObserver,
            settingsRepository = appContainer.settingsRepository
        )
    )
    val serversViewModel: ServersViewModel = viewModel(
        factory = ServersViewModel.factory(
            catalogRepository = appContainer.catalogRepository,
            scanCoordinator = appContainer.scanCoordinator,
            settingsRepository = appContainer.settingsRepository
        )
    )
    val scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModel.factory(
            catalogRepository = appContainer.catalogRepository,
            scanCoordinator = appContainer.scanCoordinator,
            settingsRepository = appContainer.settingsRepository
        )
    )
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.factory(appContainer.catalogRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(appContainer)
    )

    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val serversUiState by serversViewModel.uiState.collectAsStateWithLifecycle()
    val favoritesUiState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    fun navigateToTopLevel(destinationRoute: String) {
        if (currentRoute == destinationRoute) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        navController.navigate(destinationRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openExternalUrl(rawUrl: String) {
        val result = ExternalBrowserHelper.launchUrl(context, rawUrl)
        if (result !is BrowserLaunchResult.Launched) {
            val errorRes = ExternalBrowserHelper.errorMessageRes(result) ?: R.string.details_open_browser_failed
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(errorRes),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    fun triggerCatalogSyncFromToolbar() {
        val remoteUrl = settingsUiState.settings.remoteCatalogUrl
        if (remoteUrl.isNotBlank()) {
            settingsViewModel.syncCatalogNow(remoteUrl)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.drawer_update_triggered),
                    duration = SnackbarDuration.Short
                )
            }
        } else {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.drawer_update_configure_hint),
                    actionLabel = context.getString(R.string.nav_settings),
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    navigateToTopLevel(BdixRoutes.SETTINGS)
                }
            }
        }
    }

    val reducedMotion = LocalReducedMotion.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        val navHostContent: @Composable (Modifier) -> Unit = { contentModifier ->
            NavHost(
                navController = navController,
                startDestination = BdixRoutes.HOME,
                enterTransition = {
                    if (reducedMotion) {
                        EnterTransition.None
                    } else {
                        fadeIn(animationSpec = tween(220)) +
                            slideInVertically(animationSpec = tween(220)) { it / 32 }
                    }
                },
                exitTransition = {
                    if (reducedMotion) ExitTransition.None else fadeOut(animationSpec = tween(160))
                },
                popEnterTransition = {
                    if (reducedMotion) {
                        EnterTransition.None
                    } else {
                        fadeIn(animationSpec = tween(220)) +
                            slideInVertically(animationSpec = tween(220)) { it / 32 }
                    }
                },
                popExitTransition = {
                    if (reducedMotion) ExitTransition.None else fadeOut(animationSpec = tween(160))
                },
                modifier = contentModifier.fillMaxSize()
            ) {
                composable(BdixRoutes.HOME) {
                    DashboardScreen(
                        uiState = dashboardUiState,
                        onViewServers = {
                            serversViewModel.prepareForViewing()
                            navigateToTopLevel(BdixRoutes.SERVERS)
                        },
                        onStartScan = { mode ->
                            scanViewModel.startScan(overrideMode = mode)
                        },
                        onStopScan = {
                            scanViewModel.stopScan()
                        }
                    )
                }

                composable(BdixRoutes.SERVERS) {
                    ServersScreen(
                        uiState = serversUiState,
                        onSelectStatusTab = serversViewModel::selectStatusTab,
                        onSelectBottomTab = serversViewModel::selectBottomTab,
                        onSelectOttRegion = serversViewModel::selectOttRegion,
                        onSearchQueryChange = serversViewModel::updateSearchQuery,
                        onClearFilters = serversViewModel::clearFilters,
                        onToggleFavorite = serversViewModel::toggleFavorite,
                        onAddCustomServerClick = {
                            serversViewModel.clearCustomServerError()
                            showAddServerDialog = true
                        },
                        onSyncCatalogClick = ::triggerCatalogSyncFromToolbar,
                        onStartTestClick = {
                            navigateToTopLevel(BdixRoutes.HOME)
                            scanViewModel.startScan(overrideMode = dashboardUiState.selectedScanMode)
                        },
                        onOpenUrl = ::openExternalUrl
                    )
                }

                composable(BdixRoutes.FAVORITES) {
                    SavedServersScreen(
                        uiState = favoritesUiState,
                        onSearchQueryChange = favoritesViewModel::updateSearchQuery,
                        onToggleFavorite = favoritesViewModel::toggleFavorite,
                        onSetPinned = favoritesViewModel::setPinned,
                        onOpenUrl = ::openExternalUrl,
                        onExportFavorites = favoritesViewModel::exportFavorites
                    )
                }

                composable(BdixRoutes.HISTORY) {
                    HistoryScreen(
                        uiState = favoritesUiState,
                        onSearchQueryChange = favoritesViewModel::updateSearchQuery,
                        onSelectStatusFilter = favoritesViewModel::selectHistoryStatusFilter,
                        onSelectProtocolFilter = favoritesViewModel::selectHistoryProtocolFilter,
                        onOpenUrl = ::openExternalUrl,
                        onDeleteHistoryEntry = favoritesViewModel::deleteHistoryEntry,
                        onClearAllHistory = favoritesViewModel::clearAllHistory,
                        onExportHistory = favoritesViewModel::exportHistory
                    )
                }

                composable(BdixRoutes.SETTINGS) {
                    SettingsScreen(
                        uiState = settingsUiState,
                        onSelectTheme = settingsViewModel::selectTheme,
                        onSelectLanguage = settingsViewModel::selectLanguage,
                        onReduceAnimationsChange = settingsViewModel::setReduceAnimations,
                        onSelectDefaultScanMode = settingsViewModel::selectDefaultScanMode,
                        onMaxConcurrencyChange = settingsViewModel::setMaxConcurrency,
                        onSpeedTestMaxBytesChange = settingsViewModel::setSpeedTestMaxBytes,
                        onManualNetworkLabelSave = settingsViewModel::setManualNetworkLabel,
                        onRemoteCatalogUrlSave = settingsViewModel::setRemoteCatalogUrl,
                        onAutoUpdatesChange = settingsViewModel::setAutoCatalogUpdates,
                        onWifiOnlyChange = settingsViewModel::setWifiOnlyCatalogUpdates,
                        onCrossOriginChange = settingsViewModel::setAllowCrossOriginPayload,
                        onSyncNowClick = settingsViewModel::syncCatalogNow,
                        onHistoryRetentionChange = settingsViewModel::setHistoryRetentionDays,
                        onClearHistory = settingsViewModel::clearAllHistory,
                        onClearFavorites = settingsViewModel::clearAllFavorites,
                        onDeleteCustomServers = settingsViewModel::deleteAllCustomServers,
                        onResetAllData = settingsViewModel::resetAllLocalData,
                        onUpdateServerListClick = ::triggerCatalogSyncFromToolbar,
                        onAddServerRequestClick = {
                            serversViewModel.clearCustomServerError()
                            showAddServerDialog = true
                        },
                        onOpenExternalUrl = ::openExternalUrl
                    )
                }
            }
        }

        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (isTopLevelDestination) {
                    BdixNavigationRail(
                        currentRoute = currentRoute,
                        onSelectDestination = ::navigateToTopLevel
                    )
                }
                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .imePadding(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        navHostContent(Modifier)
                    }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    AnimatedVisibility(
                        visible = isTopLevelDestination,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        BdixBottomNavigationBar(
                            currentRoute = currentRoute,
                            onSelectDestination = ::navigateToTopLevel
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    navHostContent(Modifier)
                }
            }
        }
    }

    if (showAddServerDialog) {
        CustomServerDialog(
            initialEndpoint = null,
            errorMessage = serversUiState.customServerError,
            onDismiss = {
                showAddServerDialog = false
                serversViewModel.clearCustomServerError()
            },
            onSave = { existingId, name, rawUrl, category, speedUrl ->
                serversViewModel.saveCustomServer(
                    existingId = existingId,
                    name = name,
                    rawUrl = rawUrl,
                    category = category,
                    speedTestUrl = speedUrl
                ) {
                    showAddServerDialog = false
                }
            }
        )
    }
}

@Composable
private fun BdixBottomNavigationBar(
    currentRoute: String?,
    onSelectDestination: (String) -> Unit
) {
    val activeRoute = currentRoute ?: BdixRoutes.HOME
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("main_bottom_navigation_bar")
    ) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val reducedMotion = LocalReducedMotion.current
        TopLevelDestination.entries.forEach { destination ->
            val selected = activeRoute == destination.route
            val label = stringResource(destination.labelRes)
            val iconScale by animateFloatAsState(
                targetValue = if (selected && !reducedMotion) 1.12f else 1f,
                animationSpec = if (reducedMotion) tween(0) else BdixMotionSpecs.bouncySpring,
                label = "nav_bar_icon_scale_${destination.route}"
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectDestination(destination.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = label,
                        modifier = Modifier
                            .size(BdixIconSize.lg)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                },
                alwaysShowLabel = true,
                colors = colors,
                modifier = Modifier.testTag(destination.testTag)
            )
        }
    }
}

@Composable
private fun BdixNavigationRail(
    currentRoute: String?,
    onSelectDestination: (String) -> Unit
) {
    val activeRoute = currentRoute ?: BdixRoutes.HOME
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .testTag("main_navigation_rail")
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        val colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val reducedMotion = LocalReducedMotion.current
        TopLevelDestination.entries.forEach { destination ->
            val selected = activeRoute == destination.route
            val label = stringResource(destination.labelRes)
            val iconScale by animateFloatAsState(
                targetValue = if (selected && !reducedMotion) 1.12f else 1f,
                animationSpec = if (reducedMotion) tween(0) else BdixMotionSpecs.bouncySpring,
                label = "nav_rail_icon_scale_${destination.route}"
            )
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectDestination(destination.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = label,
                        modifier = Modifier
                            .size(BdixIconSize.lg)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = colors,
                modifier = Modifier.testTag(destination.testTag)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
