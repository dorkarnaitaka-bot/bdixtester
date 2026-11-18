package com.jubayerhossain.bdixtester.feature.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jubayerhossain.bdixtester.core.export.CatalogExportSerializer
import com.jubayerhossain.bdixtester.core.export.ExportDataset
import com.jubayerhossain.bdixtester.core.export.ExportFormat
import com.jubayerhossain.bdixtester.core.model.CatalogSourceType
import com.jubayerhossain.bdixtester.core.model.ScanMode
import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.core.model.ServerProtocol
import com.jubayerhossain.bdixtester.core.model.TestStatus
import com.jubayerhossain.bdixtester.core.util.ServerDisplayCategory
import com.jubayerhossain.bdixtester.core.util.ServerDisplayClassifier
import com.jubayerhossain.bdixtester.data.catalog.BdOttPlatforms
import com.jubayerhossain.bdixtester.data.catalog.OttPlatform
import com.jubayerhossain.bdixtester.data.catalog.OttRegion
import com.jubayerhossain.bdixtester.data.catalog.OttRegionFilter
import com.jubayerhossain.bdixtester.data.network.ScanCoordinator
import com.jubayerhossain.bdixtester.data.preferences.AppSettings
import com.jubayerhossain.bdixtester.data.preferences.SettingsRepository
import com.jubayerhossain.bdixtester.domain.model.FavoriteInfo
import com.jubayerhossain.bdixtester.domain.model.ServerEndpoint
import com.jubayerhossain.bdixtester.domain.model.ServerTestResult
import com.jubayerhossain.bdixtester.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ServerSortOrder(val displayLabel: String) {
    SOURCE_ORDER("Source Order"),
    NAME_ASC("Name (A–Z)"),
    CATEGORY("Category"),
    PROTOCOL("Protocol"),
    LATENCY("Fastest Latency")
}

enum class ServerStatusTab {
    WORKING,
    NOT_TESTED,
    NOT_WORKING
}

enum class ServerBottomTab {
    ALL,
    OTT,
    TV,
    FTP,
    MOVIE
}

data class ServersUiState(
    val totalCatalogCount: Int = 0,
    val filteredServers: List<ServerEndpoint> = emptyList(),
    val filteredOttPlatforms: List<OttPlatform> = BdOttPlatforms.all,
    val selectedOttRegion: OttRegionFilter = OttRegionFilter.ALL,
    val latestResults: Map<String, ServerTestResult> = emptyMap(),
    val favorites: Map<String, FavoriteInfo> = emptyMap(),
    val selectedStatusTab: ServerStatusTab = ServerStatusTab.WORKING,
    val selectedBottomTab: ServerBottomTab = ServerBottomTab.ALL,
    val workingCount: Int = 0,
    val notTestedCount: Int = 0,
    val notWorkingCount: Int = 0,
    val selectedCategory: ServerCategory = ServerCategory.ALL,
    val selectedProtocol: ServerProtocol? = null,
    val selectedStatus: TestStatus? = null,
    val selectedSourceType: CatalogSourceType? = null,
    val featuredOnly: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: ServerSortOrder = ServerSortOrder.SOURCE_ORDER,
    val customServerError: String? = null,
    val testingServerIds: Set<String> = emptySet()
)

class ServersViewModel(
    private val catalogRepository: CatalogRepository,
    private val scanCoordinator: ScanCoordinator,
    private val settingsRepository: SettingsRepository,
    initialCategory: ServerCategory = ServerCategory.ALL
) : ViewModel() {

    private val _selectedStatusTab = MutableStateFlow(ServerStatusTab.WORKING)
    private val _selectedBottomTab = MutableStateFlow(ServerBottomTab.ALL)
    private val _selectedOttRegion = MutableStateFlow(OttRegionFilter.ALL)
    private val _selectedCategory = MutableStateFlow(initialCategory)
    private val _selectedProtocol = MutableStateFlow<ServerProtocol?>(null)
    private val _selectedStatus = MutableStateFlow<TestStatus?>(null)
    private val _selectedSourceType = MutableStateFlow<CatalogSourceType?>(null)
    private val _featuredOnly = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(ServerSortOrder.SOURCE_ORDER)
    private val _customServerError = MutableStateFlow<String?>(null)
    private val _testingServerIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<ServersUiState> = combine(
        catalogRepository.endpoints,
        catalogRepository.latestResults,
        catalogRepository.favorites,
        _selectedStatusTab,
        _selectedBottomTab,
        _selectedOttRegion,
        _searchQuery,
        _sortOrder,
        _customServerError,
        _testingServerIds
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val allEndpoints = values[0] as List<ServerEndpoint>
        @Suppress("UNCHECKED_CAST")
        val results = values[1] as Map<String, ServerTestResult>
        @Suppress("UNCHECKED_CAST")
        val favorites = values[2] as Map<String, FavoriteInfo>
        val statusTab = values[3] as ServerStatusTab
        val bottomTab = values[4] as ServerBottomTab
        val ottRegion = values[5] as OttRegionFilter
        val query = values[6] as String
        val sort = values[7] as ServerSortOrder
        val customError = values[8] as String?
        @Suppress("UNCHECKED_CAST")
        val testingIds = values[9] as Set<String>

        val workingCount = allEndpoints.count { statusTabFor(results[it.id]?.status) == ServerStatusTab.WORKING }
        val notTestedCount = allEndpoints.count { statusTabFor(results[it.id]?.status) == ServerStatusTab.NOT_TESTED }
        val notWorkingCount = allEndpoints.count { statusTabFor(results[it.id]?.status) == ServerStatusTab.NOT_WORKING }

        val normalizedQuery = query.trim().lowercase()
        val filtered = allEndpoints
            .asSequence()
            .filter { statusTabFor(results[it.id]?.status) == statusTab }
            .filter { endpoint ->
                when (bottomTab) {
                    ServerBottomTab.ALL -> true
                    ServerBottomTab.OTT -> false
                    ServerBottomTab.TV -> ServerDisplayClassifier.classify(endpoint) == ServerDisplayCategory.TV
                    ServerBottomTab.FTP -> ServerDisplayClassifier.classify(endpoint) == ServerDisplayCategory.FTP
                    ServerBottomTab.MOVIE -> ServerDisplayClassifier.classify(endpoint) == ServerDisplayCategory.MOVIE
                }
            }
            .filter {
                normalizedQuery.isEmpty() ||
                    it.name.lowercase().contains(normalizedQuery) ||
                    it.originalUrl.lowercase().contains(normalizedQuery) ||
                    it.host.lowercase().contains(normalizedQuery) ||
                    ServerDisplayClassifier.displayLabel(it).lowercase().contains(normalizedQuery)
            }
            .toList()
            .let { list ->
                when (sort) {
                    ServerSortOrder.SOURCE_ORDER -> list
                    ServerSortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                    ServerSortOrder.CATEGORY -> list.sortedBy { ServerDisplayClassifier.displayLabel(it) }
                    ServerSortOrder.PROTOCOL -> list.sortedBy { it.protocol.displayLabel }
                    ServerSortOrder.LATENCY -> list.sortedWith(
                        compareBy<ServerEndpoint> { endpoint ->
                            results[endpoint.id]?.primaryLatencyMs?.takeIf { it >= 0L } ?: Long.MAX_VALUE
                        }.thenBy { it.name.lowercase() }
                    )
                }
            }

        val filteredOtt = BdOttPlatforms.all
            .asSequence()
            .filter { platform ->
                when (ottRegion) {
                    OttRegionFilter.ALL -> true
                    OttRegionFilter.BANGLADESH -> platform.region == OttRegion.BANGLADESH
                    OttRegionFilter.GLOBAL_IN_BD -> platform.region == OttRegion.GLOBAL_IN_BD
                }
            }
            .filter { platform ->
                normalizedQuery.isEmpty() ||
                    platform.name.lowercase().contains(normalizedQuery) ||
                    platform.tagline.lowercase().contains(normalizedQuery) ||
                    platform.url.lowercase().contains(normalizedQuery)
            }
            .toList()

        ServersUiState(
            totalCatalogCount = allEndpoints.size,
            filteredServers = filtered,
            filteredOttPlatforms = filteredOtt,
            selectedOttRegion = ottRegion,
            latestResults = results,
            favorites = favorites,
            selectedStatusTab = statusTab,
            selectedBottomTab = bottomTab,
            workingCount = workingCount,
            notTestedCount = notTestedCount,
            notWorkingCount = notWorkingCount,
            selectedCategory = _selectedCategory.value,
            selectedProtocol = _selectedProtocol.value,
            selectedStatus = _selectedStatus.value,
            selectedSourceType = _selectedSourceType.value,
            featuredOnly = _featuredOnly.value,
            searchQuery = query,
            sortOrder = sort,
            customServerError = customError,
            testingServerIds = testingIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ServersUiState(selectedCategory = initialCategory)
    )

    fun selectStatusTab(tab: ServerStatusTab) {
        _selectedStatusTab.value = tab
    }

    fun selectBottomTab(tab: ServerBottomTab) {
        _selectedBottomTab.value = tab
    }

    fun selectOttRegion(region: OttRegionFilter) {
        _selectedOttRegion.value = region
    }

    fun prepareForViewing() {
        val results = catalogRepository.latestResults.value
        val hasWorking = results.values.any { it.status == TestStatus.REACHABLE }
        val hasAnyTested = results.isNotEmpty()
        _selectedStatusTab.value = when {
            hasWorking -> ServerStatusTab.WORKING
            !hasAnyTested -> ServerStatusTab.NOT_TESTED
            else -> ServerStatusTab.WORKING
        }
    }

    fun selectCategory(category: ServerCategory) {
        _selectedCategory.value = category
    }

    fun selectProtocol(protocol: ServerProtocol?) {
        _selectedProtocol.value = protocol
    }

    fun selectStatus(status: TestStatus?) {
        _selectedStatus.value = status
    }

    fun selectSourceType(sourceType: CatalogSourceType?) {
        _selectedSourceType.value = sourceType
    }

    fun toggleFeaturedOnly() {
        _featuredOnly.value = !_featuredOnly.value
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOrder(sortOrder: ServerSortOrder) {
        _sortOrder.value = sortOrder
    }

    fun clearFilters() {
        _selectedBottomTab.value = ServerBottomTab.ALL
        _selectedOttRegion.value = OttRegionFilter.ALL
        _selectedCategory.value = ServerCategory.ALL
        _selectedProtocol.value = null
        _selectedStatus.value = null
        _selectedSourceType.value = null
        _featuredOnly.value = false
        _searchQuery.value = ""
    }

    fun clearCustomServerError() {
        _customServerError.value = null
    }

    fun toggleFavorite(serverId: String) {
        viewModelScope.launch {
            catalogRepository.toggleFavorite(serverId)
        }
    }

    fun saveCustomServer(
        existingId: String?,
        name: String,
        rawUrl: String,
        category: ServerCategory,
        speedTestUrl: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = catalogRepository.saveCustomServer(
                existingId = existingId,
                name = name,
                rawUrl = rawUrl,
                category = category,
                speedTestUrl = speedTestUrl
            )
            result.onSuccess {
                _customServerError.value = null
                onSuccess()
            }.onFailure { err ->
                _customServerError.value = err.message ?: "Invalid custom server input."
            }
        }
    }

    fun deleteCustomServer(serverId: String) {
        viewModelScope.launch {
            catalogRepository.deleteCustomServer(serverId)
        }
    }

    fun testSingleServer(endpoint: ServerEndpoint) {
        if (_testingServerIds.value.contains(endpoint.id)) return
        viewModelScope.launch {
            _testingServerIds.value = _testingServerIds.value + endpoint.id
            try {
                val settings: AppSettings = settingsRepository.settings.first()
                scanCoordinator.testSingleEndpoint(
                    endpoint = endpoint,
                    scanMode = ScanMode.FULL_SCAN,
                    settings = settings
                )
            } finally {
                _testingServerIds.value = _testingServerIds.value - endpoint.id
            }
        }
    }

    fun buildExportContent(dataset: ExportDataset, format: ExportFormat): String {
        val state = uiState.value
        val items = when (dataset) {
            ExportDataset.CUSTOM_SERVERS -> catalogRepository.endpoints.value.filter {
                it.sourceType == CatalogSourceType.USER_CUSTOM
            }
            else -> state.filteredServers
        }
        return CatalogExportSerializer.exportEndpoints(
            endpoints = items,
            latestResults = state.latestResults,
            favorites = state.favorites,
            format = format,
            dataset = dataset
        )
    }

    companion object {
        fun statusTabFor(status: TestStatus?): ServerStatusTab {
            return when (status) {
                TestStatus.REACHABLE -> ServerStatusTab.WORKING
                null, TestStatus.NOT_TESTED, TestStatus.QUEUED, TestStatus.TESTING -> ServerStatusTab.NOT_TESTED
                TestStatus.UNREACHABLE,
                TestStatus.TIMED_OUT,
                TestStatus.RESTRICTED,
                TestStatus.UNSUPPORTED,
                TestStatus.CANCELLED -> ServerStatusTab.NOT_WORKING
            }
        }

        fun factory(
            catalogRepository: CatalogRepository,
            scanCoordinator: ScanCoordinator,
            settingsRepository: SettingsRepository,
            initialCategory: ServerCategory = ServerCategory.ALL
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ServersViewModel(
                    catalogRepository = catalogRepository,
                    scanCoordinator = scanCoordinator,
                    settingsRepository = settingsRepository,
                    initialCategory = initialCategory
                ) as T
            }
        }
    }
}
