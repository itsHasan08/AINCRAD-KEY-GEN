package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.StatusGreenDot
import com.example.ui.theme.TitleDarkCharcoal

data class AppFutureFeature(
    val title: String,
    val description: String,
    val status: String,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * Support / Future Features Screen:
 * Focused purely on the upcoming features and capabilities of this application.
 */
@Composable
fun SupportScreen(
    modifier: Modifier = Modifier
) {
    val futureFeatures = listOf(
        AppFutureFeature(
            title = "One-Tap Instant Bypass 2.0",
            description = "সম্পূর্ণ ব্যাকগ্রাউন্ডে একক ক্লিকে পোর্টাল ও শর্টনার প্রসেস শেষ করে স্বয়ংক্রিয়ভাবে নতুন ২৪ ঘণ্টার Key সংগ্রহ।",
            status = "UPCOMING",
            icon = Icons.Default.AutoAwesome,
            accentColor = StatusGreenDot
        ),
        AppFutureFeature(
            title = "Direct In-App Activation",
            description = "সংগৃহীত Key কপি-পেস্ট ছাড়াই এক ক্লিকে সংশ্লিষ্ট গেম ও অ্যাপের সাথে অটোমেটিক সিঙ্ক ও অ্যাক্টিভেশন।",
            status = "PLANNED",
            icon = Icons.Default.VpnKey,
            accentColor = Color(0xFF0284C7)
        ),
        AppFutureFeature(
            title = "Cloud Key Backup & Sync",
            description = "ডিভাইস পরিবর্তন বা অ্যাপ রিস্টল করলেও পূর্বে সেভ করা সকল ভ্যালিড Key ক্লাউড থেকে রিস্টোর করার সুবিধা।",
            status = "IN PROGRESS",
            icon = Icons.Default.CloudDone,
            accentColor = Color(0xFF7C3AED)
        ),
        AppFutureFeature(
            title = "Smart 24-Hour Expiry Alert",
            description = "Key-এর ২৪ ঘণ্টার মেয়াদ শেষ হওয়ার ৩০ মিনিট আগে পুশ নোটিফিকেশন অ্যালার্ট ও অটো-রিনিউ অপশন।",
            status = "PLANNED",
            icon = Icons.Default.NotificationsActive,
            accentColor = Color(0xFFD97706)
        ),
        AppFutureFeature(
            title = "Multi-Server Speed Engine",
            description = "পোর্টাল জ্যাম বা ট্র্যাফিক থাকলে একাধিক অল্টারনেটিভ ফাস্ট নোড দিয়ে দ্রুততম সময়ে সংযোগ স্থাপন।",
            status = "RESEARCHING",
            icon = Icons.Default.Speed,
            accentColor = Color(0xFF059669)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6F4EA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Features",
                    tint = StatusGreenDot,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "APPLICATION FUTURE FEATURES",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    letterSpacing = 1.0.sp,
                    color = TitleDarkCharcoal
                )
                Text(
                    text = "Aincrad Key Gen আসন্ন ফিচার ও রোডম্যাপ",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feature Cards
        futureFeatures.forEach { feature ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(feature.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = null,
                                    tint = feature.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = feature.title,
                                fontFamily = OswaldFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = TitleDarkCharcoal
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(feature.accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = feature.status,
                                fontFamily = RobotoMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = feature.accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = feature.description,
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "AINCRAD KEY GEN • ROADMAP",
            fontFamily = RobotoMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
    }
}
