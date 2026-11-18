package com.jubayerhossain.bdixtester.navigation

import android.net.Uri
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jubayerhossain.bdixtester.core.di.AppContainer
import com.jubayerhossain.bdixtester.feature.dashboard.DashboardScreen
import com.jubayerhossain.bdixtester.feature.dashboard.DashboardViewModel
import com.jubayerhossain.bdixtester.feature.favorites.FavoritesScreen
import com.jubayerhossain.bdixtester.feature.favorites.FavoritesViewModel
import com.jubayerhossain.bdixtester.feature.scan.ScanScreen
import com.jubayerhossain.bdixtester.feature.scan.ScanViewModel
import com.jubayerhossain.bdixtester.feature.servers.ServerDetailsScreen
import com.jubayerhossain.bdixtester.feature.servers.ServerDetailsViewModel
import com.jubayerhossain.bdixtester.feature.servers.ServersScreen
import com.jubayerhossain.bdixtester.feature.servers.ServersViewModel
import com.jubayerhossain.bdixtester.feature.settings.SettingsScreen
import com.jubayerhossain.bdixtester.feature.settings.SettingsViewModel

@Composable
fun BdixTesterAppRoot(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showTopLevelChrome = TopLevelDestination.entries.any { dest ->
        currentDestination?.hierarchy?.any { it.route == dest.route } == true
    }

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

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showTopLevelChrome && !useNavigationRail) {
                    NavigationBar(modifier = Modifier.testTag("bottom_navigation_bar")) {
                        TopLevelDestination.entries.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route
                            } == true
                            val label = stringResource(dest.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelChrome && useNavigationRail) {
                    NavigationRail(modifier = Modifier.testTag("side_navigation_rail")) {
                        TopLevelDestination.entries.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route
                            } == true
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = BdixRoutes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(BdixRoutes.HOME) {
                        val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                        DashboardScreen(
                            uiState = uiState,
                            onOpenScan = { mode ->
                                scanViewModel.startScan(overrideMode = mode)
                                navigateTopLevel(BdixRoutes.SCAN)
                            },
                            onOpenCategory = { category ->
                                serversViewModel.selectCategory(category)
                                navigateTopLevel(BdixRoutes.SERVERS)
                            },
                            onOpenServerDetails = { serverId ->
                                navController.navigate(BdixRoutes.serverDetailsRoute(serverId))
                            },
                            onToggleFavorite = dashboardViewModel::toggleFavorite
                        )
                    }

                    composable(BdixRoutes.SERVERS) {
                        val uiState by serversViewModel.uiState.collectAsStateWithLifecycle()
                        ServersScreen(
                            uiState = uiState,
                            onSelectCategory = serversViewModel::selectCategory,
                            onSelectProtocol = serversViewModel::selectProtocol,
                            onSelectStatus = serversViewModel::selectStatus,
                            onSelectSourceType = serversViewModel::selectSourceType,
                            onToggleFeaturedOnly = serversViewModel::toggleFeaturedOnly,
                            onSearchQueryChange = serversViewModel::updateSearchQuery,
                            onSortOrderChange = serversViewModel::updateSortOrder,
                            onClearFilters = serversViewModel::clearFilters,
                            onServerClick = { serverId ->
                                navController.navigate(BdixRoutes.serverDetailsRoute(serverId))
                            },
                            onToggleFavorite = serversViewModel::toggleFavorite,
                            onTestSingleServer = serversViewModel::testSingleServer,
                            onSaveCustomServer = serversViewModel::saveCustomServer,
                            onDeleteCustomServer = serversViewModel::deleteCustomServer,
                            onClearCustomServerError = serversViewModel::clearCustomServerError,
                            onExport = serversViewModel::buildExportContent
                        )
                    }

                    composable(BdixRoutes.SCAN) {
                        val uiState by scanViewModel.uiState.collectAsStateWithLifecycle()
                        ScanScreen(
                            uiState = uiState,
                            onSelectMode = scanViewModel::selectScanMode,
                            onSelectScope = scanViewModel::selectScanScope,
                            onSelectScopeCategory = scanViewModel::selectScopeCategory,
                            onStartScan = { scanViewModel.startScan() },
                            onStopScan = scanViewModel::stopScan,
                            onExportSummary = scanViewModel::buildScanSummaryExport
                        )
                    }

                    composable(BdixRoutes.FAVORITES) {
                        val uiState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
                        FavoritesScreen(
                            uiState = uiState,
                            onSearchQueryChange = favoritesViewModel::updateSearchQuery,
                            onSelectStatusFilter = favoritesViewModel::selectHistoryStatusFilter,
                            onSelectProtocolFilter = favoritesViewModel::selectHistoryProtocolFilter,
                            onToggleFavorite = favoritesViewModel::toggleFavorite,
                            onSetPinned = favoritesViewModel::setPinned,
                            onOpenServerDetails = { serverId ->
                                navController.navigate(BdixRoutes.serverDetailsRoute(serverId))
                            },
                            onDeleteHistoryEntry = favoritesViewModel::deleteHistoryEntry,
                            onClearAllHistory = favoritesViewModel::clearAllHistory,
                            onExportFavorites = favoritesViewModel::exportFavorites,
                            onExportHistory = favoritesViewModel::exportHistory
                        )
                    }

                    composable(BdixRoutes.SETTINGS) {
                        val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                        SettingsScreen(
                            uiState = uiState,
                            onSelectTheme = settingsViewModel::selectTheme,
                            onSelectLanguage = settingsViewModel::selectLanguage,
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
                            onResetAllData = settingsViewModel::resetAllLocalData
                        )
                    }

                    composable(
                        route = BdixRoutes.SERVER_DETAILS,
                        arguments = listOf(navArgument("serverId") { type = NavType.StringType })
                    ) { entry ->
                        val serverId = Uri.decode(entry.arguments?.getString("serverId").orEmpty())
                        val detailsViewModel: ServerDetailsViewModel = viewModel(
                            key = "details_$serverId",
                            factory = ServerDetailsViewModel.factory(
                                serverId = serverId,
                                catalogRepository = appContainer.catalogRepository,
                                scanCoordinator = appContainer.scanCoordinator,
                                speedTestEngine = appContainer.speedTestEngine,
                                settingsRepository = appContainer.settingsRepository
                            )
                        )
                        val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
                        ServerDetailsScreen(
                            uiState = uiState,
                            onNavigateBack = { navController.popBackStack() },
                            onRunSingleProbe = detailsViewModel::runSingleProbe,
                            onToggleFavorite = detailsViewModel::toggleFavorite,
                            onTogglePin = detailsViewModel::togglePin,
                            onStartSpeedTest = { targetUrl ->
                                detailsViewModel.startSpeedTest(targetUrl, allowCrossHost = false)
                            },
                            onCancelSpeedTest = detailsViewModel::cancelSpeedTest,
                            onConfirmCrossHost = detailsViewModel::confirmCrossHostRedirect,
                            onDismissCrossHost = detailsViewModel::dismissCrossHostRedirect,
                            onSaveCustomEdit = detailsViewModel::saveEditedCustomServer,
                            onDeleteCustom = {
                                detailsViewModel.deleteCustomServer {
                                    navController.popBackStack()
                                }
                            },
                            onClearCustomEditError = detailsViewModel::clearCustomEditError
                        )
                    }
                }
            }
        }
    }
}
