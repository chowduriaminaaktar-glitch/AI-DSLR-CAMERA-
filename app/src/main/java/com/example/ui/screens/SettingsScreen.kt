package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GridType
import com.example.data.PhotoQuality
import com.example.data.VideoResolution
import com.example.ui.AppScreen
import com.example.ui.CameraViewModel

@Composable
fun SettingsScreen(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.cameraSettings

    BackHandler {
        viewModel.navigateTo(AppScreen.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1218))
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.CAMERA) },
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to camera",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "CAMERA SETTINGS",
                color = Color(0xFFFFB300),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Settings Content List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Camera Hardware & Viewfinder
            Text(
                text = "VIEWFINDER & SHOOTING",
                color = Color(0xFFFFB300),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161A24)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Artificial Level Indicator Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Artificial Horizon Level", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Displays real-time horizon tilt line", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.isLevelIndicatorEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(isLevelIndicatorEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB300),
                                checkedTrackColor = Color(0x66FFB300)
                            )
                        )
                    }

                    // Grid Overlay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Grid Overlay", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                when (settings.gridType) {
                                    GridType.NONE -> "Off"
                                    GridType.RULE_OF_THIRDS -> "Rule of Thirds (3x3)"
                                    GridType.GOLDEN_RATIO -> "Golden Ratio (Phi)"
                                },
                                color = Color(0xFFFFB300),
                                fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                GridType.NONE to "Off",
                                GridType.RULE_OF_THIRDS to "3x3",
                                GridType.GOLDEN_RATIO to "Phi"
                            ).forEach { (type, label) ->
                                val isSelected = settings.gridType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFFB300) else Color(0xFF262C3A))
                                        .clickable { viewModel.updateSettings(settings.copy(gridType = type)) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Photo Resolution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Photo Capture Quality", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Full DSLR sensor resolution", color = Color.Gray, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                PhotoQuality.HIGH to "High",
                                PhotoQuality.MEDIUM to "Med"
                            ).forEach { (qual, label) ->
                                val isSelected = settings.photoQuality == qual
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFFB300) else Color(0xFF262C3A))
                                        .clickable { viewModel.updateSettings(settings.copy(photoQuality = qual)) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Video Resolution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Video Recording Resolution", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Hardware encoder pipeline", color = Color.Gray, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                VideoResolution.FHD_1080P to "1080p FHD",
                                VideoResolution.HD_720P to "720p HD"
                            ).forEach { (res, label) ->
                                val isSelected = settings.videoResolution == res
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFFB300) else Color(0xFF262C3A))
                                        .clickable { viewModel.updateSettings(settings.copy(videoResolution = res)) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: AI DSLR Enhancement Settings
            Text(
                text = "AI DSLR PROCESSING PIPELINE",
                color = Color(0xFFFFB300),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161A24)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Auto-enhance after capture toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto AI Enhance on Capture", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Instantly processes photos using AI scene analysis", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.autoAiEnhance,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(autoAiEnhance = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB300),
                                checkedTrackColor = Color(0x66FFB300)
                            )
                        )
                    }

                    // Default AI Strength Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Enhancement Strength", color = Color.White, fontSize = 13.sp)
                            Text("${(settings.defaultAiStrength * 100).toInt()}%", color = Color(0xFFFFB300), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.defaultAiStrength,
                            onValueChange = {
                                viewModel.updateSettings(settings.copy(defaultAiStrength = it))
                            },
                            valueRange = 0.3f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFB300),
                                activeTrackColor = Color(0xFFFFB300)
                            )
                        )
                    }

                    // Cloud AI Architecture Status Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E2433),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD0E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF4DD0E1))
                            Column {
                                Text(
                                    text = "AI Architecture: Dual Engine",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Local high-performance DSP engine is active (100% offline). Cloud AI gateway is decoupled and ready for external backend models.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Storage & Save Location
            Text(
                text = "STORAGE & EXPORT",
                color = Color(0xFFFFB300),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161A24)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Save to Public Gallery", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Export photos & videos to DCIM/AI_DSLR_Camera", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.saveToPublicGallery,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(saveToPublicGallery = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB300),
                                checkedTrackColor = Color(0x66FFB300)
                            )
                        )
                    }
                }
            }

            // Section 4: About
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161A24)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AI DSLR Camera", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Version 1.0.0 • Professional Mobile Photography", color = Color.Gray, fontSize = 12.sp)
                    Text("Powered by real-time CameraX, DSLR bokeh depth synthesis, unsharp micro-contrast convolution, and multi-profile color science.", color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}
