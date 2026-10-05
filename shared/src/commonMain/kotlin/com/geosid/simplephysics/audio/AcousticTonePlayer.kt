package com.geosid.simplephysics.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

expect class AcousticTonePlayer() {
    val isPlaying: Boolean
    fun play()
    fun pause()
    fun update(
        f1: Float,
        f2: Float,
        amp1: Float,
        amp2: Float,
        masterVolume: Float,
        isBinaural: Boolean
    )
    fun release()
}

@Composable
fun rememberAcousticTonePlayer(): AcousticTonePlayer {
    val player = remember { AcousticTonePlayer() }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    return player
}
