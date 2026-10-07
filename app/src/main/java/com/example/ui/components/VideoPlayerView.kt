package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoPlayerView(
    modifier: Modifier = Modifier,
    videoUri: Uri,
    autoPlay: Boolean = true
) {
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isCompleted by remember { mutableStateOf(false) }
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(videoUri) {
        onDispose {
            videoViewInstance?.stopPlayback()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(videoUri)
                    val controller = MediaController(context)
                    controller.setAnchorView(this)
                    setMediaController(controller)

                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        if (autoPlay) {
                            start()
                            isPlaying = true
                        }
                    }

                    setOnCompletionListener {
                        isPlaying = false
                        isCompleted = true
                    }

                    videoViewInstance = this
                }
            },
            update = { view ->
                // Update URI if changed
                view.setVideoURI(videoUri)
                if (autoPlay) {
                    view.start()
                    isPlaying = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Play/Pause indicator
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable {
                        videoViewInstance?.start()
                        isPlaying = true
                        isCompleted = false
                    }
                    .testTag("video_play_overlay_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.Replay else Icons.Default.PlayArrow,
                    contentDescription = "Play video",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
