package com.example.ui

import android.view.View
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.example.ui.components.NoKeyFoundDialog
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
import java.io.ByteArrayInputStream

enum class AincradNavTab {
    HOME,
    HISTORY,
    SUPPORT
}

/**
 * Aincrad Key Gen Main Screen:
 * - Pure white background (#FFFFFF) with dark/black system status icons.
 * - Bottom Navigation Bar (Home, History, Support).
 * - Clean top header: Circular Aincrad logo, Title, and centered borderless "SYSTEM READY".
 * - Website NEVER opens automatically on app launch. Opens ONLY when user taps PORTAL.
 * - START SEASON activates bypass system without opening/closing the UI repeatedly.
 * - Real key extraction: Extracts the exact original key directly from the website without altering letters.
 * - Minimal SeasonKeyModal showing ONLY Key, COPY, and SAVE options.
 * - CLEAR DATA wipes cookies and cache, and closes portal without opening website.
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
    var backgroundWebView by remember { mutableStateOf<WebView?>(null) }
    var statusNotification by remember { mutableStateOf<String?>(null) }

    // State for 2-Second Loading Animation
    var isExtractingKey by remember { mutableStateOf(false) }

    // State for Floating Key Modal (displays ONLY Key, Copy, Save)
    var activeKeyModalString by remember { mutableStateOf<String?>(null) }

    // State for Alert when no key is on the website yet
    var showNoKeyFoundDialog by remember { mutableStateOf(false) }

    // Saved Keys List
    var savedKeysList by remember { mutableStateOf(keyRepo.getAllKeys()) }

    DisposableEffect(Unit) {
        AincradKeyEngine.onKeyExtractedCallback = { extractedKey ->
            coroutineScope.launch {
                activeKeyModalString = extractedKey
                isExtractingKey = false
                showNoKeyFoundDialog = false
                statusNotification = "✓ ORIGINAL KEY EXTRACTED: $extractedKey"
            }
        }
        onDispose {
            soundManager.release()
            AincradKeyEngine.onKeyExtractedCallback = null
            try {
                backgroundWebView?.destroy()
                backgroundWebView = null
            } catch (_: Exception) {}
        }
    }

    // Starts background bypass session if portal is not visually open
    fun startBackgroundBypass() {
        if (backgroundWebView == null) {
            try {
                prepareFreshDirectories(context)
                val bgWv = WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        setSupportMultipleWindows(false)
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                    }
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun onKeyExtracted(key: String) {
                            if (key.isNotBlank()) {
                                AincradKeyEngine.onKeyDetectedFromWebsite(key)
                            }
                        }
                    }, "AincradBridge")
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                            val url = request?.url?.toString() ?: ""
                            if (AincradExtensionEngine.isAdOrTrackerUrl(url)) {
                                return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url?.toString() ?: ""
                            val shortcut = AincradExtensionEngine.transformShortenerRedirect(url)
                            if (shortcut != null) {
                                view?.loadUrl(shortcut)
                                return true
                            }
                            return false
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            AincradExtensionEngine.injectExtension(view)
                        }
                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                            try { view?.destroy() } catch (_: Exception) {}
                            backgroundWebView = null
                            return true
                        }
                    }
                    loadUrl(PORTAL_URL)
                }
                backgroundWebView = bgWv
            } catch (_: Exception) {}
        } else {
            AincradExtensionEngine.triggerActiveKeyExtraction(backgroundWebView)
        }
    }

    // Function to completely wipe all cookies, storage, and CLOSE webview (website will NOT reload/open)
    fun wipeCookiesAndClosePortal() {
        try {
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

                    try {
                        backgroundWebView?.destroy()
                        backgroundWebView = null
                    } catch (_: Exception) {}

                    // Explicit requirement: Clear data must NOT open or reload the website
                    isPortalOpen = false
                    statusNotification = "DATA & COOKIES WIPED • PORTAL CLOSED"
                }
            }
        } catch (_: Exception) {
            isPortalOpen = false
            statusNotification = "DATA CLEARED • PORTAL CLOSED"
        }
    }

    // Trigger START SEASON with 2-second loading animation and active key extraction
    fun onStartSeasonClicked() {
        soundManager.play(SoundType.RISING_SWEET)
        isExtractingKey = true

        // Requirement: Clicking START SEASON must NOT pop open or close the UI portal repeatedly
        if (activeWebView != null && isPortalOpen) {
            AincradExtensionEngine.triggerActiveKeyExtraction(activeWebView)
        } else {
            startBackgroundBypass()
        }

        // 2-second loading animation before checking extracted key
        coroutineScope.launch {
            delay(2000L)
            isExtractingKey = false
            val exactWebsiteKey = AincradKeyEngine.getExtractedWebsiteKey()
            if (exactWebsiteKey != null) {
                activeKeyModalString = exactWebsiteKey
                showNoKeyFoundDialog = false
            } else {
                statusNotification = "পোর্টাল বাইপাস প্রসেসিং চলছে... Key প্রস্তুত হলে ভেসে উঠবে"
                if (activeWebView != null && isPortalOpen) {
                    AincradExtensionEngine.triggerActiveKeyExtraction(activeWebView)
                } else {
                    startBackgroundBypass()
                }
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
                        // 1. TOP LOGO: Circular Aincrad fortress logo
                        AincradLogo(
                            logoSize = if (isPortalOpen) 76.dp else 115.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. TITLE: "AINCRAD KEY GEN"
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
                                // Button 1: PORTAL (Opens Webview below Clear Data ONLY on click)
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
                                        }
                                    }
                                )

                                // Button 2: START SEASON (2-second loading animation then pops up exact website key)
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

                            // Button 3: CLEAR DATA (Wipes data and closes website without opening it)
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
                                onClose = { isPortalOpen = false },
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

            // 2-Second Loading Animation Dialog
            if (isExtractingKey) {
                SeasonKeyLoadingDialog()
            }

            // Floating Season Key Modal (Strictly ONLY Key, COPY, and SAVE options)
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

            // Alert Dialog when no key has appeared on the website yet
            if (showNoKeyFoundDialog) {
                NoKeyFoundDialog(
                    onDismiss = { showNoKeyFoundDialog = false },
                    onOpenPortal = {
                        showNoKeyFoundDialog = false
                        currentTab = AincradNavTab.HOME
                        isPortalOpen = true
                    }
                )
            }
        }
    }
}
