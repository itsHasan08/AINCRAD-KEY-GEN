package com.example.engine

import android.webkit.CookieManager
import android.webkit.WebView
import java.net.URLEncoder

/**
 * Aincrad Bypass Engine:
 * Implements silent background shortener bypass, automated clickers,
 * and DOM/cookie/storage key extraction without displaying any intrusive branding or extension text.
 */
object AincradExtensionEngine {

    const val HANDSHAKE_TOKEN = "3TMdueEaUw"
    const val BASE_SHORTENER_URL = "https://encurtarapido.com/"

    // Injected content script (silent, completely clean, no visible extension badges)
    val CONTENT_SCRIPT: String = """
        (function() {
            // Strip any intrusive third-party watermarks or branding
            try {
                var bad = document.querySelectorAll('#xenox-brand, #xenox-toast-lock, [id*="extension"], [class*="extension"]');
                bad.forEach(function(el) { el.remove(); });
            } catch(e) {}

            if (window.__AINCRAD_BYPASS_INITIALIZED__) {
                if (typeof window.__aincradScanKey === 'function') {
                    window.__aincradScanKey();
                }
                return;
            }
            window.__AINCRAD_BYPASS_INITIALIZED__ = true;

            var CONFIG = {
                TOKEN: '$HANDSHAKE_TOKEN',
                BASE_URL: '$BASE_SHORTENER_URL',
                CLICK_COOLDOWN: 1500
            };

            var CLICK_KEYWORDS = [
                "clique aqui para prosseguir",
                "clique aqui para continuar",
                "prosseguir",
                "continuar",
                "continue",
                "click here to continue",
                "click to continue",
                "get link",
                "obter link",
                "get key",
                "gerar key",
                "generate key",
                "claim",
                "claim key",
                "verify",
                "start season",
                "generate",
                "ir para o link",
                "acessar link"
            ];

            var FINAL_BUTTON_KEYWORDS = [
                "obter link",
                "get link",
                "acessar link",
                "download",
                "claim key",
                "get key",
                "prosseguir"
            ];

            var lastClickTime = 0;

            function simulateClick(el) {
                if (!el) return;
                try {
                    var rect = el.getBoundingClientRect();
                    var x = rect.left + rect.width / 2;
                    var y = rect.top + rect.height / 2;
                    var opts = { bubbles: true, cancelable: true, view: window, clientX: x, clientY: y };
                    el.dispatchEvent(new MouseEvent('mousedown', opts));
                    el.dispatchEvent(new MouseEvent('mouseup', opts));
                    el.dispatchEvent(new MouseEvent('click', opts));
                    if (typeof el.click === 'function') el.click();
                } catch(e) {
                    try { el.click(); } catch(_) {}
                }
            }

            // 1. Scan DOM, input fields, cookies, localStorage, and text for the original key
            window.__aincradScanKey = function() {
                var aincradRegex = /AINCRAD-[A-Za-z0-9_\-]+/i;
                var generalRegex = /(?:key|token|chave)\s*[:=]\s*([A-Za-z0-9_\-]{8,})/i;

                // Priority 1: Check URL query & hash
                try {
                    var href = window.location.href || '';
                    var mUrl = href.match(aincradRegex);
                    if (mUrl && window.AincradBridge) {
                        window.AincradBridge.onKeyExtracted(mUrl[0]);
                        return true;
                    }
                    var p = new URLSearchParams(window.location.search);
                    var kParam = p.get('key') || p.get('token') || p.get('k');
                    if (kParam && kParam.length >= 8 && window.AincradBridge) {
                        window.AincradBridge.onKeyExtracted(kParam);
                        return true;
                    }
                } catch(e) {}

                // Priority 2: Key input fields, readonly textboxes, code tags
                var selectors = [
                    '#key', '#keyValue', '#token', '#output', '#txtKey', '#myKey', '#generated_key',
                    'input[name="key"]', 'input[name="token"]', 'input[readonly]', '[data-clipboard-text]',
                    '.key', 'code', 'pre', '.alert-success', '.text-success', '.badge-success',
                    'input[type="text"]', 'textarea'
                ];
                for (var s = 0; s < selectors.length; s++) {
                    var items = document.querySelectorAll(selectors[s]);
                    for (var j = 0; j < items.length; j++) {
                        var it = items[j];
                        var v = (it.value || it.innerText || it.textContent || it.getAttribute('data-clipboard-text') || '').trim();
                        var mA = v.match(aincradRegex);
                        if (mA && window.AincradBridge) {
                            window.AincradBridge.onKeyExtracted(mA[0]);
                            return true;
                        }
                        var mG = v.match(generalRegex);
                        if (mG && mG[1] && window.AincradBridge) {
                            window.AincradBridge.onKeyExtracted(mG[1]);
                            return true;
                        }
                        // Direct value if input is dedicated key field and non-empty
                        if ((it.id === 'key' || it.id === 'keyValue' || it.name === 'key') && v.length >= 8 && window.AincradBridge) {
                            window.AincradBridge.onKeyExtracted(v);
                            return true;
                        }
                    }
                }

                // Priority 3: Body and generic elements
                var els = document.querySelectorAll('h1, h2, h3, h4, p, span, b, strong, div, a');
                for (var i = 0; i < els.length; i++) {
                    var text = (els[i].innerText || els[i].textContent || '').trim();
                    var matchA = text.match(aincradRegex);
                    if (matchA && window.AincradBridge) {
                        window.AincradBridge.onKeyExtracted(matchA[0]);
                        return true;
                    }
                    var matchG = text.match(generalRegex);
                    if (matchG && matchG[1] && window.AincradBridge) {
                        window.AincradBridge.onKeyExtracted(matchG[1]);
                        return true;
                    }
                }

                // Priority 4: Cookies
                try {
                    var cookieStr = document.cookie || '';
                    var mC = cookieStr.match(aincradRegex);
                    if (mC && window.AincradBridge) {
                        window.AincradBridge.onKeyExtracted(mC[0]);
                        return true;
                    }
                    var mCG = cookieStr.match(generalRegex);
                    if (mCG && mCG[1] && window.AincradBridge) {
                        window.AincradBridge.onKeyExtracted(mCG[1]);
                        return true;
                    }
                } catch(e) {}

                // Priority 5: localStorage & sessionStorage
                try {
                    for (var k = 0; k < localStorage.length; k++) {
                        var lk = localStorage.key(k);
                        var lv = localStorage.getItem(lk) || '';
                        var mLS = lv.match(aincradRegex);
                        if (mLS && window.AincradBridge) {
                            window.AincradBridge.onKeyExtracted(mLS[0]);
                            return true;
                        }
                    }
                } catch(e) {}

                return false;
            };

            // 2. Shortener handshake redirection
            function handleTokenHandshake() {
                if (window.location.hostname.indexOf('precisosaberinvestir.com.br') !== -1) {
                    try {
                        var p = new URLSearchParams(window.location.search);
                        var sf_v = p.get('sf_v') || p.get('id') || CONFIG.TOKEN;
                        var sf_ts = p.get('sf_ft_ts') || p.get('ts') || '';
                        var sf_sig = p.get('sf_sig') || p.get('sf_ft_sig');
                        if (sf_sig) {
                            window.location.replace(CONFIG.BASE_URL + sf_v + '?rtgok=1&sf_ft_ts=' + encodeURIComponent(sf_ts) + '&sf_ft_sig=' + encodeURIComponent(sf_sig));
                        }
                    } catch(e) {}
                }
            }

            // 3. Automation loop: countdown timers and continue clicks
            function automationLoop() {
                if (window.__aincradScanKey()) return;

                // Handle countdown timers if present (accelerate or watch)
                try {
                    if (typeof count !== 'undefined' && count > 0) count = 0;
                    if (typeof counter !== 'undefined' && counter > 0) counter = 0;
                } catch(e) {}

                // Check final buttons first (e.g. Obter link / Get link / Continue)
                var buttons = document.querySelectorAll('button, a, .btn, .button, input[type="submit"], input[type="button"]');
                for (var b = 0; b < buttons.length; b++) {
                    var btn = buttons[b];
                    var text = (btn.innerText || btn.textContent || btn.value || '').trim().toLowerCase();

                    // Check if final button is ready
                    for (var f = 0; f < FINAL_BUTTON_KEYWORDS.length; f++) {
                        var fk = FINAL_BUTTON_KEYWORDS[f];
                        if (text === fk || text.indexOf(fk) !== -1) {
                            var now = Date.now();
                            if (now - lastClickTime > CONFIG.CLICK_COOLDOWN) {
                                lastClickTime = now;
                                simulateClick(btn);
                                return;
                            }
                        }
                    }

                    // Check intermediate continue / proceed buttons
                    for (var k = 0; k < CLICK_KEYWORDS.length; k++) {
                        var kw = CLICK_KEYWORDS[k];
                        if (text === kw || text.indexOf(kw) !== -1) {
                            var now2 = Date.now();
                            if (now2 - lastClickTime > CONFIG.CLICK_COOLDOWN) {
                                lastClickTime = now2;
                                simulateClick(btn);
                                return;
                            }
                        }
                    }
                }
            }

            // 4. MutationObserver for instant key and DOM event detection
            try {
                var observer = new MutationObserver(function() {
                    window.__aincradScanKey();
                });
                observer.observe(document.documentElement || document.body, {
                    childList: true,
                    subtree: true,
                    attributes: true
                });
            } catch(e) {}

            // Initial execution
            handleTokenHandshake();
            window.__aincradScanKey();

            if (window.__aincradInterval) {
                clearInterval(window.__aincradInterval);
            }
            window.__aincradInterval = setInterval(automationLoop, 1000);
        })();
    """.trimIndent()

    // Script to forcefully trigger key extraction and immediate button clicks
    val FORCE_TRIGGER_SCRIPT: String = """
        (function() {
            if (typeof window.__aincradScanKey === 'function') {
                window.__aincradScanKey();
            }
            var btns = document.querySelectorAll('button, a, .btn, input[type="submit"], input[type="button"]');
            for (var i = 0; i < btns.length; i++) {
                var t = (btns[i].innerText || btns[i].textContent || btns[i].value || '').trim().toLowerCase();
                if (t.indexOf('get key') !== -1 || t.indexOf('gerar key') !== -1 || t.indexOf('generate key') !== -1 || t.indexOf('prosseguir') !== -1 || t.indexOf('obter link') !== -1 || t.indexOf('start season') !== -1) {
                    try { btns[i].click(); } catch(e) {}
                    break;
                }
            }
        })();
    """.trimIndent()

    /**
     * Rules filter:
     * Blocks intrusive ad popups and trackers so the portal session remains uninterrupted.
     */
    fun isAdOrTrackerUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("popads.net") ||
                lower.contains("adsterra.com") ||
                lower.contains("monetag.com") ||
                lower.contains("propellerads.com") ||
                lower.contains("googlesyndication.com/pagead") ||
                lower.contains("doubleclick.net") ||
                lower.contains("bet365") ||
                lower.contains("1xbet") ||
                lower.contains("onclick") ||
                lower.contains("exdynsrv.com")
    }

    /**
     * Fast-tracks the precisosaberinvestir token handshake directly into encurtarapido.
     */
    fun transformShortenerRedirect(url: String): String? {
        if (!url.contains("precisosaberinvestir.com.br")) return null
        return try {
            val uri = android.net.Uri.parse(url)
            val sfV = uri.getQueryParameter("sf_v") ?: uri.getQueryParameter("id") ?: HANDSHAKE_TOKEN
            val sfSig = uri.getQueryParameter("sf_sig") ?: uri.getQueryParameter("sf_ft_sig")
            val sfTs = uri.getQueryParameter("sf_ft_ts") ?: uri.getQueryParameter("ts") ?: ""
            if (!sfSig.isNullOrEmpty()) {
                "$BASE_SHORTENER_URL$sfV?rtgok=1&sf_ft_ts=${URLEncoder.encode(sfTs, "UTF-8")}&sf_ft_sig=${URLEncoder.encode(sfSig, "UTF-8")}"
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Injects the content script into the specified WebView.
     */
    fun injectExtension(webView: WebView?) {
        webView?.post {
            webView.evaluateJavascript(CONTENT_SCRIPT, null)
        }
    }

    /**
     * Forces immediate key scanning and automation triggers.
     */
    fun triggerActiveKeyExtraction(webView: WebView?) {
        webView?.post {
            val currentUrl = webView.url
            AincradKeyEngine.extractFromContent(currentUrl)

            try {
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookie(currentUrl ?: "https://aincradmods.com")
                AincradKeyEngine.extractFromContent(cookies)
            } catch (_: Exception) {}

            webView.evaluateJavascript(FORCE_TRIGGER_SCRIPT, null)
            webView.evaluateJavascript(CONTENT_SCRIPT, null)
        }
    }
}
