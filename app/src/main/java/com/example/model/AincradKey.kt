package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AincradKey(
    val id: String = UUID.randomUUID().toString(),
    val keyCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = createdAt + 24 * 60 * 60 * 1000L, // Valid for exactly 24 hours
    val originUrl: String = "https://aincradmods.com/getkey",
    val cookiesPresent: Boolean = false
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt

    val formattedCreatedTime: String
        get() {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            return sdf.format(Date(createdAt))
        }

    val formattedExpiresTime: String
        get() {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            return sdf.format(Date(expiresAt))
        }

    val remainingTimeText: String
        get() {
            val diff = expiresAt - System.currentTimeMillis()
            if (diff <= 0) return "EXPIRED"
            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
            return "${hours}h ${minutes}m remaining"
        }
}
