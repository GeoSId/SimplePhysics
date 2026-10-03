package com.geosid.simplephysics.audio

actual class AcousticTonePlayer actual constructor() {
    actual val isPlaying: Boolean = false
    actual fun play() {}
    actual fun pause() {}
    actual fun update(
        f1: Float,
        f2: Float,
        amp1: Float,
        amp2: Float,
        masterVolume: Float,
        isBinaural: Boolean
    ) {}
    actual fun release() {}
}
