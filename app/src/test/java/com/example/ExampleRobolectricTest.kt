package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundFxManager
import com.example.audio.SoundType
import com.example.data.KeyRepository
import com.example.engine.AincradKeyEngine
import com.example.model.AincradKey
import com.example.ui.components.PORTAL_URL
import com.example.ui.components.prepareFreshDirectories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KEY GEN", appName)
    }

    @Test
    fun `verify all required button strings exist`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("PORTAL", context.getString(R.string.portal_btn))
        assertEquals("START SEASON", context.getString(R.string.start_season_btn))
        assertEquals("CLEAR DATA", context.getString(R.string.clear_data_btn))
        assertEquals("SYSTEM READY", context.getString(R.string.system_ready))
    }

    @Test
    fun `sound manager initializes and plays without error`() {
        val soundManager = SoundFxManager()
        assertNotNull(soundManager)
        soundManager.play(SoundType.HIGH_TICK)
        soundManager.play(SoundType.RISING_SWEET)
        soundManager.play(SoundType.HEAVY_THUD)
        soundManager.release()
    }

    @Test
    fun `verify portal url target is valid`() {
        assertEquals("https://aincradmods.com/getkey", PORTAL_URL)
    }

    @Test
    fun `prepare fresh directories creates cache hierarchy without throwing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prepareFreshDirectories(context)
        val jsCache = File(context.cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
        assertTrue("Expected js cache directory to exist", jsCache.exists())
    }

    @Test
    fun `aincrad key engine strictly extracts exact website key without modifying characters`() {
        AincradKeyEngine.clear()
        assertNull("Initially no website key is detected", AincradKeyEngine.getExtractedWebsiteKey())

        // Web extraction test preserving exact character case
        val content = "Congratulations! Here is your token: AINCRAD-MHG5-MIKA valid for 24 hours"
        val extracted = AincradKeyEngine.extractFromContent(content)
        assertEquals("AINCRAD-MHG5-MIKA", extracted)
        assertEquals("AINCRAD-MHG5-MIKA", AincradKeyEngine.getExtractedWebsiteKey())

        AincradKeyEngine.clear()
        assertNull(AincradKeyEngine.getExtractedWebsiteKey())
    }

    @Test
    fun `aincrad key model computes valid 24h expiration`() {
        val key = AincradKey(keyCode = "AINCRAD-MHG5-MIKA")
        assertEquals(24 * 60 * 60 * 1000L, key.expiresAt - key.createdAt)
        assertFalse(key.isExpired)
        assertTrue(key.remainingTimeText.contains("remaining"))
    }

    @Test
    fun `key repository persists and retrieves keys`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = KeyRepository(context)
        repo.clearAll()
        assertEquals(0, repo.getAllKeys().size)

        val key = AincradKey(keyCode = "AINCRAD-MHG5-MIKA")
        repo.saveKey(key)

        val retrieved = repo.getAllKeys()
        assertEquals(1, retrieved.size)
        assertEquals("AINCRAD-MHG5-MIKA", retrieved[0].keyCode)
        assertEquals(key.expiresAt, retrieved[0].expiresAt)

        repo.deleteKey(key.id)
        assertEquals(0, repo.getAllKeys().size)
    }

    @Test
    fun `aincrad extension engine ad filter detects trackers and allows genuine portal`() {
        assertTrue(com.example.engine.AincradExtensionEngine.isAdOrTrackerUrl("https://popads.net/serve.js"))
        assertTrue(com.example.engine.AincradExtensionEngine.isAdOrTrackerUrl("https://adsterra.com/banner.php"))
        assertFalse(com.example.engine.AincradExtensionEngine.isAdOrTrackerUrl("https://aincradmods.com/getkey"))
        assertFalse(com.example.engine.AincradExtensionEngine.isAdOrTrackerUrl("https://encurtarapido.com/3TMdueEaUw"))
    }

    @Test
    fun `aincrad extension engine transforms precisosaberinvestir token handshake`() {
        val testUrl = "https://precisosaberinvestir.com.br/token.php?sf_v=3TMdueEaUw&sf_sig=xyz123&ts=999"
        val redirected = com.example.engine.AincradExtensionEngine.transformShortenerRedirect(testUrl)
        assertNotNull(redirected)
        assertTrue(redirected!!.startsWith("https://encurtarapido.com/3TMdueEaUw"))
        assertTrue(redirected.contains("sf_ft_sig=xyz123"))
    }
}
