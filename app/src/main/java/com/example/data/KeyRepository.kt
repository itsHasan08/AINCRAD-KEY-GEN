package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AincradKey
import org.json.JSONArray
import org.json.JSONObject

class KeyRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aincrad_keys_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LIST = "saved_keys_json"
    }

    fun getAllKeys(): List<AincradKey> {
        val jsonStr = prefs.getString(KEY_LIST, null) ?: return emptyList()
        val list = mutableListOf<AincradKey>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AincradKey(
                        id = obj.optString("id"),
                        keyCode = obj.optString("keyCode"),
                        createdAt = obj.optLong("createdAt"),
                        expiresAt = obj.optLong("expiresAt"),
                        originUrl = obj.optString("originUrl"),
                        cookiesPresent = obj.optBoolean("cookiesPresent")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list.sortedByDescending { it.createdAt }
    }

    fun saveKey(key: AincradKey) {
        val current = getAllKeys().toMutableList()
        // If already exists, do not duplicate
        if (current.none { it.keyCode == key.keyCode }) {
            current.add(0, key)
            persist(current)
        }
    }

    fun deleteKey(id: String) {
        val updated = getAllKeys().filterNot { it.id == id }
        persist(updated)
    }

    fun clearAll() {
        prefs.edit().remove(KEY_LIST).apply()
    }

    private fun persist(keys: List<AincradKey>) {
        val jsonArray = JSONArray()
        keys.forEach { key ->
            val obj = JSONObject().apply {
                put("id", key.id)
                put("keyCode", key.keyCode)
                put("createdAt", key.createdAt)
                put("expiresAt", key.expiresAt)
                put("originUrl", key.originUrl)
                put("cookiesPresent", key.cookiesPresent)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_LIST, jsonArray.toString()).apply()
    }
}
