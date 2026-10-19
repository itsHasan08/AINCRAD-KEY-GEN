package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.AincradExtensionEngine
import com.example.engine.AincradKeyEngine
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.StatusGreenDot
import com.example.ui.theme.StatusGreenText
import java.io.ByteArrayInputStream
import java.io.File

const val PORTAL_URL = "https://aincradmods.com/getkey"

/**
 * Prepares WebView cache directories to prevent Chromium SimpleCache indexer errors.
 */
fun prepareFreshDirectories(context: Context) {
    try {
        val baseCache = File(context.cacheDir, "WebView/Default/HTTP Cache")
        if (!baseCache.exists()) baseCache.mkdirs()
        val jsCache = File(baseCache, "Code Cache/js")
        if (!jsCache.exists()) jsCache.mkdirs()
        val wasmCache = File(baseCache, "Code Cache/wasm")
        if (!wasmCache.exists()) baseCache.resolve("Code Cache/wasm").mkdirs()
        val indexDir = File(baseCache, "index-dir")
        if (!indexDir.exists()) indexDir.mkdirs()

        // Clean any zero-byte or corrupt variations seed files to prevent Seed missing signature errors
        val webViewDir = File(context.dataDir, "app_webview")
        if (!webViewDir.exists()) {
            webViewDir.mkdirs()
        } else {
            val seed = File(webViewDir, "variations_seed")
            if (seed.exists() && (seed.length() == 0L || !seed.canRead())) {
                seed.delete()
            }
            val seedNew = File(webViewDir, "variations_seed_new")
            if (seedNew.exists() && (seedNew.length() == 0L || !seedNew.canRead())) {
                seedNew.delete()
            }
        }
    } catch (_: Exception) {
    }
}

/**
 * Embedded in-app WebView located directly BELOW the CLEAR DATA button.
 * Opens ONLY when the user clicks the PORTAL button.
 * Uses software rendering layer to prevent Chromium rendernode GPU crash on Linux container/emulator.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmbeddedPortalWebView(
    isOpen: Boolean,
    onClose: () -> Unit,
    onWebViewReady: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }

    BackHandler(enabled = isOpen) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onClose()
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        val containerShape = RoundedCornerShape(18.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(containerShape)
                .background(Color.White)
                .border(width = 1.5.dp, color = Color(0xFFE2E8F0), shape = containerShape)
                .testTag("embedded_portal_container")
        ) {
            // Header Bar directly below CLEAR DATA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Incognito shield badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE6F4EA))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Portal",
                        tint = StatusGreenDot,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PORTAL SESSION",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreenText,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // URL text indicator
                Text(
                    text = "aincradmods.com/getkey",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Refresh Button
                IconButton(
                    onClick = {
                        webViewInstance?.let { wv ->
                            prepareFreshDirectories(wv.context)
                            wv.reload()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Progress bar
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = StatusGreenDot,
                    trackColor = Color(0xFFE2E8F0)
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFFE2E8F0))
                )
            }

            // Embedded Android WebView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        prepareFreshDirectories(context)
                        WebView(context).apply {
                            // CRITICAL: Software layer prevents GPU rendernode missing errors and Chromium renderer crashes (-1)
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(false)
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                            }

                            // Add Javascript Interface for real-time key extraction from portal website
                            addJavascriptInterface(object {
                                @android.webkit.JavascriptInterface
                                fun onKeyExtracted(key: String) {
                                    if (key.isNotBlank()) {
                                        AincradKeyEngine.onKeyDetectedFromWebsite(key)
                                    }
                                }
                            }, "AincradBridge")

                            // Keep all navigation in-app with rules ad filtering and shortener handshake redirects
                            webViewClient = object : WebViewClient() {
                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    val url = request?.url?.toString() ?: ""
                                    // Block invasive popup ads and trackers
                                    if (AincradExtensionEngine.isAdOrTrackerUrl(url)) {
                                        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: ""
                                    // Fast-track shortener handshake redirect directly
                                    val shortcut = AincradExtensionEngine.transformShortenerRedirect(url)
                                    if (shortcut != null) {
                                        view?.loadUrl(shortcut)
                                        return true
                                    }
                                    return false
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    AincradExtensionEngine.injectExtension(view)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                    AincradExtensionEngine.injectExtension(view)
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    isLoading = false
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    // Safely remove and destroy the dead WebView to prevent infinite crash loops
                                    try {
                                        (view?.parent as? ViewGroup)?.removeView(view)
                                        view?.destroy()
                                    } catch (_: Exception) {}
                                    webViewInstance = null
                                    return true
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress / 100f
                                    if (newProgress >= 100) {
                                        isLoading = false
                                    }
                                    if (newProgress in 35..90) {
                                        AincradExtensionEngine.injectExtension(view)
                                    }
                                }
                            }

                            webViewInstance = this
                            onWebViewReady(this)

                            // Load portal when user opens PORTAL
                            loadUrl(PORTAL_URL)
                        }
                    },
                    update = {
                        // Managed by webViewInstance
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.stopLoading()
                webViewInstance?.destroy()
            } catch (_: Exception) {}
            webViewInstance = null
        }
    }
}
