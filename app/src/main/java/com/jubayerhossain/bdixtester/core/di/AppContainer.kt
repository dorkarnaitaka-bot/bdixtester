package com.jubayerhossain.bdixtester.core.di

import android.content.Context
import com.jubayerhossain.bdixtester.core.model.AppLanguage
import com.jubayerhossain.bdixtester.core.model.ThemePreference
import com.jubayerhossain.bdixtester.core.network.AndroidConnectivityObserver
import com.jubayerhossain.bdixtester.core.network.ConnectivityObserver
import com.jubayerhossain.bdixtester.data.local.BdixDatabase
import com.jubayerhossain.bdixtester.data.network.EndpointProbeExecutor
import com.jubayerhossain.bdixtester.data.network.FtpControlTester
import com.jubayerhossain.bdixtester.data.network.HttpEndpointTester
import com.jubayerhossain.bdixtester.data.network.RoutingEndpointProbeExecutor
import com.jubayerhossain.bdixtester.data.network.ScanCoordinator
import com.jubayerhossain.bdixtester.data.network.SpeedTestEngine
import com.jubayerhossain.bdixtester.data.preferences.AppSettings
import com.jubayerhossain.bdixtester.data.preferences.SettingsDataStoreRepository
import com.jubayerhossain.bdixtester.data.preferences.SettingsRepository
import com.jubayerhossain.bdixtester.data.remote.CatalogSignatureVerifier
import com.jubayerhossain.bdixtester.data.remote.CatalogSyncScheduler
import com.jubayerhossain.bdixtester.data.remote.RemoteCatalogSyncManager
import com.jubayerhossain.bdixtester.data.repository.RoomCatalogRepository
import com.jubayerhossain.bdixtester.domain.repository.CatalogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: BdixDatabase = BdixDatabase.create(appContext)
    val settingsRepository: SettingsRepository = SettingsDataStoreRepository(appContext)
    val catalogRepository: CatalogRepository = RoomCatalogRepository(database = database)
    val connectivityObserver: ConnectivityObserver = AndroidConnectivityObserver(appContext)

    val okHttpClient: OkHttpClient = OkHttpClient()
    val httpEndpointTester = HttpEndpointTester(baseClient = okHttpClient)
    val ftpControlTester = FtpControlTester()
    val probeExecutor: EndpointProbeExecutor = RoutingEndpointProbeExecutor(
        httpTester = httpEndpointTester,
        ftpTester = ftpControlTester
    )

    val scanCoordinator = ScanCoordinator(
        catalogRepository = catalogRepository,
        probeExecutor = probeExecutor
    )

    val speedTestEngine = SpeedTestEngine(
        baseClient = okHttpClient,
        catalogRepository = catalogRepository
    )

    val signatureVerifier = CatalogSignatureVerifier()
    val remoteCatalogSyncManager = RemoteCatalogSyncManager(
        database = database,
        connectivityObserver = connectivityObserver,
        signatureVerifier = signatureVerifier,
        baseClient = okHttpClient
    )
    val catalogSyncScheduler = CatalogSyncScheduler(appContext)

    val appSettings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(appScope, SharingStarted.Eagerly, AppSettings())

    val themePreference: StateFlow<ThemePreference> = appSettings
        .map { it.themePreference }
        .distinctUntilChanged()
        .stateIn(appScope, SharingStarted.Eagerly, ThemePreference.SYSTEM)

    val appLanguage: StateFlow<AppLanguage> = appSettings
        .map { it.appLanguage }
        .distinctUntilChanged()
        .stateIn(appScope, SharingStarted.Eagerly, AppLanguage.SYSTEM)

    val reduceAnimations: StateFlow<Boolean> = appSettings
        .map { it.reduceAnimations }
        .distinctUntilChanged()
        .stateIn(appScope, SharingStarted.Eagerly, false)

    fun initializeOnStartup() {
        appScope.launch {
            catalogRepository.ensureInitialized()
            val initialSettings = settingsRepository.settings.first()
            catalogRepository.pruneExpiredHistory(initialSettings.historyRetentionDays)
            catalogSyncScheduler.applySchedule(initialSettings)

            settingsRepository.settings
                .map { Triple(it.autoCatalogUpdates, it.wifiOnlyCatalogUpdates, it.remoteCatalogUrl) }
                .distinctUntilChanged()
                .collect {
                    catalogSyncScheduler.applySchedule(settingsRepository.settings.first())
                }
        }
    }
}
