package com.jubayerhossain.bdixtester.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BdixMotionSpecs {
    val bouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val snappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

/**
 * Tactile spring press scale feedback driven by an [InteractionSource].
 */
fun Modifier.springPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = BdixMotionSpecs.bouncySpring,
        label = "spring_press_scale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Live connection status dot with an expanding soft radar pulse halo when [isLive] is true.
 */
@Composable
fun PulsingStatusDot(
    color: Color,
    isLive: Boolean,
    dotSize: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "status_dot_pulse")
    val pulseProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_progress"
    )

    Box(
        modifier = modifier.size(dotSize * 2.1f),
        contentAlignment = Alignment.Center
    ) {
        if (isLive) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        val ringScale = 1f + pulseProgress * 1.45f
                        scaleX = ringScale
                        scaleY = ringScale
                        alpha = (1f - pulseProgress) * 0.55f
                    }
                    .background(color = color, shape = CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(color = color, shape = CircleShape)
        )
    }
}

/**
 * Smooth rolling vertical counter for tabular numbers (Hero working count, status tab badges).
 */
@Composable
fun RollingCountText(
    count: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            if (targetState >= initialState) {
                (slideInVertically(tween(220)) { it / 2 } + fadeIn(tween(180))) togetherWith
                    (slideOutVertically(tween(220)) { -it / 2 } + fadeOut(tween(140)))
            } else {
                (slideInVertically(tween(220)) { -it / 2 } + fadeIn(tween(180))) togetherWith
                    (slideOutVertically(tween(220)) { it / 2 } + fadeOut(tween(140)))
            }
        },
        modifier = modifier,
        label = "rolling_count"
    ) { targetCount ->
        Text(
            text = targetCount.toString(),
            style = style,
            color = color,
            maxLines = 1
        )
    }
}

/**
 * Expressive bookmark icon with spring scale pop and smooth tint transition.
 */
@Composable
fun AnimatedBookmarkIcon(
    isFavorite: Boolean,
    contentDescription: String,
    iconSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val tint by animateColorAsState(
        targetValue = if (isFavorite) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "bookmark_tint"
    )

    LaunchedEffect(isFavorite) {
        scale.snapTo(if (isFavorite) 1.32f else 0.86f)
        scale.animateTo(1f, animationSpec = BdixMotionSpecs.bouncySpring)
    }

    Icon(
        imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
            .size(iconSize)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    )
}

/**
 * Expressive pin icon with smooth tilt rotation and spring bounce.
 */
@Composable
fun AnimatedPinIcon(
    isPinned: Boolean,
    contentDescription: String,
    iconSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isPinned) 0f else -32f,
        animationSpec = BdixMotionSpecs.bouncySpring,
        label = "pin_rotation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPinned) 1.12f else 1f,
        animationSpec = BdixMotionSpecs.bouncySpring,
        label = "pin_scale"
    )
    val tint by animateColorAsState(
        targetValue = if (isPinned) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "pin_tint"
    )

    Icon(
        imageVector = Icons.Rounded.PushPin,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
            .size(iconSize)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
    )
}

/**
 * Gliding diagonal shimmer sheen overlay for gradient pill CTA buttons.
 */
@Composable
fun ShimmerPillOverlay(
    modifier: Modifier = Modifier,
    shimmerColor: Color = Color.White.copy(alpha = 0.28f),
    durationMillis: Int = 2600
) {
    val transition = rememberInfiniteTransition(label = "shimmer_pill")
    val shimmerProgress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val bandWidth = width * 0.36f
        val startX = shimmerProgress * width
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    shimmerColor,
                    Color.Transparent
                ),
                start = Offset(startX - bandWidth, 0f),
                end = Offset(startX + bandWidth, height)
            )
        )
    }
}

/**
 * Gentle floating bob animation for empty-state illustrations.
 */
@Composable
fun FloatingMotionBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "floating_empty")
    val offsetY by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset_y"
    )
    val scale by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_scale"
    )

    Box(
        modifier = modifier.graphicsLayer {
            translationY = offsetY
            scaleX = scale
            scaleY = scale
        },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
