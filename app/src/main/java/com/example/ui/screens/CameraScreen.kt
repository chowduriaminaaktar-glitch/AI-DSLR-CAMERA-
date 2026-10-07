package com.example.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.CameraManager
import com.example.data.CameraMode
import com.example.ui.AppScreen
import com.example.ui.CameraViewModel
import com.example.ui.components.CameraControlBar
import com.example.ui.components.CameraTopBar
import com.example.ui.components.CameraViewfinder
import com.example.ui.components.DslrManualPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    val cameraManager = remember(lifecycleOwner) {
        CameraManager(context, lifecycleOwner)
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraManager.release()
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            cameraManager.bindCamera(previewView)
        }
    }

    // Photo/Video Picker Launcher for importing existing media
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isVideo = mimeType.startsWith("video")
            viewModel.importMediaFromUri(uri, isVideo)
        }
    }

    var countdownTime by remember { mutableIntStateOf(0) }
    var isCountingDown by remember { mutableStateOf(false) }

    fun triggerShutter() {
        if (uiState.cameraMode == CameraMode.VIDEO) {
            if (cameraManager.isRecording()) {
                cameraManager.stopVideoRecording()
            } else {
                val videoFile = viewModel.createNewVideoFile()
                cameraManager.startVideoRecording(
                    outputFile = videoFile,
                    hasAudioPermission = uiState.isAudioPermissionGranted,
                    onStarted = { viewModel.onVideoRecordingStarted() },
                    onProgress = { viewModel.onVideoRecordingProgress(it) },
                    onFinished = { viewModel.onVideoRecordingFinished(it) },
                    onError = { viewModel.updateSettings(uiState.cameraSettings) }
                )
            }
        } else {
            // Photo capture
            val timerSecs = uiState.cameraSettings.timerSeconds
            if (timerSecs > 0 && !isCountingDown) {
                scope.launch {
                    isCountingDown = true
                    countdownTime = timerSecs
                    while (countdownTime > 0) {
                        delay(1000)
                        countdownTime--
                    }
                    isCountingDown = false
                    val photoFile = viewModel.createNewImageFile()
                    cameraManager.capturePhoto(
                        outputFile = photoFile,
                        onSuccess = { viewModel.onPhotoCaptured(it) },
                        onError = {}
                    )
                }
            } else {
                val photoFile = viewModel.createNewImageFile()
                cameraManager.capturePhoto(
                    outputFile = photoFile,
                    onSuccess = { viewModel.onPhotoCaptured(it) },
                    onError = {}
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            // Permission Request Card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFB300))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x33FFB300),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = "Camera Access Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "AI DSLR Camera requires access to your device camera to preview and capture high-resolution photos and videos.",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Button(
                            onClick = onRequestCameraPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("grant_camera_permission_button")
                        ) {
                            Text(
                                text = "Grant Camera Permission",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Main Camera Live Viewfinder
            CameraViewfinder(
                previewView = previewView,
                gridType = uiState.cameraSettings.gridType,
                showLevelIndicator = uiState.cameraSettings.isLevelIndicatorEnabled,
                onTapToFocus = { x, y ->
                    cameraManager.focusOnPoint(previewView, x, y)
                },
                onPinchZoom = { delta ->
                    val newZoom = (uiState.zoomRatio * delta).coerceIn(1f, 8f)
                    cameraManager.setZoom(newZoom)
                    viewModel.updateZoom(newZoom)
                }
            )

            // Top Bar
            CameraTopBar(
                modifier = Modifier.align(Alignment.TopCenter),
                flashMode = uiState.cameraSettings.flashMode,
                gridType = uiState.cameraSettings.gridType,
                timerSeconds = uiState.cameraSettings.timerSeconds,
                isRecording = uiState.isRecording,
                recordingDurationMs = uiState.recordingDurationMs,
                cameraMode = uiState.cameraMode,
                exposureIndex = uiState.exposureIndex,
                onFlashToggle = {
                    viewModel.cycleFlashMode()
                    cameraManager.setFlashMode(viewModel.uiState.value.cameraSettings.flashMode)
                },
                onGridToggle = { viewModel.cycleGridType() },
                onTimerToggle = { viewModel.cycleTimer() },
                onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) }
            )

            // Floating Import Button (Select from gallery)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 16.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                IconButton(
                    onClick = {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    modifier = Modifier.testTag("import_media_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Import from gallery",
                        tint = Color.White
                    )
                }
            }

            // Pro DSLR Manual Panel (when in PRO_DSLR or PORTRAIT mode)
            if (uiState.cameraMode == CameraMode.PRO_DSLR) {
                DslrManualPanel(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 190.dp),
                    exposureIndex = uiState.exposureIndex,
                    exposureRange = -4..4,
                    simulatedAperture = uiState.simulatedAperture,
                    onExposureChange = { exp ->
                        cameraManager.setExposureIndex(exp)
                        viewModel.updateExposure(exp)
                    },
                    onApertureChange = { ap ->
                        viewModel.updateAperture(ap)
                    }
                )
            }

            // Bottom Camera Controls
            CameraControlBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                cameraMode = uiState.cameraMode,
                isRecording = uiState.isRecording,
                currentZoom = uiState.zoomRatio,
                latestMedia = uiState.latestMedia,
                onModeSelect = { mode -> viewModel.setCameraMode(mode) },
                onZoomSelect = { target ->
                    cameraManager.setZoom(target)
                    viewModel.updateZoom(target)
                },
                onShutterClick = { triggerShutter() },
                onSwitchCamera = { cameraManager.switchCamera(previewView) },
                onOpenGallery = { viewModel.navigateTo(AppScreen.GALLERY) }
            )

            // Shutter Countdown Overlay
            if (isCountingDown && countdownTime > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$countdownTime",
                        fontSize = 110.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFB300)
                    )
                }
            }
        }
    }
}
