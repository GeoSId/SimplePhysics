package com.geosid.simplephysics.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

actual class AcousticTonePlayer actual constructor() {
    @Volatile private var _isPlaying: Boolean = false
    actual val isPlaying: Boolean get() = _isPlaying

    @Volatile private var f1: Float = 440.0f
    @Volatile private var f2: Float = 442.0f
    @Volatile private var amp1: Float = 1.0f
    @Volatile private var amp2: Float = 1.0f
    @Volatile private var masterVolume: Float = 0.25f
    @Volatile private var isBinaural: Boolean = false

    private var audioThread: Thread? = null
    private var track: AudioTrack? = null

    private val sampleRate = 44100
    private var phase1 = 0.0
    private var phase2 = 0.0

    actual fun play() {
        if (_isPlaying) return
        _isPlaying = true

        audioThread = thread(name = "AcousticBeatsAudio-Android", isDaemon = true) {
            try {
                val channelMask = AudioFormat.CHANNEL_OUT_STEREO
                val encoding = AudioFormat.ENCODING_PCM_16BIT
                val minBuf = AudioTrack.getMinBufferSize(sampleRate, channelMask, encoding)
                val bufferFrames = 1024
                val bufferSizeBytes = maxOf(minBuf, bufferFrames * 4)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelMask)
                    .setEncoding(encoding)
                    .build()

                val newTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                newTrack.play()
                track = newTrack

                val shortBuffer = ShortArray(bufferFrames * 2)
                val twoPi = 2.0 * PI

                while (_isPlaying) {
                    val curF1 = f1.toDouble()
                    val curF2 = f2.toDouble()
                    val curA1 = amp1.toDouble()
                    val curA2 = amp2.toDouble()
                    val curVol = masterVolume.toDouble()
                    val curBinaural = isBinaural

                    val deltaPhase1 = (twoPi * curF1) / sampleRate
                    val deltaPhase2 = (twoPi * curF2) / sampleRate

                    var shortIdx = 0
                    for (frame in 0 until bufferFrames) {
                        phase1 += deltaPhase1
                        if (phase1 >= twoPi) phase1 -= twoPi

                        phase2 += deltaPhase2
                        if (phase2 >= twoPi) phase2 -= twoPi

                        val s1 = sin(phase1) * curA1
                        val s2 = sin(phase2) * curA2

                        val leftSample: Double
                        val rightSample: Double

                        if (curBinaural) {
                            leftSample = s1 * curVol
                            rightSample = s2 * curVol
                        } else {
                            val sum = (s1 + s2) * 0.5 * curVol
                            leftSample = sum
                            rightSample = sum
                        }

                        val leftShort = (leftSample * 32767.0).toInt().coerceIn(-32768, 32767)
                        val rightShort = (rightSample * 32767.0).toInt().coerceIn(-32768, 32767)

                        shortBuffer[shortIdx++] = leftShort.toShort()
                        shortBuffer[shortIdx++] = rightShort.toShort()
                    }

                    newTrack.write(shortBuffer, 0, shortBuffer.size)
                }
            } catch (_: Exception) {
                // Audio interruption
            } finally {
                try {
                    track?.stop()
                    track?.flush()
                    track?.release()
                } catch (_: Exception) {}
                track = null
            }
        }
    }

    actual fun pause() {
        if (!_isPlaying) return
        _isPlaying = false
        try {
            audioThread?.join(200)
        } catch (_: Exception) {}
        audioThread = null
    }

    actual fun update(
        f1: Float,
        f2: Float,
        amp1: Float,
        amp2: Float,
        masterVolume: Float,
        isBinaural: Boolean
    ) {
        this.f1 = f1
        this.f2 = f2
        this.amp1 = amp1
        this.amp2 = amp2
        this.masterVolume = masterVolume
        this.isBinaural = isBinaural
    }

    actual fun release() {
        pause()
    }
}
