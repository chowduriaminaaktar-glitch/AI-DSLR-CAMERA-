package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiDslrStyle
import com.example.ai.AiEnhancementService
import com.example.ai.SceneAnalysis
import com.example.camera.CameraManager
import com.example.data.CameraMode
import com.example.data.CameraSettings
import com.example.data.FlashMode
import com.example.data.GridType
import com.example.data.MediaItem
import com.example.data.MediaRepository
import com.example.data.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class AppScreen {
    CAMERA,
    PHOTO_PREVIEW,
    VIDEO_PREVIEW,
    GALLERY,
    SETTINGS
}

data class CameraUiState(
    val currentScreen: AppScreen = AppScreen.CAMERA,
    val cameraSettings: CameraSettings = CameraSettings(),
    val cameraMode: CameraMode = CameraMode.PHOTO,
    val zoomRatio: Float = 1.0f,
    val exposureIndex: Int = 0,
    val simulatedAperture: String = "f/1.8",
    val isRecording: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val latestMedia: MediaItem? = null,
    val activeMediaItem: MediaItem? = null,
    val activeOriginalBitmap: Bitmap? = null,
    val activeEnhancedBitmap: Bitmap? = null,
    val isAiProcessing: Boolean = false,
    val aiProgress: Float = 0f,
    val selectedStyle: AiDslrStyle = AiDslrStyle.NATURAL_DSLR,
    val aiStrength: Float = 0.85f,
    val bokehStrength: Float = 0.40f,
    val sceneAnalysis: SceneAnalysis? = null,
    val galleryFilter: String = "ALL", // ALL, PHOTOS, VIDEOS, ENHANCED
    val userMessage: String? = null,
    val isAudioPermissionGranted: Boolean = false
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private val aiService = AiEnhancementService(application)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val mediaItems: StateFlow<List<MediaItem>> = repository.mediaItems

    init {
        viewModelScope.launch {
            repository.loadSavedMedia()
            val list = repository.mediaItems.value
            if (list.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(latestMedia = list.first())
            }
        }
    }

    fun setAudioPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isAudioPermissionGranted = granted)
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen, userMessage = null)
    }

    fun setCameraMode(mode: CameraMode) {
        val simulatedAperture = when (mode) {
            CameraMode.PORTRAIT -> "f/1.4"
            CameraMode.PRO_DSLR -> "f/2.8"
            else -> "f/1.8"
        }
        _uiState.value = _uiState.value.copy(
            cameraMode = mode,
            simulatedAperture = simulatedAperture
        )
    }

    fun cycleFlashMode() {
        val current = _uiState.value.cameraSettings.flashMode
        val next = when (current) {
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.AUTO
        }
        _uiState.value = _uiState.value.copy(
            cameraSettings = _uiState.value.cameraSettings.copy(flashMode = next)
        )
    }

    fun cycleGridType() {
        val current = _uiState.value.cameraSettings.gridType
        val next = when (current) {
            GridType.NONE -> GridType.RULE_OF_THIRDS
            GridType.RULE_OF_THIRDS -> GridType.GOLDEN_RATIO
            GridType.GOLDEN_RATIO -> GridType.NONE
        }
        _uiState.value = _uiState.value.copy(
            cameraSettings = _uiState.value.cameraSettings.copy(gridType = next)
        )
    }

    fun cycleTimer() {
        val current = _uiState.value.cameraSettings.timerSeconds
        val next = when (current) {
            0 -> 3
            3 -> 10
            else -> 0
        }
        _uiState.value = _uiState.value.copy(
            cameraSettings = _uiState.value.cameraSettings.copy(timerSeconds = next)
        )
    }

    fun updateZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoomRatio = zoom)
    }

    fun updateExposure(index: Int) {
        _uiState.value = _uiState.value.copy(exposureIndex = index)
    }

    fun updateAperture(aperture: String) {
        val bokeh = when (aperture) {
            "f/1.4" -> 0.85f
            "f/2.8" -> 0.55f
            "f/5.6" -> 0.25f
            else -> 0.05f
        }
        _uiState.value = _uiState.value.copy(
            simulatedAperture = aperture,
            bokehStrength = bokeh
        )
    }

    fun updateSettings(newSettings: CameraSettings) {
        _uiState.value = _uiState.value.copy(cameraSettings = newSettings)
    }

    fun onPhotoCaptured(file: File) {
        viewModelScope.launch {
            val item = repository.addMediaItem(file, isVideo = false)
            _uiState.value = _uiState.value.copy(
                latestMedia = item,
                activeMediaItem = item,
                userMessage = "Photo captured successfully!"
            )

            // Load Bitmap for preview & enhancement
            loadBitmapForPhoto(item)

            if (_uiState.value.cameraSettings.autoAiEnhance || _uiState.value.cameraMode == CameraMode.PORTRAIT) {
                // Auto enhance with AI
                val style = if (_uiState.value.cameraMode == CameraMode.PORTRAIT) {
                    AiDslrStyle.PORTRAIT_BOKEH
                } else {
                    _uiState.value.selectedStyle
                }
                applyAiEnhancement(style, _uiState.value.aiStrength, _uiState.value.bokehStrength)
            }

            navigateTo(AppScreen.PHOTO_PREVIEW)
        }
    }

    fun onVideoRecordingStarted() {
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            recordingDurationMs = 0L
        )
    }

    fun onVideoRecordingProgress(durationMs: Long) {
        _uiState.value = _uiState.value.copy(recordingDurationMs = durationMs)
    }

    fun onVideoRecordingFinished(file: File) {
        viewModelScope.launch {
            val item = repository.addMediaItem(file, isVideo = true)
            _uiState.value = _uiState.value.copy(
                isRecording = false,
                latestMedia = item,
                activeMediaItem = item,
                userMessage = "Video recorded successfully!"
            )
            navigateTo(AppScreen.VIDEO_PREVIEW)
        }
    }

    private suspend fun loadBitmapForPhoto(item: MediaItem) = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(item.filePath, options)

            // Downsample if huge to prevent OOM
            val reqW = 1920
            val reqH = 1920
            var inSample = 1
            if (options.outHeight > reqH || options.outWidth > reqW) {
                val halfH = options.outHeight / 2
                val halfW = options.outWidth / 2
                while ((halfH / inSample) >= reqH && (halfW / inSample) >= reqW) {
                    inSample *= 2
                }
            }

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = inSample
            }
            val bmp = BitmapFactory.decodeFile(item.filePath, decodeOpts)
            if (bmp != null) {
                val analysis = aiService.analyzeScene(bmp)
                _uiState.value = _uiState.value.copy(
                    activeOriginalBitmap = bmp,
                    activeEnhancedBitmap = null,
                    sceneAnalysis = analysis,
                    selectedStyle = analysis.suggestedStyle
                )
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Failed to load bitmap", e)
        }
    }

    fun openMediaItem(item: MediaItem) {
        _uiState.value = _uiState.value.copy(activeMediaItem = item)
        if (item.mediaType == MediaType.PHOTO) {
            viewModelScope.launch {
                loadBitmapForPhoto(item)
                navigateTo(AppScreen.PHOTO_PREVIEW)
            }
        } else {
            navigateTo(AppScreen.VIDEO_PREVIEW)
        }
    }

    fun applyAiEnhancement(
        style: AiDslrStyle = _uiState.value.selectedStyle,
        strength: Float = _uiState.value.aiStrength,
        bokeh: Float = _uiState.value.bokehStrength
    ) {
        val original = _uiState.value.activeOriginalBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiProcessing = true,
                aiProgress = 0.1f,
                selectedStyle = style,
                aiStrength = strength,
                bokehStrength = bokeh
            )

            try {
                val enhanced = aiService.enhancePhoto(
                    bitmap = original,
                    style = style,
                    strength = strength,
                    blurStrength = bokeh,
                    onProgress = { progress ->
                        _uiState.value = _uiState.value.copy(aiProgress = progress)
                    }
                )

                _uiState.value = _uiState.value.copy(
                    activeEnhancedBitmap = enhanced,
                    isAiProcessing = false,
                    aiProgress = 1.0f,
                    userMessage = "AI DSLR enhancement complete!"
                )
            } catch (e: Exception) {
                Log.e("CameraViewModel", "AI Enhancement failed", e)
                _uiState.value = _uiState.value.copy(
                    isAiProcessing = false,
                    userMessage = "AI processing failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun saveEnhancedPhoto() {
        val enhancedBmp = _uiState.value.activeEnhancedBitmap ?: return
        val currentStyle = _uiState.value.selectedStyle
        viewModelScope.launch {
            val file = repository.createEnhancedImageFile(currentStyle.displayName)
            val saved = repository.saveBitmapToFile(enhancedBmp, file)
            if (saved) {
                val item = repository.addMediaItem(file, isVideo = false, isEnhanced = true, style = currentStyle.displayName)
                if (_uiState.value.cameraSettings.saveToPublicGallery) {
                    repository.exportToPublicGallery(item)
                }
                _uiState.value = _uiState.value.copy(
                    latestMedia = item,
                    activeMediaItem = item,
                    userMessage = "Enhanced photo saved to device!"
                )
            } else {
                _uiState.value = _uiState.value.copy(userMessage = "Failed to save enhanced photo")
            }
        }
    }

    fun enhanceCurrentVideo(style: AiDslrStyle = _uiState.value.selectedStyle) {
        val currentItem = _uiState.value.activeMediaItem ?: return
        if (currentItem.mediaType != MediaType.VIDEO) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiProcessing = true,
                aiProgress = 0.0f
            )

            val sourceFile = File(currentItem.filePath)
            val targetFile = repository.createEnhancedVideoFile(style.displayName)

            try {
                aiService.enhanceVideo(
                    sourceFile = sourceFile,
                    targetFile = targetFile,
                    style = style,
                    strength = _uiState.value.aiStrength,
                    onProgress = { progress ->
                        _uiState.value = _uiState.value.copy(aiProgress = progress)
                    }
                )

                val enhancedItem = repository.addMediaItem(
                    file = targetFile,
                    isVideo = true,
                    isEnhanced = true,
                    style = style.displayName
                )

                if (_uiState.value.cameraSettings.saveToPublicGallery) {
                    repository.exportToPublicGallery(enhancedItem)
                }

                _uiState.value = _uiState.value.copy(
                    isAiProcessing = false,
                    activeMediaItem = enhancedItem,
                    latestMedia = enhancedItem,
                    userMessage = "Enhanced DSLR video saved to device!"
                )
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Video enhancement failed", e)
                _uiState.value = _uiState.value.copy(
                    isAiProcessing = false,
                    userMessage = "Video enhancement failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun importMediaFromUri(uri: Uri, isVideo: Boolean) {
        viewModelScope.launch {
            try {
                val resolver = getApplication<Application>().contentResolver
                val file = if (isVideo) repository.createNewVideoFile() else repository.createNewImageFile()

                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val item = repository.addMediaItem(file, isVideo = isVideo)
                openMediaItem(item)
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Failed to import media", e)
                _uiState.value = _uiState.value.copy(userMessage = "Failed to import media: ${e.localizedMessage}")
            }
        }
    }

    fun exportToPublicGallery(item: MediaItem) {
        viewModelScope.launch {
            val uri = repository.exportToPublicGallery(item)
            if (uri != null) {
                _uiState.value = _uiState.value.copy(userMessage = "Saved to public gallery!")
            } else {
                _uiState.value = _uiState.value.copy(userMessage = "Export failed")
            }
        }
    }

    fun deleteMedia(item: MediaItem) {
        viewModelScope.launch {
            val success = repository.deleteMediaItem(item)
            if (success) {
                if (_uiState.value.activeMediaItem?.id == item.id) {
                    _uiState.value = _uiState.value.copy(activeMediaItem = null)
                    navigateTo(AppScreen.GALLERY)
                }
                _uiState.value = _uiState.value.copy(userMessage = "Media deleted")
            }
        }
    }

    fun shareMedia(item: MediaItem) {
        repository.shareMediaItem(item)
    }

    fun setGalleryFilter(filter: String) {
        _uiState.value = _uiState.value.copy(galleryFilter = filter)
    }

    fun dismissUserMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }

    fun createNewImageFile(): File = repository.createNewImageFile()
    fun createNewVideoFile(): File = repository.createNewVideoFile()
}
