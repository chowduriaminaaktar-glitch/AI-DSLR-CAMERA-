package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.data.FlashMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.TimeUnit

sealed class CameraState {
    object Idle : CameraState()
    object Initializing : CameraState()
    object Ready : CameraState()
    data class RecordingVideo(val durationMs: Long) : CameraState()
    data class Error(val message: String) : CameraState()
}

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val tag = "CameraManager"

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    private var isBackCamera = true
    private var currentFlashMode = FlashMode.AUTO

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _maxZoomRatio = MutableStateFlow(5f)
    val maxZoomRatio: StateFlow<Float> = _maxZoomRatio.asStateFlow()

    private val _exposureIndex = MutableStateFlow(0)
    val exposureIndex: StateFlow<Int> = _exposureIndex.asStateFlow()

    private val _exposureRange = MutableStateFlow(-4..4)
    val exposureRange: StateFlow<IntRange> = _exposureRange.asStateFlow()

    fun bindCamera(previewView: PreviewView, onReady: () -> Unit = {}) {
        _cameraState.value = CameraState.Initializing
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                startCamera(previewView)
                _cameraState.value = CameraState.Ready
                onReady()
            } catch (e: Exception) {
                Log.e(tag, "Failed to bind camera", e)
                _cameraState.value = CameraState.Error("Failed to open camera: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun startCamera(previewView: PreviewView) {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val cameraSelector = if (isBackCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val flash = when (currentFlashMode) {
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(flash)
            .build()

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        try {
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )

            // Setup Zoom observation
            camera?.cameraInfo?.zoomState?.observe(lifecycleOwner) { state ->
                if (state != null) {
                    _zoomRatio.value = state.zoomRatio
                    _maxZoomRatio.value = state.maxZoomRatio.coerceAtMost(8f)
                }
            }

            // Setup Exposure observation
            camera?.cameraInfo?.exposureState?.let { exp ->
                if (exp.isExposureCompensationSupported) {
                    _exposureRange.value = exp.exposureCompensationRange.lower..exp.exposureCompensationRange.upper
                    _exposureIndex.value = exp.exposureCompensationIndex
                }
            }

            // Update torch if needed
            applyTorchIfNeeded()

        } catch (e: Exception) {
            Log.e(tag, "Use cases binding failed", e)
            _cameraState.value = CameraState.Error("Could not start camera view: ${e.localizedMessage}")
        }
    }

    fun switchCamera(previewView: PreviewView) {
        isBackCamera = !isBackCamera
        startCamera(previewView)
    }

    fun setFlashMode(flashMode: FlashMode) {
        currentFlashMode = flashMode
        val mode = when (flashMode) {
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = mode
        applyTorchIfNeeded()
    }

    private fun applyTorchIfNeeded() {
        try {
            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                camera?.cameraControl?.enableTorch(currentFlashMode == FlashMode.ON)
            }
        } catch (_: Exception) {}
    }

    fun focusOnPoint(previewView: PreviewView, x: Float, y: Float) {
        try {
            val factory = previewView.meteringPointFactory
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                .build()
            camera?.cameraControl?.startFocusAndMetering(action)
        } catch (e: Exception) {
            Log.e(tag, "Focus metering failed", e)
        }
    }

    fun setZoom(ratio: Float) {
        val clamped = ratio.coerceIn(1f, _maxZoomRatio.value)
        camera?.cameraControl?.setZoomRatio(clamped)
    }

    fun setExposureIndex(index: Int) {
        try {
            val range = _exposureRange.value
            val clamped = index.coerceIn(range.first, range.last)
            _exposureIndex.value = clamped
            camera?.cameraControl?.setExposureCompensationIndex(clamped)
        } catch (e: Exception) {
            Log.e(tag, "Set exposure failed", e)
        }
    }

    fun capturePhoto(
        outputFile: File,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError(IllegalStateException("Camera is not ready"))
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSuccess(outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(tag, "Photo capture failed", exception)
                    onError(exception)
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun startVideoRecording(
        outputFile: File,
        hasAudioPermission: Boolean,
        onStarted: () -> Unit,
        onProgress: (Long) -> Unit,
        onFinished: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = videoCapture ?: run {
            onError(IllegalStateException("Video capture not ready"))
            return
        }

        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        val pending = capture.output.prepareRecording(context, outputOptions)

        if (hasAudioPermission) {
            try {
                pending.withAudioEnabled()
            } catch (e: SecurityException) {
                Log.w(tag, "Audio permission missing, recording video without audio")
            }
        }

        activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    _cameraState.value = CameraState.RecordingVideo(0L)
                    onStarted()
                }
                is VideoRecordEvent.Status -> {
                    val durationMs = event.recordingStats.recordedDurationNanos / 1_000_000
                    _cameraState.value = CameraState.RecordingVideo(durationMs)
                    onProgress(durationMs)
                }
                is VideoRecordEvent.Finalize -> {
                    _cameraState.value = CameraState.Ready
                    activeRecording = null
                    if (event.hasError()) {
                        Log.e(tag, "Video recording finalized with error: ${event.error}")
                        onError(IllegalStateException("Recording error: ${event.error}"))
                    } else {
                        onFinished(outputFile)
                    }
                }
            }
        }
    }

    fun stopVideoRecording() {
        try {
            activeRecording?.stop()
            activeRecording = null
            _cameraState.value = CameraState.Ready
        } catch (e: Exception) {
            Log.e(tag, "Failed to stop recording", e)
        }
    }

    fun isRecording(): Boolean = activeRecording != null

    fun release() {
        try {
            activeRecording?.stop()
            activeRecording = null
            cameraProvider?.unbindAll()
        } catch (_: Exception) {}
    }
}
