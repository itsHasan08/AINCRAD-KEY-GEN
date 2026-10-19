package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundType {
    HIGH_TICK,
    RISING_SWEET,
    HEAVY_THUD
}

class SoundFxManager {

    private var tickTrack: AudioTrack? = null
    private var sweetTrack: AudioTrack? = null
    private var thudTrack: AudioTrack? = null

    private val sampleRate = 44100

    init {
        try {
            tickTrack = createStaticTrack(generateHighTick())
            sweetTrack = createStaticTrack(generateRisingSweetTone())
            thudTrack = createStaticTrack(generateHeavyThud())
        } catch (e: Exception) {
            Log.e("SoundFxManager", "Failed to initialize audio tracks", e)
        }
    }

    fun play(type: SoundType) {
        try {
            val track = when (type) {
                SoundType.HIGH_TICK -> tickTrack
                SoundType.RISING_SWEET -> sweetTrack
                SoundType.HEAVY_THUD -> thudTrack
            } ?: return

            synchronized(track) {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            Log.e("SoundFxManager", "Error playing sound: $type", e)
        }
    }

    fun release() {
        try {
            tickTrack?.release()
            sweetTrack?.release()
            thudTrack?.release()
        } catch (e: Exception) {
            Log.e("SoundFxManager", "Error releasing tracks", e)
        }
    }

    private fun createStaticTrack(pcmData: ShortArray): AudioTrack {
        val bufferSizeBytes = pcmData.size * 2
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcmData, 0, pcmData.size)
        return audioTrack
    }

    /**
     * PORTAL click: Sharp high tick
     * A very snappy, high-frequency mechanical / digital switch tick (~35ms).
     */
    private fun generateHighTick(): ShortArray {
        val durationMs = 38
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // High pitch around 2400Hz decaying quickly
            val freq = 2400.0 + 800.0 * exp(-t * 220.0)
            val envelope = exp(-t * 130.0)
            // Transient click at the very start
            val transient = if (i < 40) (1.0 - i / 40.0) * 0.4 else 0.0
            val wave = sin(2.0 * PI * freq * t) * envelope + transient
            val sampleVal = (wave.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            buffer[i] = sampleVal
        }
        return buffer
    }

    /**
     * START SEASON click: Rising sweet tone
     * Melodic, harmonious chime sweeping from ~520Hz (C5) up to ~1040Hz (C6) with sweet harmonic.
     */
    private fun generateRisingSweetTone(): ShortArray {
        val durationMs = 170
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)

        var phase = 0.0
        val durationSec = durationMs / 1000.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = (t / durationSec).coerceIn(0.0, 1.0)
            // Exponential pitch sweep up from 520Hz to 1040Hz
            val freq = 520.0 + 520.0 * (progress * progress)
            phase += 2.0 * PI * freq / sampleRate

            // Envelope: gentle attack, then warm decay
            val attackSec = 0.015
            val attack = if (t < attackSec) t / attackSec else 1.0
            val decay = exp(-((t - attackSec).coerceAtLeast(0.0)) * 14.0)
            val envelope = attack * decay

            // Fundamental + sweet octave overtone
            val wave = (sin(phase) * 0.75 + sin(phase * 2.0) * 0.25) * envelope
            val sampleVal = (wave.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
            buffer[i] = sampleVal
        }
        return buffer
    }

    /**
     * CLEAR DATA click: Low heavy thud
     * Deep punchy low thump (120Hz down to 42Hz) with fast resonant decay.
     */
    private fun generateHeavyThud(): ShortArray {
        val durationMs = 150
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)

        var phase = 0.0
        val durationSec = durationMs / 1000.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = (t / durationSec).coerceIn(0.0, 1.0)
            // Pitch drop for heavy impact
            val freq = 120.0 - 75.0 * progress
            phase += 2.0 * PI * freq / sampleRate

            // Attack transient (first 3ms punch) and steep decay
            val attackSec = 0.004
            val attack = if (t < attackSec) t / attackSec else 1.0
            val decay = exp(-((t - attackSec).coerceAtLeast(0.0)) * 28.0)
            val envelope = attack * decay

            // Sub-bass thump with sub-octave warmth
            val wave = (sin(phase) * 0.8 + sin(phase * 0.5) * 0.3) * envelope
            val sampleVal = (wave.coerceIn(-1.0, 1.0) * 29000).toInt().toShort()
            buffer[i] = sampleVal
        }
        return buffer
    }
}
