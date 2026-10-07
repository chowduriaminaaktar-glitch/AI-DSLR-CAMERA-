package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MediaRepository(private val context: Context) {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    private val mediaDir: File by lazy {
        val dir = File(context.filesDir, "captured_media")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun loadSavedMedia() = withContext(Dispatchers.IO) {
        val files = mediaDir.listFiles() ?: emptyArray()
        val list = mutableListOf<MediaItem>()

        files.sortedByDescending { it.lastModified() }.forEach { file ->
            val isVideo = file.extension.lowercase() in listOf("mp4", "mkv", "mov", "3gp")
            val isPhoto = file.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")

            if (isVideo || isPhoto) {
                val uri = try {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } catch (e: Exception) {
                    Uri.fromFile(file)
                }

                var width = 0
                var height = 0
                var duration = 0L

                if (isVideo) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(file.absolutePath)
                        duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                        height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                    } catch (_: Exception) {
                    } finally {
                        try { retriever.release() } catch (_: Exception) {}
                    }
                } else {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    width = options.outWidth
                    height = options.outHeight
                }

                val isEnhanced = file.name.contains("_enhanced_")
                val style = if (isEnhanced) {
                    file.name.substringAfter("_enhanced_").substringBefore(".")
                } else null

                list.add(
                    MediaItem(
                        id = file.name,
                        uri = uri,
                        filePath = file.absolutePath,
                        displayName = file.name,
                        dateAdded = file.lastModified(),
                        mediaType = if (isVideo) MediaType.VIDEO else MediaType.PHOTO,
                        durationMs = duration,
                        isEnhanced = isEnhanced,
                        enhancedStyle = style,
                        width = width,
                        height = height,
                        sizeBytes = file.length()
                    )
                )
            }
        }
        _mediaItems.value = list
    }

    fun createNewImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(mediaDir, "DSLR_IMG_${timeStamp}_${UUID.randomUUID().toString().take(5)}.jpg")
    }

    fun createNewVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(mediaDir, "DSLR_VID_${timeStamp}_${UUID.randomUUID().toString().take(5)}.mp4")
    }

    fun createEnhancedImageFile(styleName: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cleanStyle = styleName.replace(" ", "_")
        return File(mediaDir, "DSLR_IMG_${timeStamp}_enhanced_${cleanStyle}.jpg")
    }

    fun createEnhancedVideoFile(styleName: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cleanStyle = styleName.replace(" ", "_")
        return File(mediaDir, "DSLR_VID_${timeStamp}_enhanced_${cleanStyle}.mp4")
    }

    suspend fun saveBitmapToFile(bitmap: Bitmap, targetFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addMediaItem(file: File, isVideo: Boolean, isEnhanced: Boolean = false, style: String? = null): MediaItem = withContext(Dispatchers.IO) {
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        var width = 0
        var height = 0
        var duration = 0L

        if (isVideo) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.absolutePath)
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            } catch (_: Exception) {
            } finally {
                try { retriever.release() } catch (_: Exception) {}
            }
        } else {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            width = options.outWidth
            height = options.outHeight
        }

        val item = MediaItem(
            id = file.name,
            uri = uri,
            filePath = file.absolutePath,
            displayName = file.name,
            dateAdded = file.lastModified(),
            mediaType = if (isVideo) MediaType.VIDEO else MediaType.PHOTO,
            durationMs = duration,
            isEnhanced = isEnhanced,
            enhancedStyle = style,
            width = width,
            height = height,
            sizeBytes = file.length()
        )

        _mediaItems.value = listOf(item) + _mediaItems.value.filter { it.id != item.id }
        item
    }

    suspend fun exportToPublicGallery(item: MediaItem): Uri? = withContext(Dispatchers.IO) {
        val file = File(item.filePath)
        if (!file.exists()) return@withContext null

        try {
            val resolver = context.contentResolver
            val time = System.currentTimeMillis()

            if (item.mediaType == MediaType.PHOTO) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "AI_DSLR_${file.name}")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.DATE_ADDED, time / 1000)
                    put(MediaStore.Images.Media.DATE_TAKEN, time)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/AI_DSLR_Camera")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext null
                resolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { input -> input.copyTo(out) }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                uri
            } else {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, "AI_DSLR_${file.name}")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.DATE_ADDED, time / 1000)
                    put(MediaStore.Video.Media.DATE_TAKEN, time)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/AI_DSLR_Camera")
                        put(MediaStore.Video.Media.IS_PENDING, 1)
                    }
                }

                val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext null
                resolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { input -> input.copyTo(out) }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteMediaItem(item: MediaItem): Boolean = withContext(Dispatchers.IO) {
        val file = File(item.filePath)
        val deleted = if (file.exists()) file.delete() else true
        if (deleted) {
            _mediaItems.value = _mediaItems.value.filter { it.id != item.id }
        }
        deleted
    }

    fun shareMediaItem(item: MediaItem) {
        try {
            val file = File(item.filePath)
            val uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                item.uri
            }

            val mimeType = if (item.mediaType == MediaType.VIDEO) "video/mp4" else "image/jpeg"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
