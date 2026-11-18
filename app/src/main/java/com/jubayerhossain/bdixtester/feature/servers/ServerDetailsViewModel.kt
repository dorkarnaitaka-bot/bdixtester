package com.jubayerhossain.bdixtester.feature.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jubayerhossain.bdixtester.core.model.ScanMode
import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.data.network.ScanCoordinator
import com.jubayerhossain.bdixtester.data.network.SpeedTestEngine
import com.jubayerhossain.bdixtester.data.network.SpeedTestExecutionOutcome
import com.jubayerhossain.bdixtester.data.network.SpeedTestProgress
import com.jubayerhossain.bdixtester.data.network.SpeedTestResultSummary
import com.jubayerhossain.bdixtester.data.preferences.AppSettings
import com.jubayerhossain.bdixtester.data.preferences.SettingsRepository
import com.jubayerhossain.bdixtester.domain.model.FavoriteInfo
import com.jubayerhossain.bdixtester.domain.model.ServerEndpoint
import com.jubayerhossain.bdixtester.domain.model.ServerTestResult
import com.jubayerhossain.bdixtester.domain.repository.CatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServerDetailsUiState(
    val endpoint: ServerEndpoint? = null,
    val latestResult: ServerTestResult? = null,
    val favoriteInfo: FavoriteInfo? = null,
    val endpointHistory: List<ServerTestResult> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isSingleTesting: Boolean = false,
    val isSpeedTestRunning: Boolean = false,
    val speedTestProgress: SpeedTestProgress? = null,
    val lastSpeedTestSummary: SpeedTestResultSummary? = null,
    val pendingCrossHostRedirect: SpeedTestExecutionOutcome.CrossHostRedirectRequiresConfirmation? = null,
    val customEditError: String? = null
)

class ServerDetailsViewModel(
    private val serverId: String,
    private val catalogRepository: CatalogRepository,
    private val scanCoordinator: ScanCoordinator,
    private val speedTestEngine: SpeedTestEngine,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isSingleTesting = MutableStateFlow(false)
    private val _isSpeedTestRunning = MutableStateFlow(false)
    private val _speedTestProgress = MutableStateFlow<SpeedTestProgress?>(null)
    private val _lastSpeedTestSummary = MutableStateFlow<SpeedTestResultSummary?>(null)
    private val _pendingCrossHost = MutableStateFlow<SpeedTestExecutionOutcome.CrossHostRedirectRequiresConfirmation?>(null)
    private val _customEditError = MutableStateFlow<String?>(null)
    private var speedTestJob: Job? = null

    val uiState: StateFlow<ServerDetailsUiState> = combine(
        catalogRepository.endpoints,
        catalogRepository.latestResults,
        catalogRepository.favorites,
        catalogRepository.observeHistoryForServer(serverId),
        settingsRepository.settings,
        _isSingleTesting,
        _isSpeedTestRunning,
        _speedTestProgress,
        _lastSpeedTestSummary,
        _pendingCrossHost,
        _customEditError
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val endpoints = values[0] as List<ServerEndpoint>
        @Suppress("UNCHECKED_CAST")
        val latest = values[1] as Map<String, ServerTestResult>
        @Suppress("UNCHECKED_CAST")
        val favorites = values[2] as Map<String, FavoriteInfo>
        @Suppress("UNCHECKED_CAST")
        val endpointHistory = values[3] as List<ServerTestResult>
        val settings = values[4] as AppSettings
        val singleTesting = values[5] as Boolean
        val speedRunning = values[6] as Boolean
        val speedProgress = values[7] as SpeedTestProgress?
        val speedSummary = values[8] as SpeedTestResultSummary?
        val crossHost = values[9] as SpeedTestExecutionOutcome.CrossHostRedirectRequiresConfirmation?
        val editError = values[10] as String?

        ServerDetailsUiState(
            endpoint = endpoints.firstOrNull { it.id == serverId },
            latestResult = latest[serverId],
            favoriteInfo = favorites[serverId],
            endpointHistory = endpointHistory,
            settings = settings,
            isSingleTesting = singleTesting,
            isSpeedTestRunning = speedRunning,
            speedTestProgress = speedProgress,
            lastSpeedTestSummary = speedSummary,
            pendingCrossHostRedirect = crossHost,
            customEditError = editError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ServerDetailsUiState(endpoint = catalogRepository.getEndpointById(serverId))
    )

    fun runSingleProbe(mode: ScanMode) {
        val endpoint = uiState.value.endpoint ?: return
        if (_isSingleTesting.value) return
        viewModelScope.launch {
            _isSingleTesting.value = true
            try {
                val settings = settingsRepository.settings.first()
                scanCoordinator.testSingleEndpoint(
                    endpoint = endpoint,
                    scanMode = mode,
                    settings = settings
                )
            } finally {
                _isSingleTesting.value = false
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            catalogRepository.toggleFavorite(serverId)
        }
    }

    fun togglePin() {
        val currentlyPinned = uiState.value.favoriteInfo?.isPinned == true
        viewModelScope.launch {
            catalogRepository.setFavoritePinned(serverId, !currentlyPinned)
        }
    }

    fun startSpeedTest(targetUrl: String, allowCrossHost: Boolean = false) {
        val endpoint = uiState.value.endpoint ?: return
        if (speedTestJob?.isActive == true) return
        _pendingCrossHost.value = null
        speedTestJob = viewModelScope.launch {
            _isSpeedTestRunning.value = true
            _speedTestProgress.value = SpeedTestProgress()
            _lastSpeedTestSummary.value = null
            try {
                val settings = settingsRepository.settings.first()
                val maxBytes = endpoint.speedTestMaxBytes ?: settings.speedTestMaxBytes
                val outcome = speedTestEngine.runSpeedTest(
                    endpoint = endpoint,
                    targetUrl = targetUrl.trim(),
                    maxBytes = maxBytes,
                    allowConfirmedCrossHost = allowCrossHost,
                    networkProfileLabel = settings.manualNetworkLabel.takeIf { it.isNotBlank() },
                    onProgress = { progress ->
                        _speedTestProgress.value = progress
                    }
                )
                when (outcome) {
                    is SpeedTestExecutionOutcome.CrossHostRedirectRequiresConfirmation -> {
                        _pendingCrossHost.value = outcome
                    }
                    is SpeedTestExecutionOutcome.Completed -> {
                        _lastSpeedTestSummary.value = outcome.result
                    }
                }
            } finally {
                _isSpeedTestRunning.value = false
            }
        }
    }

    fun confirmCrossHostRedirect() {
        val pending = _pendingCrossHost.value ?: return
        _pendingCrossHost.value = null
        startSpeedTest(targetUrl = pending.targetUrl, allowCrossHost = true)
    }

    fun dismissCrossHostRedirect() {
        _pendingCrossHost.value = null
    }

    fun cancelSpeedTest() {
        speedTestJob?.cancel()
    }

    fun saveEditedCustomServer(
        name: String,
        rawUrl: String,
        category: ServerCategory,
        speedTestUrl: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = catalogRepository.saveCustomServer(
                existingId = serverId,
                name = name,
                rawUrl = rawUrl,
                category = category,
                speedTestUrl = speedTestUrl
            )
            res.onSuccess {
                _customEditError.value = null
                onSuccess()
            }.onFailure { err ->
                _customEditError.value = err.message ?: "Invalid server details."
            }
        }
    }

    fun deleteCustomServer(onDeleted: () -> Unit) {
        viewModelScope.launch {
            catalogRepository.deleteCustomServer(serverId)
            onDeleted()
        }
    }

    fun clearCustomEditError() {
        _customEditError.value = null
    }

    companion object {
        fun factory(
            serverId: String,
            catalogRepository: CatalogRepository,
            scanCoordinator: ScanCoordinator,
            speedTestEngine: SpeedTestEngine,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ServerDetailsViewModel(
                    serverId = serverId,
                    catalogRepository = catalogRepository,
                    scanCoordinator = scanCoordinator,
                    speedTestEngine = speedTestEngine,
                    settingsRepository = settingsRepository
                ) as T
            }
        }
    }
}
