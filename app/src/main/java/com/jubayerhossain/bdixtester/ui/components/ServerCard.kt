package com.jubayerhossain.bdixtester.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.R
import com.jubayerhossain.bdixtester.core.model.CatalogSourceType
import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.core.model.ServerProtocol
import com.jubayerhossain.bdixtester.core.model.TestStatus
import com.jubayerhossain.bdixtester.core.model.VerificationState
import com.jubayerhossain.bdixtester.domain.model.ServerEndpoint
import com.jubayerhossain.bdixtester.domain.model.ServerTestResult
import com.jubayerhossain.bdixtester.ui.theme.BdixSpacing
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme
import com.jubayerhossain.bdixtester.ui.theme.MonoUrlStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServerCard(
    endpoint: ServerEndpoint,
    result: ServerTestResult?,
    onCardClick: () -> Unit,
    onVisitWebsiteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    isPinned: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onTestClick: (() -> Unit)? = null,
    onEditCustomClick: (() -> Unit)? = null,
    onDeleteCustomClick: (() -> Unit)? = null
) {
    val status = result?.status ?: TestStatus.NOT_TESTED

    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("server_card_${endpoint.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BdixSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(BdixSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.action_pin),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = endpoint.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (endpoint.isFeatured) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = stringResource(R.string.label_featured),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("favorite_toggle_${endpoint.id}")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = stringResource(
                                    if (isFavorite) R.string.action_remove_favorite else R.string.action_add_favorite
                                ),
                                tint = if (isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            LtrContent {
                Text(
                    text = endpoint.originalUrl,
                    style = MonoUrlStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(status = status)
                MetadataPill(label = endpoint.category.displayName)
                MetadataPill(label = endpoint.protocol.displayLabel)
                if (endpoint.sourceType != CatalogSourceType.BUILT_IN) {
                    MetadataPill(label = endpoint.sourceType.displayLabel)
                } else if (endpoint.sourceIndex != null) {
                    LtrContent {
                        MetadataPill(label = "#${endpoint.sourceIndex + 1}")
                    }
                }
                if (endpoint.isPrivateIp) {
                    MetadataPill(label = stringResource(R.string.label_private_ip))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val latency = result?.primaryLatencyMs
                    val throughput = result?.downloadMbps
                    if (latency != null) {
                        LtrContent {
                            Text(
                                text = stringResource(R.string.label_latency_ms, latency),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (throughput != null) {
                        LtrContent {
                            Text(
                                text = stringResource(
                                    R.string.label_throughput_mbps,
                                    "%.2f".format(Locale.US, throughput)
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (latency == null && throughput == null) {
                        Text(
                            text = result?.failureMessage ?: stringResource(R.string.label_not_yet_tested),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onEditCustomClick != null) {
                        IconButton(
                            onClick = onEditCustomClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_custom_${endpoint.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_edit)
                            )
                        }
                    }
                    if (onDeleteCustomClick != null) {
                        IconButton(
                            onClick = onDeleteCustomClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_custom_${endpoint.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.action_delete)
                            )
                        }
                    }
                    if (onTestClick != null) {
                        IconButton(
                            onClick = onTestClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("test_endpoint_${endpoint.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = stringResource(R.string.action_test_server),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onVisitWebsiteClick,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("visit_website_button_${endpoint.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_visit_website),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataPill(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ServerCardPreview() {
    BdixTesterTheme {
        ServerCard(
            endpoint = ServerEndpoint(
                id = "builtin:0",
                name = "sample-bdix.local",
                originalUrl = "http://sample-bdix.local/",
                host = "sample-bdix.local",
                explicitPort = null,
                protocol = ServerProtocol.HTTP,
                category = ServerCategory.UNCATEGORIZED,
                isFeatured = false,
                sourceType = CatalogSourceType.BUILT_IN,
                sourceIndex = 0,
                verificationState = VerificationState.UNKNOWN,
                lastReviewedAtEpochMs = null,
                speedTestUrl = null,
                isPrivateIp = false
            ),
            result = null,
            onCardClick = {},
            onVisitWebsiteClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
