package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.AiDslrStyle
import com.example.ui.AppScreen
import com.example.ui.CameraViewModel
import com.example.ui.components.BeforeAfterSlider

@Composable
fun PhotoPreviewScreen(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    BackHandler {
        viewModel.navigateTo(AppScreen.CAMERA)
    }

    var selectedStyle by remember(uiState.selectedStyle) { mutableStateOf(uiState.selectedStyle) }
    var currentStrength by remember(uiState.aiStrength) { mutableFloatStateOf(uiState.aiStrength) }
    var currentBokeh by remember(uiState.bokehStrength) { mutableFloatStateOf(uiState.bokehStrength) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1218))
    ) {
        // Top Navigation & Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.CAMERA) },
                modifier = Modifier.testTag("preview_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to camera",
                    tint = Color.White
                )
            }

            Text(
                text = "AI DSLR STUDIO",
                color = Color(0xFFFFB300),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Share
                IconButton(
                    onClick = {
                        uiState.activeMediaItem?.let { viewModel.shareMedia(it) }
                    },
                    modifier = Modifier.testTag("preview_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }

                // Delete
                IconButton(
                    onClick = {
                        uiState.activeMediaItem?.let { viewModel.deleteMedia(it) }
                    },
                    modifier = Modifier.testTag("preview_delete_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                }
            }
        }

        // Scene Analysis Banner
        uiState.sceneAnalysis?.let { analysis ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E232F)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Scene: ${analysis.sceneType} • Suggested: ${analysis.suggestedStyle.displayName}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Main Image Viewport (Before/After or Single)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.activeEnhancedBitmap != null && uiState.activeOriginalBitmap != null) {
                // Interactive Before / After Split Slider
                BeforeAfterSlider(
                    originalBitmap = uiState.activeOriginalBitmap,
                    enhancedBitmap = uiState.activeEnhancedBitmap
                )
            } else if (uiState.activeOriginalBitmap != null) {
                // Original Photo Display
                AsyncImage(
                    model = uiState.activeOriginalBitmap,
                    contentDescription = "Original photo",
                    modifier = Modifier.fillMaxSize()
                )

                // Quick Enhance Callout if not processed yet
                if (!uiState.isAiProcessing) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                    ) {
                        Button(
                            onClick = {
                                viewModel.applyAiEnhancement(selectedStyle, currentStrength, currentBokeh)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier.testTag("apply_initial_enhance_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFB300))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Process AI DSLR Enhancement", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                CircularProgressIndicator(color = Color(0xFFFFB300))
            }

            // Processing Indicator
            if (uiState.isAiProcessing) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    color = Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { uiState.aiProgress },
                                    color = Color(0xFFFFB300),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Enhancing with AI DSLR...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Rendering prime lens bokeh & tone mapping",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom AI Controls & Presets Panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF131720),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Style Presets Carousel
                Text(
                    text = "DSLR PICTURE PROFILES",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiDslrStyle.values().forEach { style ->
                        val isSelected = selectedStyle == style
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFFFB300) else Color(0xFF222836))
                                .clickable {
                                    selectedStyle = style
                                    currentBokeh = style.defaultBlur
                                    viewModel.applyAiEnhancement(style, currentStrength, style.defaultBlur)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("style_${style.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }

                // Sliders Row: Strength & Bokeh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // AI Strength
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("AI Strength", color = Color.Gray, fontSize = 11.sp)
                            Text("${(currentStrength * 100).toInt()}%", color = Color(0xFFFFB300), fontSize = 11.sp)
                        }
                        Slider(
                            value = currentStrength,
                            onValueChange = {
                                currentStrength = it
                            },
                            onValueChangeFinished = {
                                viewModel.applyAiEnhancement(selectedStyle, currentStrength, currentBokeh)
                            },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFB300),
                                activeTrackColor = Color(0xFFFFB300)
                            )
                        )
                    }

                    // Bokeh Blur
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DSLR Bokeh", color = Color.Gray, fontSize = 11.sp)
                            Text("${(currentBokeh * 100).toInt()}%", color = Color(0xFFFFB300), fontSize = 11.sp)
                        }
                        Slider(
                            value = currentBokeh,
                            onValueChange = {
                                currentBokeh = it
                            },
                            onValueChangeFinished = {
                                viewModel.applyAiEnhancement(selectedStyle, currentStrength, currentBokeh)
                            },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFB300),
                                activeTrackColor = Color(0xFFFFB300)
                            )
                        )
                    }
                }

                // Action Buttons: Save Enhanced Photo & Export to Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveEnhancedPhoto() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_enhanced_photo_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Enhanced Photo", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            uiState.activeMediaItem?.let { viewModel.exportToPublicGallery(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3242)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("export_to_device_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Device Gallery", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
