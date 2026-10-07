package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CameraMode
import com.example.data.FlashMode
import com.example.data.GridType

@Composable
fun CameraTopBar(
    modifier: Modifier = Modifier,
    flashMode: FlashMode,
    gridType: GridType,
    timerSeconds: Int,
    isRecording: Boolean,
    recordingDurationMs: Long,
    cameraMode: CameraMode,
    exposureIndex: Int,
    onFlashToggle: () -> Unit,
    onGridToggle: () -> Unit,
    onTimerToggle: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Icon Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode button
            IconButton(
                onClick = onFlashToggle,
                modifier = Modifier.testTag("flash_toggle_button")
            ) {
                val icon = when (flashMode) {
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.OFF -> Icons.Default.FlashOff
                }
                val tint = if (flashMode != FlashMode.OFF) Color(0xFFFFB300) else Color.White
                Icon(icon, contentDescription = "Flash mode", tint = tint)
            }

            // Timer button
            IconButton(
                onClick = onTimerToggle,
                modifier = Modifier.testTag("timer_toggle_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Shutter timer",
                        tint = if (timerSeconds > 0) Color(0xFFFFB300) else Color.White
                    )
                    if (timerSeconds > 0) {
                        Text(
                            text = "${timerSeconds}s",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Recording banner or DSLR HUD telemetry
            if (isRecording) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Red.copy(alpha = 0.85f),
                    modifier = Modifier.testTag("recording_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        val totalSecs = recordingDurationMs / 1000
                        val mins = totalSecs / 60
                        val secs = totalSecs % 60
                        Text(
                            text = String.format("REC %02d:%02d", mins, secs),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // DSLR Status HUD Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFB300))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI DSLR",
                            color = Color(0xFFFFB300),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (exposureIndex >= 0) "+${exposureIndex}EV" else "${exposureIndex}EV",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = when (cameraMode) {
                                CameraMode.PHOTO -> "PHOTO"
                                CameraMode.VIDEO -> "VIDEO"
                                CameraMode.PORTRAIT -> "BOKEH"
                                CameraMode.PRO_DSLR -> "PRO"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Grid toggle button
            IconButton(
                onClick = onGridToggle,
                modifier = Modifier.testTag("grid_toggle_button")
            ) {
                Icon(
                    Icons.Default.GridOn,
                    contentDescription = "Grid overlay",
                    tint = if (gridType != GridType.NONE) Color(0xFFFFB300) else Color.White.copy(alpha = 0.6f)
                )
            }

            // Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }
    }
}
