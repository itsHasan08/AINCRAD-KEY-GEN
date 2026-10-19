package com.example

import android.app.Application
import android.os.Build
import android.system.Os
import android.webkit.WebView
import java.io.File

class AincradApplication : Application() {

    companion object {
        init {
            try {
                // Tell Mesa / OpenGL loader to use software rasterizer directly
                // to prevent querying non-existent hardware GPU rendernode (/dev/dri/renderD*)
                Os.setenv("LIBGL_ALWAYS_SOFTWARE", "1", true)
                Os.setenv("MESA_LOADER_DRIVER_OVERRIDE", "swrast", true)
                Os.setenv("GALLIUM_DRIVER", "llvmpipe", true)
            } catch (_: Throwable) {
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            // Pre-clean any corrupt Chromium variations seed file to prevent "Seed missing signature"
            val webViewDir = File(dataDir, "app_webview")
            if (webViewDir.exists()) {
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
}
