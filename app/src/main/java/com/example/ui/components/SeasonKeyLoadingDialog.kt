package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TitleDarkCharcoal

/**
 * Premium KEY GEN Loading Dialog:
 * Sleek, high-tech animated dual-ring loader in Emerald Green and Ruby Red,
 * perfectly harmonizing with the pure white design aesthetic.
 */
@Composable
fun SeasonKeyLoadingDialog() {
    val infiniteTransition = rememberInfiniteTransition(label = "keygen_loader")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = { /* Modal persists during active bypass */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF10B981), Color(0xFFEF4444))
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(28.dp)
                .testTag("season_key_loading_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Dual-Ring Concentric Cyber Energy Spinner
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Emerald Green Ring
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(72.dp)
                            .rotate(outerRotation),
                        color = Color(0xFF10B981),
                        strokeWidth = 3.5.dp,
                        trackColor = Color(0xFFD1FAE5)
                    )

                    // Inner Ruby Red Ring
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .rotate(innerRotation),
                        color = Color(0xFFEF4444),
                        strokeWidth = 3.dp,
                        trackColor = Color(0xFFFEE2E2)
                    )

                    // Breathing Center Core
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFFEF4444))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "KEY GEN ACTIVE BYPASS",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    letterSpacing = 1.8.sp,
                    color = TitleDarkCharcoal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BYPASSING SHORTENER & EXTRACTING...",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
