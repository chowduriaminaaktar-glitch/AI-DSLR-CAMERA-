package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ai.AiDslrStyle
import com.example.ui.AppScreen
import com.example.ui.CameraViewModel
import com.example.ui.components.VideoPlayerView

@Composable
fun VideoPreviewScreen(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    BackHandler {
        viewModel.navigateTo(AppScreen.CAMERA)
    }

    val activeItem = uiState.activeMediaItem
    var selectedStyle by remember { mutableStateOf(AiDslrStyle.CINEMATIC_TEAL) }

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.CAMERA) },
                modifier = Modifier.testTag("video_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to camera",
                    tint = Color.White
                )
            }

            Text(
                text = "DSLR VIDEO PLAYBACK",
                color = Color(0xFFFFB300),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Share
                IconButton(
                    onClick = {
                        activeItem?.let { viewModel.shareMedia(it) }
                    },
                    modifier = Modifier.testTag("video_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }

                // Delete
                IconButton(
                    onClick = {
                        activeItem?.let { viewModel.deleteMedia(it) }
                    },
                    modifier = Modifier.testTag("video_delete_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                }
            }
        }

        // Real Video Player View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (activeItem != null) {
                VideoPlayerView(
                    videoUri = activeItem.uri,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("No video selected", color = Color.Gray)
            }

            // AI Video Processing Overlay
            if (uiState.isAiProcessing) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
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
                                    modifier = Modifier.size(52.dp)
                                )
                                Text(
                                    text = "Processing AI DSLR Video...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Color grading and dynamic range tone curves",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                LinearProgressIndicator(
                                    progress = { uiState.aiProgress },
                                    modifier = Modifier.fillMaxWidth(0.7f),
                                    color = Color(0xFFFFB300)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Video Enhancement Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF131720),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI VIDEO COLOR SCIENCE",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (activeItem?.isEnhanced == true) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33FFB300)
                        ) {
                            Text(
                                text = "ENHANCED: ${activeItem.enhancedStyle ?: "DSLR"}",
                                color = Color(0xFFFFB300),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Preset styles carousel
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
                                .clickable { selectedStyle = style }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
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

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.enhanceCurrentVideo(selectedStyle) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("enhance_video_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enhance DSLR Video", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            activeItem?.let { viewModel.exportToPublicGallery(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3242)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("export_video_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", color = Color.White)
                    }
                }
            }
        }
    }
}
