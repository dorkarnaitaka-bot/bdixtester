package com.jubayerhossain.bdixtester.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jubayerhossain.bdixtester.core.model.TestStatus
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme
import com.jubayerhossain.bdixtester.ui.theme.LocalBdixSemanticColors

@Composable
fun StatusBadge(
    status: TestStatus,
    modifier: Modifier = Modifier
) {
    val semantic = LocalBdixSemanticColors.current
    val (icon: ImageVector, tint: Color, container: Color) = when (status) {
        TestStatus.NOT_TESTED -> Triple(
            Icons.Default.RadioButtonUnchecked,
            semantic.neutral,
            semantic.neutralContainer
        )
        TestStatus.QUEUED -> Triple(
            Icons.Default.HourglassTop,
            semantic.neutral,
            semantic.neutralContainer
        )
        TestStatus.TESTING -> Triple(
            Icons.Default.HourglassTop,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
        TestStatus.REACHABLE -> Triple(
            Icons.Default.CheckCircle,
            semantic.reachable,
            semantic.reachableContainer
        )
        TestStatus.RESTRICTED -> Triple(
            Icons.Default.Lock,
            semantic.restricted,
            semantic.restrictedContainer
        )
        TestStatus.UNREACHABLE -> Triple(
            Icons.Default.ErrorOutline,
            semantic.unreachable,
            semantic.unreachableContainer
        )
        TestStatus.TIMED_OUT -> Triple(
            Icons.Default.TimerOff,
            semantic.restricted,
            semantic.restrictedContainer
        )
        TestStatus.UNSUPPORTED -> Triple(
            Icons.Default.Block,
            semantic.neutral,
            semantic.neutralContainer
        )
        TestStatus.CANCELLED -> Triple(
            Icons.Default.Cancel,
            semantic.neutral,
            semantic.neutralContainer
        )
    }

    Surface(
        color = container,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier
            .testTag("status_badge_${status.name.lowercase()}")
            .semantics { contentDescription = "Status: ${status.displayLabel}" }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status.displayLabel,
                style = MaterialTheme.typography.labelMedium,
                color = tint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    BdixTesterTheme {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusBadge(TestStatus.NOT_TESTED)
            StatusBadge(TestStatus.REACHABLE)
            StatusBadge(TestStatus.RESTRICTED)
        }
    }
}
