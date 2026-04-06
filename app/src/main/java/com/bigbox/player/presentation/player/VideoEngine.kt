package com.bigbox.player.presentation.player

import android.content.Context
import android.widget.FrameLayout

interface VideoEngine {
    fun init(context: Context, container: FrameLayout)
    fun playUrl(url: String)
    fun pause()
    fun resume()
    fun stop()
    fun release()
}
