package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.StatusGreenDot
import com.example.ui.theme.StatusGreenText
import kotlin.math.sin

/**
 * Centered, COMPLETELY BORDERLESS Status Indicator:
 * - Perfectly centered horizontally
 * - NO border, NO card container, NO pill outline
 * - Pulsing green dot with animated ripple ring
 * - "SYSTEM READY" bold green text
 * - 4 animated vertical green signal bars
 */
@Composable
fun StatusPanel(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_anim")

    // Ripple progress (0f to 1f)
    val ripple by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    // Dot pulse scale
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    // Signal bar cycle
    val signalTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "signal_time"
    )

    // Centered Row with NO BORDER and NO BACKGROUND
    Row(
        modifier = modifier
            .wrapContentWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .testTag("status_panel"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left: Pulsing Green Dot with expanding ripple ring
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f - 1f
                val baseRadius = 4.dp.toPx()

                // Expanding ripple ring
                val r = baseRadius + (maxRadius - baseRadius) * ripple
                val alpha = (1f - ripple).coerceIn(0f, 0.7f)
                drawCircle(
                    color = StatusGreenDot.copy(alpha = alpha),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Core glowing dot
                drawCircle(
                    color = StatusGreenDot.copy(alpha = 0.35f),
                    radius = baseRadius * dotPulse * 1.5f,
                    center = center
                )
                drawCircle(
                    color = StatusGreenDot,
                    radius = baseRadius * dotPulse,
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center: "SYSTEM READY" text in bold green (NO border around it)
        Text(
            text = stringResource(id = R.string.system_ready),
            fontFamily = RobotoMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            letterSpacing = 1.0.sp,
            color = StatusGreenText,
            modifier = Modifier.testTag("status_text")
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Right: 4 animated green signal bars
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            modifier = Modifier
                .height(16.dp)
                .testTag("signal_bars")
        ) {
            val baseHeights = listOf(6.dp, 9.dp, 12.dp, 15.dp)
            baseHeights.forEachIndexed { index, targetHeight ->
                val phase = signalTime + index * 0.9f
                val wave = (sin(phase.toDouble()).toFloat() + 1f) / 2f
                val currentFraction = 0.6f + 0.4f * wave
                val barHeight = targetHeight * currentFraction

                Box(
                    modifier = Modifier
                        .width(2.8.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(1.4.dp))
                        .background(StatusGreenDot.copy(alpha = 0.7f + 0.3f * wave))
                )
            }
        }
    }
}
