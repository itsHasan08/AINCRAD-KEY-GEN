package com.example.engine

import java.util.regex.Pattern

object AincradKeyEngine {
    // Regex pattern matching Aincrad keys from the portal website
    private val KEY_PATTERN = Pattern.compile("AINCRAD-[A-Za-z0-9_\\-]+", Pattern.CASE_INSENSITIVE)
    private val GENERAL_KEY_PATTERN = Pattern.compile("(?:key|token|chave)\\s*[:=]\\s*([A-Za-z0-9_\\-]{8,})", Pattern.CASE_INSENSITIVE)

    @Volatile
    var liveWebsiteDetectedKey: String? = null
        private set

    @Volatile
    var onKeyExtractedCallback: ((String) -> Unit)? = null

    /**
     * Stores the exact key detected from the live portal website without altering any characters.
     * Never invents or generates mock keys.
     */
    fun onKeyDetectedFromWebsite(rawKey: String) {
        val trimmed = rawKey.trim()
        if (trimmed.isNotBlank()) {
            val extracted = cleanExtractedKey(trimmed)
            if (extracted.isNotBlank()) {
                liveWebsiteDetectedKey = extracted
                onKeyExtractedCallback?.invoke(extracted)
            }
        }
    }

    private fun cleanExtractedKey(raw: String): String {
        val matcher = KEY_PATTERN.matcher(raw)
        if (matcher.find()) {
            return matcher.group()
        }
        val generalMatcher = GENERAL_KEY_PATTERN.matcher(raw)
        if (generalMatcher.find()) {
            val group = generalMatcher.group(1)
            if (!group.isNullOrBlank()) return group
        }
        return raw.trim()
    }

    /**
     * Inspects text content, DOM elements, or raw cookies to extract the exact key from the website.
     * Preserves exact characters without any modification.
     */
    fun extractFromContent(content: String?): String? {
        if (content.isNullOrBlank()) return null
        val matcher = KEY_PATTERN.matcher(content)
        return if (matcher.find()) {
            val exactKey = matcher.group()
            liveWebsiteDetectedKey = exactKey
            exactKey
        } else {
            val generalMatcher = GENERAL_KEY_PATTERN.matcher(content)
            if (generalMatcher.find()) {
                val group = generalMatcher.group(1)
                if (!group.isNullOrBlank()) {
                    liveWebsiteDetectedKey = group
                    group
                } else null
            } else null
        }
    }

    /**
     * Returns the exact key extracted directly from the website.
     * Returns null if the website has not yet generated or displayed a key.
     */
    fun getExtractedWebsiteKey(): String? {
        return liveWebsiteDetectedKey
    }

    /**
     * Clears the currently cached website key (e.g. on Clear Data).
     */
    fun clear() {
        liveWebsiteDetectedKey = null
    }
}
