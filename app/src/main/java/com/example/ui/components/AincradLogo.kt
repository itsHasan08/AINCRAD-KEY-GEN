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
import com.example.ui.theme.LogoCharcoalDark
import com.example.ui.theme.LogoCharcoalLight
import com.example.ui.theme.LogoIceHighlight
import com.example.ui.theme.LogoIceShadow
import com.example.ui.theme.LogoIceSilver

/**
 * High-quality circular Aincrad logo matching the exact screenshot:
 * Charcoal/black circular disc with an ice-silver/blue stylized floating fortress "A"
 * with bridge towers and inverted citadel foundation.
 */
@Composable
fun AincradLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 115.dp
) {
    Box(
        modifier = modifier
            .size(logoSize)
            .testTag("aincrad_logo"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(logoSize)) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val center = Offset(canvasW / 2f, canvasH / 2f)
            val outerRadius = canvasW / 2f - 3f

            // 1. Dark charcoal/black disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LogoCharcoalLight,
                        LogoCharcoalDark,
                        Color(0xFF08090C)
                    ),
                    center = Offset(canvasW * 0.48f, canvasH * 0.42f),
                    radius = outerRadius * 1.05f
                ),
                radius = outerRadius,
                center = center
            )

            // 2. Fine inner metallic ring
            drawCircle(
                color = Color(0x44B4E1F2),
                radius = outerRadius * 0.88f,
                center = center,
                style = Stroke(width = 1.2f)
            )

            val iceBrush = Brush.linearGradient(
                colors = listOf(
                    LogoIceHighlight,
                    LogoIceSilver,
                    LogoIceShadow
                ),
                start = Offset(canvasW * 0.3f, canvasH * 0.2f),
                end = Offset(canvasW * 0.7f, canvasH * 0.8f)
            )

            val iceHighlightBrush = Brush.linearGradient(
                colors = listOf(Color.White, LogoIceHighlight, LogoIceSilver),
                start = Offset(canvasW * 0.45f, canvasH * 0.15f),
                end = Offset(canvasW * 0.55f, canvasH * 0.55f)
            )

            val cx = canvasW / 2f

            // 3. Top Spire / Apex Needle
            val spirePath = Path().apply {
                moveTo(cx, canvasH * 0.17f)
                lineTo(cx - canvasW * 0.035f, canvasH * 0.26f)
                lineTo(cx + canvasW * 0.035f, canvasH * 0.26f)
                close()
            }
            drawPath(spirePath, brush = iceHighlightBrush)

            // 4. Stepped upper pyramid crown of the "A"
            val crownPath = Path().apply {
                moveTo(cx - canvasW * 0.055f, canvasH * 0.27f)
                lineTo(cx + canvasW * 0.055f, canvasH * 0.27f)
                lineTo(cx + canvasW * 0.09f, canvasH * 0.34f)
                lineTo(cx - canvasW * 0.09f, canvasH * 0.34f)
                close()
            }
            drawPath(crownPath, brush = iceBrush)

            // 5. Left and Right Fortress Flanks (Legs of the "A")
            val leftLeg = Path().apply {
                moveTo(cx - canvasW * 0.08f, canvasH * 0.35f)
                lineTo(cx - canvasW * 0.04f, canvasH * 0.47f)
                lineTo(cx - canvasW * 0.12f, canvasH * 0.47f)
                lineTo(cx - canvasW * 0.18f, canvasH * 0.61f)
                lineTo(cx - canvasW * 0.26f, canvasH * 0.61f)
                lineTo(cx - canvasW * 0.22f, canvasH * 0.53f)
                lineTo(cx - canvasW * 0.17f, canvasH * 0.53f)
                lineTo(cx - canvasW * 0.14f, canvasH * 0.43f)
                lineTo(cx - canvasW * 0.10f, canvasH * 0.43f)
                lineTo(cx - canvasW * 0.09f, canvasH * 0.35f)
                close()
            }
            drawPath(leftLeg, brush = iceHighlightBrush)

            val rightLeg = Path().apply {
                moveTo(cx + canvasW * 0.08f, canvasH * 0.35f)
                lineTo(cx + canvasW * 0.04f, canvasH * 0.47f)
                lineTo(cx + canvasW * 0.12f, canvasH * 0.47f)
                lineTo(cx + canvasW * 0.18f, canvasH * 0.61f)
                lineTo(cx + canvasW * 0.26f, canvasH * 0.61f)
                lineTo(cx + canvasW * 0.22f, canvasH * 0.53f)
                lineTo(cx + canvasW * 0.17f, canvasH * 0.53f)
                lineTo(cx + canvasW * 0.14f, canvasH * 0.43f)
                lineTo(cx + canvasW * 0.10f, canvasH * 0.43f)
                lineTo(cx + canvasW * 0.09f, canvasH * 0.35f)
                close()
            }
            drawPath(rightLeg, brush = iceBrush)

            // 6. Horizontal Fortress Bridge (Crossbar of the "A")
            val bridgePath = Path().apply {
                moveTo(cx - canvasW * 0.34f, canvasH * 0.485f)
                lineTo(cx + canvasW * 0.34f, canvasH * 0.485f)
                lineTo(cx + canvasW * 0.34f, canvasH * 0.515f)
                lineTo(cx - canvasW * 0.34f, canvasH * 0.515f)
                close()
            }
            drawPath(bridgePath, brush = iceHighlightBrush)

            // Twin Guard Turrets / Spires on Bridge Flanks
            drawLine(
                brush = iceHighlightBrush,
                start = Offset(cx - canvasW * 0.27f, canvasH * 0.42f),
                end = Offset(cx - canvasW * 0.27f, canvasH * 0.57f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                brush = iceHighlightBrush,
                start = Offset(cx + canvasW * 0.27f, canvasH * 0.42f),
                end = Offset(cx + canvasW * 0.27f, canvasH * 0.57f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // 7. Lower Inverted Citadel Tiers (Floating fortress keel)
            val underTier1 = Path().apply {
                moveTo(cx - canvasW * 0.20f, canvasH * 0.565f)
                lineTo(cx + canvasW * 0.20f, canvasH * 0.565f)
                lineTo(cx + canvasW * 0.17f, canvasH * 0.61f)
                lineTo(cx - canvasW * 0.17f, canvasH * 0.61f)
                close()
            }
            drawPath(underTier1, brush = iceHighlightBrush)

            val underTier2 = Path().apply {
                moveTo(cx - canvasW * 0.14f, canvasH * 0.625f)
                lineTo(cx + canvasW * 0.14f, canvasH * 0.625f)
                lineTo(cx + canvasW * 0.10f, canvasH * 0.665f)
                lineTo(cx - canvasW * 0.10f, canvasH * 0.665f)
                close()
            }
            drawPath(underTier2, brush = iceBrush)

            val underTip = Path().apply {
                moveTo(cx - canvasW * 0.035f, canvasH * 0.735f)
                lineTo(cx + canvasW * 0.035f, canvasH * 0.735f)
                lineTo(cx, canvasH * 0.79f)
                close()
            }
            drawPath(underTip, brush = iceBrush)

            // Fine edge outlines
            val fineStroke = Stroke(width = 1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawPath(leftLeg, color = Color(0x33FFFFFF), style = fineStroke)
            drawPath(rightLeg, color = Color(0x33FFFFFF), style = fineStroke)
            drawPath(crownPath, color = Color(0x33FFFFFF), style = fineStroke)
            drawPath(underTier1, color = Color(0x33FFFFFF), style = fineStroke)
        }
    }
}
