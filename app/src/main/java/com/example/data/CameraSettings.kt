package com.example.data

enum class FlashMode {
    OFF,
    AUTO,
    ON
}

enum class GridType {
    NONE,
    RULE_OF_THIRDS,
    GOLDEN_RATIO
}

enum class CameraMode {
    PHOTO,
    VIDEO,
    PORTRAIT,
    PRO_DSLR
}

enum class PhotoQuality {
    HIGH,
    MEDIUM,
    STANDARD
}

enum class VideoResolution {
    FHD_1080P,
    HD_720P
}

data class CameraSettings(
    val flashMode: FlashMode = FlashMode.AUTO,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val isLevelIndicatorEnabled: Boolean = true,
    val autoAiEnhance: Boolean = false,
    val defaultAiStrength: Float = 0.85f,
    val defaultStyleName: String = "Natural DSLR",
    val photoQuality: PhotoQuality = PhotoQuality.HIGH,
    val videoResolution: VideoResolution = VideoResolution.FHD_1080P,
    val saveToPublicGallery: Boolean = true,
    val timerSeconds: Int = 0 // 0 = off, 3, 10
)
