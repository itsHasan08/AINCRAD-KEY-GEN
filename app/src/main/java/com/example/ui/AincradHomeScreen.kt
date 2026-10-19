package com.example.ui

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SoundFxManager
import com.example.audio.SoundType
import com.example.data.KeyRepository
import com.example.engine.AincradExtensionEngine
import com.example.engine.AincradKeyEngine
import com.example.model.AincradKey
import com.example.ui.components.AincradActionButton
import com.example.ui.components.AincradLogo
import com.example.ui.components.EmbeddedPortalWebView
import com.example.ui.components.PORTAL_URL
import com.example.ui.components.SeasonKeyLoadingDialog
import com.example.ui.components.SeasonKeyModal
import com.example.ui.components.StatusPanel
import com.example.ui.components.prepareFreshDirectories
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.theme.ClearDataBtnBg
import com.example.ui.theme.ClearDataBtnBorder
import com.example.ui.theme.ClearDataBtnText
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.PortalBtnBg
import com.example.ui.theme.PortalBtnBorder
import com.example.ui.theme.PortalBtnText
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.StartSeasonBtnBg
import com.example.ui.theme.StartSeasonBtnBorder
import com.example.ui.theme.StartSeasonBtnText
import com.example.ui.theme.StatusGreenDot
import com.example.ui.theme.StatusGreenText
import com.example.ui.theme.TitleDarkCharcoal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AincradNavTab {
    HOME,
    HISTORY,
    SUPPORT
}

/**
 * KEY GEN Main Screen:
 * - Pure white background (#FFFFFF) with dark/black system status icons.
 * - Bottom Navigation Bar (Home, History, Support).
 * - Clean top header: Red & Green dual-tone circular Key Gen logo on white, Title, and centered borderless "SYSTEM READY".
 * - Portal opens ONLY when user taps PORTAL.
 * - START SEASON activates bypass system ONLY if portal is open and verification is done.
 * - Real key extraction: Extracts the exact original key directly from the website without altering letters.
 * - Minimal SeasonKeyModal showing ONLY Key, COPY, and SAVE options on clean translucent surface (no black box).
 * - CLEAR DATA wipes cookies and cache, stops bypass, and closes portal.
 */
@Composable
fun AincradHomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { SoundFxManager() }
    val keyRepo = remember { KeyRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(AincradNavTab.HOME) }
    var isPortalOpen by remember { mutableStateOf(false) }
    var activeWebView by remember { mutableStateOf<WebView?>(null) }
    var statusNotification by remember { mutableStateOf<String?>(null) }

    // State for 2-Second Loading Animation
    var isExtractingKey by remember { mutableStateOf(false) }

    // State for Floating Key Modal (displays ONLY Key, Copy, Save on clean light surface)
    var activeKeyModalString by remember { mutableStateOf<String?>(null) }

    // Saved Keys List
    var savedKeysList by remember { mutableStateOf(keyRepo.getAllKeys()) }

    DisposableEffect(Unit) {
        AincradKeyEngine.onKeyExtractedCallback = { extractedKey ->
            coroutineScope.launch {
                AincradExtensionEngine.stopBypass(activeWebView)
                activeKeyModalString = extractedKey
                isExtractingKey = false
                statusNotification = "✓ ORIGINAL KEY EXTRACTED: $extractedKey"
            }
        }
        onDispose {
            soundManager.release()
            AincradKeyEngine.onKeyExtractedCallback = null
            AincradExtensionEngine.stopBypass(activeWebView)
        }
    }

    // Function to completely wipe all cookies, storage, and CLOSE webview
    fun wipeCookiesAndClosePortal() {
        try {
            AincradExtensionEngine.stopBypass(activeWebView)
            AincradKeyEngine.clear()
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies {
                cookieManager.removeSessionCookies {
                    cookieManager.flush()
                    WebStorage.getInstance().deleteAllData()

                    activeWebView?.post {
                        activeWebView?.clearCache(true)
                        activeWebView?.clearHistory()
                        activeWebView?.clearFormData()
                        activeWebView?.loadUrl("about:blank")
                    }

                    isPortalOpen = false
                    statusNotification = "DATA & COOKIES WIPED • PORTAL CLOSED"
                }
            }
        } catch (_: Exception) {
            isPortalOpen = false
            statusNotification = "DATA CLEARED • PORTAL CLOSED"
        }
    }

    // Trigger START SEASON
    // CRITICAL USER RULE: If Portal is not open, START SEASON does NOT work!
    fun onStartSeasonClicked() {
        if (!isPortalOpen || activeWebView == null) {
            soundManager.play(SoundType.HEAVY_THUD)
            statusNotification = "⚠️ অনুগ্রহ করে প্রথমে PORTAL ওপেন করে হিউম্যান ভেরিফিকেশন সম্পন্ন করুন"
            return
        }

        soundManager.play(SoundType.RISING_SWEET)
        isExtractingKey = true
        statusNotification = "বাইপাস সক্রিয় হয়েছে... কন্টিনিউ ও শর্টনার প্রসেস চলছে"

        // Activate the bypass on the currently open and verified portal
        AincradExtensionEngine.startBypass(activeWebView)

        coroutineScope.launch {
            delay(2000L)
            isExtractingKey = false
            val exactWebsiteKey = AincradKeyEngine.getExtractedWebsiteKey()
            if (exactWebsiteKey != null) {
                activeKeyModalString = exactWebsiteKey
            } else {
                statusNotification = "পোর্টাল বাইপাস প্রসেসিং চলছে... Key প্রস্তুত হলে ভেসে উঠবে"
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
        bottomBar = {
            // Bottom Navigation Bar with Home, History, and Support
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color(0xFFF1F5F9))
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 1: HOME
                NavigationBarItem(
                    selected = currentTab == AincradNavTab.HOME,
                    onClick = { currentTab = AincradNavTab.HOME },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Home",
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StatusGreenDot,
                        selectedTextColor = TitleDarkCharcoal,
                        indicatorColor = Color(0xFFE6F4EA),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                // Tab 2: HISTORY
                NavigationBarItem(
                    selected = currentTab == AincradNavTab.HISTORY,
                    onClick = {
                        savedKeysList = keyRepo.getAllKeys()
                        currentTab = AincradNavTab.HISTORY
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (savedKeysList.isNotEmpty()) {
                                    Badge(
                                        containerColor = StatusGreenDot,
                                        contentColor = Color.White
                                    ) {
                                        Text("${savedKeysList.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "History",
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StatusGreenDot,
                        selectedTextColor = TitleDarkCharcoal,
                        indicatorColor = Color(0xFFE6F4EA),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                // Tab 3: SUPPORT
                NavigationBarItem(
                    selected = currentTab == AincradNavTab.SUPPORT,
                    onClick = { currentTab = AincradNavTab.SUPPORT },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Support",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Support",
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StatusGreenDot,
                        selectedTextColor = TitleDarkCharcoal,
                        indicatorColor = Color(0xFFE6F4EA),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (currentTab) {
                AincradNavTab.HOME -> {
                    // Main Home View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 520.dp)
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // 1. TOP LOGO: Dual-tone Red & Green Cyber Emblem on White
                        AincradLogo(
                            logoSize = if (isPortalOpen) 72.dp else 110.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. TITLE: "KEY GEN"
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isPortalOpen) 22.sp else 28.sp,
                            letterSpacing = 2.0.sp,
                            color = TitleDarkCharcoal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("app_title")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 3. SYSTEM READY: Horizontally CENTERED, COMPLETELY BORDERLESS
                        StatusPanel(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 4. ACTION BUTTONS:
                        // Row 1: [PORTAL] and [START SEASON]
                        // Row 2: [CLEAR DATA]
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Button 1: PORTAL (Opens Webview below Clear Data)
                                AincradActionButton(
                                    text = stringResource(id = R.string.portal_btn),
                                    backgroundColor = PortalBtnBg,
                                    borderColor = PortalBtnBorder,
                                    textColor = PortalBtnText,
                                    height = 45.dp,
                                    modifier = Modifier.weight(1f),
                                    testTag = "portal_button",
                                    onClick = {
                                        soundManager.play(SoundType.HIGH_TICK)
                                        isPortalOpen = !isPortalOpen
                                        if (isPortalOpen) {
                                            activeWebView?.let { wv ->
                                                prepareFreshDirectories(context)
                                                if (wv.url.isNullOrBlank() || wv.url == "about:blank") {
                                                    wv.loadUrl(PORTAL_URL)
                                                }
                                            }
                                        } else {
                                            AincradExtensionEngine.stopBypass(activeWebView)
                                        }
                                    }
                                )

                                // Button 2: START SEASON (Runs bypass ONLY when portal is open)
                                AincradActionButton(
                                    text = stringResource(id = R.string.start_season_btn),
                                    backgroundColor = StartSeasonBtnBg,
                                    borderColor = StartSeasonBtnBorder,
                                    textColor = StartSeasonBtnText,
                                    height = 45.dp,
                                    modifier = Modifier.weight(1f),
                                    testTag = "start_season_button",
                                    onClick = {
                                        onStartSeasonClicked()
                                    }
                                )
                            }

                            // Button 3: CLEAR DATA (Wipes data, stops bypass, and closes portal)
                            AincradActionButton(
                                text = stringResource(id = R.string.clear_data_btn),
                                backgroundColor = ClearDataBtnBg,
                                borderColor = ClearDataBtnBorder,
                                textColor = ClearDataBtnText,
                                height = 45.dp,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "clear_data_button",
                                onClick = {
                                    soundManager.play(SoundType.HEAVY_THUD)
                                    wipeCookiesAndClosePortal()
                                }
                            )
                        }

                        // Status notification banner
                        AnimatedVisibility(
                            visible = statusNotification != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            statusNotification?.let { msg ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE6F4EA))
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = msg,
                                        fontFamily = RobotoMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusGreenText,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // 5. WEBVIEW SECTION: Embedded directly BELOW CLEAR DATA (ONLY when isPortalOpen is true)
                        if (isPortalOpen) {
                            Spacer(modifier = Modifier.height(8.dp))

                            EmbeddedPortalWebView(
                                isOpen = true,
                                onClose = {
                                    AincradExtensionEngine.stopBypass(activeWebView)
                                    isPortalOpen = false
                                },
                                onWebViewReady = { wv ->
                                    activeWebView = wv
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }

                AincradNavTab.HISTORY -> {
                    // Full History Screen
                    HistoryScreen(
                        keys = savedKeysList,
                        onDeleteKey = { id ->
                            keyRepo.deleteKey(id)
                            savedKeysList = keyRepo.getAllKeys()
                        },
                        onClearAll = {
                            keyRepo.clearAll()
                            savedKeysList = emptyList()
                        }
                    )
                }

                AincradNavTab.SUPPORT -> {
                    // Full Support Screen
                    SupportScreen()
                }
            }

            // High-Tech Dual-Ring Loading Animation Dialog
            if (isExtractingKey) {
                SeasonKeyLoadingDialog()
            }

            // Floating Season Key Modal (Clean translucent surface, strictly NO BLACK BOX)
            activeKeyModalString?.let { keyString ->
                SeasonKeyModal(
                    keyCode = keyString,
                    onDismiss = { activeKeyModalString = null },
                    onSaveKey = { savedKeyStr ->
                        keyRepo.saveKey(
                            AincradKey(
                                keyCode = savedKeyStr,
                                createdAt = System.currentTimeMillis(),
                                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
                            )
                        )
                        savedKeysList = keyRepo.getAllKeys()
                    },
                    isAlreadySaved = savedKeysList.any { it.keyCode == keyString }
                )
            }
        }
    }
}
