package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CameraMode
import com.example.data.MediaItem

@Composable
fun CameraControlBar(
    modifier: Modifier = Modifier,
    cameraMode: CameraMode,
    isRecording: Boolean,
    currentZoom: Float,
    latestMedia: MediaItem?,
    onModeSelect: (CameraMode) -> Unit,
    onZoomSelect: (Float) -> Unit,
    onShutterClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenGallery: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Zoom Quick Selector Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x33FFFFFF),
            border = BorderStroke(1.dp, Color(0x22FFFFFF))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1.0f, 2.0f, 5.0f).forEach { zoomTarget ->
                    val isSelected = (currentZoom >= zoomTarget - 0.2f && currentZoom <= zoomTarget + 0.2f)
                    val bgColor = if (isSelected) Color(0xFFFFB300) else Color.Transparent
                    val textColor = if (isSelected) Color.Black else Color.White

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable { onZoomSelect(zoomTarget) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${zoomTarget.toInt()}x",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }

        // Mode Selector Carousel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val modes = listOf(
                CameraMode.PHOTO to "PHOTO",
                CameraMode.VIDEO to "VIDEO",
                CameraMode.PORTRAIT to "PORTRAIT",
                CameraMode.PRO_DSLR to "PRO DSLR"
            )

            modes.forEach { (mode, label) ->
                val isSelected = cameraMode == mode
                Text(
                    text = label,
                    fontSize = if (isSelected) 13.sp else 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clickable { onModeSelect(mode) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("mode_${label.lowercase()}")
                )
            }
        }

        // Bottom Action Row: Gallery, Shutter, Switch Camera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Thumbnail Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E222B))
                    .clickable { onOpenGallery() }
                    .testTag("gallery_thumbnail_button"),
                contentAlignment = Alignment.Center
            ) {
                if (latestMedia != null) {
                    AsyncImage(
                        model = latestMedia.uri,
                        contentDescription = "Latest capture preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Open gallery",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Large Ergonomic DSLR Shutter Button
            val isVideoMode = cameraMode == CameraMode.VIDEO
            val shutterOuterSize by animateDpAsState(
                targetValue = if (isRecording) 82.dp else 76.dp,
                label = "outer_size"
            )
            val shutterInnerSize by animateDpAsState(
                targetValue = if (isRecording) 32.dp else 60.dp,
                label = "inner_size"
            )
            val innerColor by animateColorAsState(
                targetValue = if (isVideoMode) Color(0xFFE53935) else Color.White,
                label = "shutter_color"
            )
            val innerShape = if (isRecording) RoundedCornerShape(8.dp) else CircleShape

            Box(
                modifier = Modifier
                    .size(shutterOuterSize)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clickable { onShutterClick() }
                    .testTag("shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(shutterInnerSize)
                        .clip(innerShape)
                        .background(innerColor)
                )
            }

            // Camera Switch Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262C36))
                    .clickable { onSwitchCamera() }
                    .testTag("switch_camera_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch camera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
