package com.jubayerhossain.bdixtester.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.BuildConfig
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.model.AppLanguage
import com.jubayerhossain.bdixtester.core.model.ThemePreference
import com.jubayerhossain.bdixtester.ui.components.LtrContent
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme
import kotlin.math.roundToInt

private enum class BulkDeleteAction {
    HISTORY,
    FAVORITES,
    CUSTOM_SERVERS,
    RESET_ALL
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSelectTheme: (ThemePreference) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onMaxConcurrencyChange: (Int) -> Unit,
    onSpeedTestMaxBytesChange: (Long) -> Unit,
    onManualNetworkLabelSave: (String) -> Unit,
    onRemoteCatalogUrlSave: (String) -> Unit,
    onAutoUpdatesChange: (Boolean) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onCrossOriginChange: (Boolean) -> Unit,
    onSyncNowClick: (String) -> Unit,
    onHistoryRetentionChange: (Int) -> Unit,
    onClearHistory: () -> Unit,
    onClearFavorites: () -> Unit,
    onDeleteCustomServers: () -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    var remoteUrlDraft by remember(settings.remoteCatalogUrl) { mutableStateOf(settings.remoteCatalogUrl) }
    var networkLabelDraft by remember(settings.manualNetworkLabel) { mutableStateOf(settings.manualNetworkLabel) }
    var pendingBulkAction by remember { mutableStateOf<BulkDeleteAction?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(BdixSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BdixSpacing.lg)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        item {
            SettingsCard(
                title = stringResource(R.string.settings_appearance_section),
                modifier = Modifier.widthIn(max = 720.dp)
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_theme_selector")
                ) {
                    ThemePreference.entries.forEachIndexed { index, pref ->
                        SegmentedButton(
                            selected = settings.themePreference == pref,
                            onClick = { onSelectTheme(pref) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemePreference.entries.size),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("theme_option_${pref.name.lowercase()}")
                        ) {
                            Text(pref.displayLabel)
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.settings_language_label),
                    style = MaterialTheme.typography.labelLarge
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_language_selector")
                ) {
                    AppLanguage.entries.forEachIndexed { index, lang ->
                        SegmentedButton(
                            selected = settings.appLanguage == lang,
                            onClick = { onSelectLanguage(lang) },
                            shape = SegmentedButtonDefaults.itemShape(index, AppLanguage.entries.size),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("language_option_${lang.name.lowercase()}")
                        ) {
                            Text(lang.displayLabel)
                        }
                    }
                }
            }
        }

        item {
            SettingsCard(
                title = stringResource(R.string.settings_network_section),
                modifier = Modifier.widthIn(max = 720.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_concurrency_label, settings.maxConcurrency),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = settings.maxConcurrency.toFloat(),
                    onValueChange = { onMaxConcurrencyChange(it.roundToInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.testTag("settings_concurrency_slider")
                )
                LtrContent {
                    Text(
                        text = stringResource(
                            R.string.settings_timeouts_summary,
                            settings.fullScanConnectTimeoutMs / 1000,
                            settings.fullScanReadTimeoutMs / 1000,
                            "${settings.quickScanDeadlineMs / 1000.0}s"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val currentCapMib = (settings.speedTestMaxBytes / (1024L * 1024L)).toInt()
                Text(
                    text = stringResource(R.string.settings_speed_cap_label, currentCapMib),
                    style = MaterialTheme.typography.bodyMedium
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)) {
                    listOf(2, 5, 10, 20).forEach { mib ->
                        FilterChip(
                            selected = currentCapMib == mib,
                            onClick = { onSpeedTestMaxBytesChange(mib.toLong() * 1024L * 1024L) },
                            label = { Text("$mib MiB") },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = networkLabelDraft,
                    onValueChange = { networkLabelDraft = it },
                    label = { Text(stringResource(R.string.settings_network_profile_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_network_label_input")
                )
                OutlinedButton(
                    onClick = { onManualNetworkLabelSave(networkLabelDraft) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }

        item {
            val sync = uiState.catalogSyncState
            SettingsCard(
                title = stringResource(R.string.settings_catalog_section),
                modifier = Modifier.widthIn(max = 720.dp)
            ) {
                LtrContent {
                    OutlinedTextField(
                        value = remoteUrlDraft,
                        onValueChange = { remoteUrlDraft = it },
                        label = { Text(stringResource(R.string.settings_remote_url_label)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_remote_url_input")
                    )
                }

                SwitchRow(
                    title = stringResource(R.string.settings_auto_sync_label),
                    checked = settings.autoCatalogUpdates,
                    onCheckedChange = {
                        onRemoteCatalogUrlSave(remoteUrlDraft)
                        onAutoUpdatesChange(it)
                    },
                    testTag = "settings_auto_sync_switch"
                )
                SwitchRow(
                    title = stringResource(R.string.settings_wifi_only_label),
                    checked = settings.wifiOnlyCatalogUpdates,
                    onCheckedChange = onWifiOnlyChange,
                    testTag = "settings_wifi_only_switch"
                )
                SwitchRow(
                    title = stringResource(R.string.settings_cross_origin_label),
                    checked = settings.allowCrossOriginRemotePayload,
                    onCheckedChange = onCrossOriginChange,
                    testTag = "settings_cross_origin_switch"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { onRemoteCatalogUrlSave(remoteUrlDraft) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                    Button(
                        onClick = { onSyncNowClick(remoteUrlDraft) },
                        enabled = !uiState.isSyncing && remoteUrlDraft.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("settings_sync_now_button")
                    ) {
                        Text(stringResource(R.string.action_sync_now))
                    }
                }

                LtrContent {
                    Text(
                        text = "Status: ${sync.lastSyncStatus.displayLabel} | Seq #${sync.catalogSequence} | Remote Entries: ${sync.remoteEntryCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                sync.lastSyncMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SettingsCard(
                title = stringResource(R.string.settings_data_section),
                modifier = Modifier.widthIn(max = 720.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_retention_label, settings.historyRetentionDays),
                    style = MaterialTheme.typography.bodyMedium
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)) {
                    listOf(7, 30, 90, 365).forEach { days ->
                        FilterChip(
                            selected = settings.historyRetentionDays == days,
                            onClick = { onHistoryRetentionChange(days) },
                            label = { Text("${days}d") },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { pendingBulkAction = BulkDeleteAction.HISTORY },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("settings_clear_history_button")
                    ) {
                        Text(stringResource(R.string.history_clear_all))
                    }
                    OutlinedButton(
                        onClick = { pendingBulkAction = BulkDeleteAction.FAVORITES },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("settings_clear_favorites_button")
                    ) {
                        Text(stringResource(R.string.settings_clear_favorites))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { pendingBulkAction = BulkDeleteAction.CUSTOM_SERVERS },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("settings_clear_custom_button")
                    ) {
                        Text(stringResource(R.string.settings_clear_custom))
                    }
                    Button(
                        onClick = { pendingBulkAction = BulkDeleteAction.RESET_ALL },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("settings_reset_all_button")
                    ) {
                        Text(stringResource(R.string.settings_reset_all))
                    }
                }
            }
        }

        item {
            SettingsCard(
                title = stringResource(R.string.settings_privacy_section),
                modifier = Modifier.widthIn(max = 720.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_privacy_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_developer_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.settings_developer_value),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LtrContent {
                        Text(
                            text = "${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    pendingBulkAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingBulkAction = null },
            title = { Text(stringResource(R.string.settings_confirm_action_title)) },
            text = {
                Text(
                    when (action) {
                        BulkDeleteAction.HISTORY -> stringResource(R.string.history_clear_confirm_body)
                        BulkDeleteAction.FAVORITES -> "Remove all saved favorite and pinned endpoints?"
                        BulkDeleteAction.CUSTOM_SERVERS -> "Delete all user-created custom endpoints?"
                        BulkDeleteAction.RESET_ALL -> "Clear history, favorites, custom servers, remote catalog cache, and reset settings to defaults? Built-in catalog entries remain intact."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (action) {
                            BulkDeleteAction.HISTORY -> onClearHistory()
                            BulkDeleteAction.FAVORITES -> onClearFavorites()
                            BulkDeleteAction.CUSTOM_SERVERS -> onDeleteCustomServers()
                            BulkDeleteAction.RESET_ALL -> onResetAllData()
                        }
                        pendingBulkAction = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingBulkAction = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(end = BdixSpacing.sm)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsCard(
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
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.md)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    BdixTesterTheme {
        SettingsScreen(
            uiState = SettingsUiState(),
            onSelectTheme = {},
            onSelectLanguage = {},
            onMaxConcurrencyChange = {},
            onSpeedTestMaxBytesChange = {},
            onManualNetworkLabelSave = {},
            onRemoteCatalogUrlSave = {},
            onAutoUpdatesChange = {},
            onWifiOnlyChange = {},
            onCrossOriginChange = {},
            onSyncNowClick = {},
            onHistoryRetentionChange = {},
            onClearHistory = {},
            onClearFavorites = {},
            onDeleteCustomServers = {},
            onResetAllData = {}
        )
    }
}
