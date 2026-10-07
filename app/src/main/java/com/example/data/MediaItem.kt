package com.example.data

import android.net.Uri

enum class MediaType {
    PHOTO,
    VIDEO
}

data class MediaItem(
    val id: String,
    val uri: Uri,
    val filePath: String,
    val displayName: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val mediaType: MediaType = MediaType.PHOTO,
    val durationMs: Long = 0L,
    val isEnhanced: Boolean = false,
    val originalUri: Uri? = null,
    val enhancedStyle: String? = null,
    val enhancementStrength: Float = 0.8f,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L
)
