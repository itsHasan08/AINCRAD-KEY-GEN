package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern KEY GEN Logo:
 * Pure white background base with vibrant Emerald Green and Crimson Ruby Red
 * dual-tone cyber key crest, designed specifically for a clean white aesthetic.
 */
@Composable
fun AincradLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 115.dp
) {
    Box(
        modifier = modifier
            .size(logoSize)
            .testTag("keygen_logo"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(logoSize)) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val center = Offset(canvasW / 2f, canvasH / 2f)
            val outerRadius = canvasW / 2f - 4f

            // 1. Pure White Base Circle with subtle drop-shadow ring
            drawCircle(
                color = Color(0xFFF1F5F9),
                radius = outerRadius + 2f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = outerRadius,
                center = center
            )

            // 2. Dual-Tone Outer Ring (Left Emerald Green, Right Ruby Red)
            val greenGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF10B981),
                    Color(0xFF059669),
                    Color(0xFF047857),
                    Color(0xFF10B981)
                ),
                center = center
            )
            val redGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFEF4444),
                    Color(0xFFDC2626),
                    Color(0xFFB91C1C),
                    Color(0xFFEF4444)
                ),
                center = center
            )

            // Outer crisp boundary rings
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFFEF4444)),
                    start = Offset(0f, canvasH / 2f),
                    end = Offset(canvasW, canvasH / 2f)
                ),
                radius = outerRadius * 0.94f,
                center = center,
                style = Stroke(width = 3.2f)
            )

            // Subtle inner ring
            drawCircle(
                color = Color(0xFFF1F5F9),
                radius = outerRadius * 0.84f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            val cx = canvasW / 2f
            val cy = canvasH / 2f

            // 3. Left Half - Emerald Green Cyber Key Wing
            val leftWing = Path().apply {
                moveTo(cx - 3f, canvasH * 0.18f)
                lineTo(cx - canvasW * 0.16f, canvasH * 0.32f)
                lineTo(cx - canvasW * 0.08f, canvasH * 0.44f)
                lineTo(cx - canvasW * 0.26f, canvasH * 0.44f)
                lineTo(cx - canvasW * 0.18f, canvasH * 0.62f)
                lineTo(cx - canvasW * 0.08f, canvasH * 0.62f)
                lineTo(cx - 3f, canvasH * 0.82f)
                lineTo(cx - 3f, canvasH * 0.70f)
                lineTo(cx - canvasW * 0.07f, canvasH * 0.54f)
                lineTo(cx - 3f, canvasH * 0.54f)
                close()
            }
            drawPath(
                path = leftWing,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF047857)),
                    startY = canvasH * 0.18f,
                    endY = canvasH * 0.82f
                )
            )

            // 4. Right Half - Ruby Red Cyber Key Wing
            val rightWing = Path().apply {
                moveTo(cx + 3f, canvasH * 0.18f)
                lineTo(cx + canvasW * 0.16f, canvasH * 0.32f)
                lineTo(cx + canvasW * 0.08f, canvasH * 0.44f)
                lineTo(cx + canvasW * 0.26f, canvasH * 0.44f)
                lineTo(cx + canvasW * 0.18f, canvasH * 0.62f)
                lineTo(cx + canvasW * 0.08f, canvasH * 0.62f)
                lineTo(cx + 3f, canvasH * 0.82f)
                lineTo(cx + 3f, canvasH * 0.70f)
                lineTo(cx + canvasW * 0.07f, canvasH * 0.54f)
                lineTo(cx + 3f, canvasH * 0.54f)
                close()
            }
            drawPath(
                path = rightWing,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF87171), Color(0xFFEF4444), Color(0xFFB91C1C)),
                    startY = canvasH * 0.18f,
                    endY = canvasH * 0.82f
                )
            )

            // 5. Central Key Shaft & Core Diamond
            val coreDiamond = Path().apply {
                moveTo(cx, cy - canvasH * 0.10f)
                lineTo(cx + canvasW * 0.08f, cy)
                lineTo(cx, cy + canvasH * 0.10f)
                lineTo(cx - canvasW * 0.08f, cy)
                close()
            }
            drawPath(coreDiamond, color = Color.White)
            drawPath(
                coreDiamond,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFFEF4444)),
                    start = Offset(cx - canvasW * 0.08f, cy),
                    end = Offset(cx + canvasW * 0.08f, cy)
                ),
                style = Stroke(width = 2.5f, join = StrokeJoin.Round)
            )

            // Core center glowing energy bead
            drawCircle(
                color = Color(0xFF0F172A),
                radius = canvasW * 0.032f,
                center = center
            )
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = canvasW * 0.018f,
                center = center
            )

            // Top Crown Spire (Diamond Needle)
            val spire = Path().apply {
                moveTo(cx, canvasH * 0.11f)
                lineTo(cx - canvasW * 0.035f, canvasH * 0.19f)
                lineTo(cx + canvasW * 0.035f, canvasH * 0.19f)
                close()
            }
            drawPath(
                path = spire,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFFEF4444))
                )
            )

            // Outline highlights for depth
            drawPath(
                leftWing,
                color = Color(0x33000000),
                style = Stroke(width = 1f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )
            drawPath(
                rightWing,
                color = Color(0x33000000),
                style = Stroke(width = 1f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )
        }
    }
}
