/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.infomaniak.core.thumbnails

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.BitmapParams
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.core.graphics.scale
import androidx.core.net.toFile
import java.io.File
import kotlin.math.min

object ThumbnailsUtils {

    private const val THUMBNAIL_SIZE = 500 // Image size, in pixels
    private const val SEGMENT_DELIMITER = ":"
    private const val DOCUMENTS_AUTHORITY = "com.android.externalstorage.documents"

    fun Context.getLocalThumbnail(fileUri: Uri, isVideo: Boolean, thumbnailSize: Int = THUMBNAIL_SIZE): Bitmap? {
        return if (SDK_INT >= 29) {
            getThumbnail(fileUri, isVideo, thumbnailSize)
        } else {
            getThumbnailBeforeAndroid29(fileUri, isVideo, thumbnailSize)
        }
    }

    @RequiresApi(29)
    private fun Context.getThumbnail(fileUri: Uri, isVideo: Boolean, thumbnailSize: Int): Bitmap? {
        return runCatching {
            val size = Size(thumbnailSize, thumbnailSize)
            val isSchemeFile = fileUri.scheme?.equals(ContentResolver.SCHEME_FILE) == true
            when {
                isSchemeFile && isVideo -> ThumbnailUtils.createVideoThumbnail(fileUri.toFile(), size, null)
                isSchemeFile && !isVideo -> ThumbnailUtils.createImageThumbnail(fileUri.toFile(), size, null)
                isVideo -> generateVideoThumbnail(fileUri)
                else -> contentResolver.loadThumbnail(fileUri, size, null)
            }
        }.getOrNull()
    }

    private fun Context.getThumbnailBeforeAndroid29(fileUri: Uri, isVideo: Boolean, thumbnailSize: Int): Bitmap? {
        val isSchemeFile = fileUri.scheme?.equals(ContentResolver.SCHEME_FILE) == true
        val localFile = fileUri.lastPathSegment?.split(SEGMENT_DELIMITER)?.let { list -> list.firstOrNull()?.let(::File) }
        val externalRealPath = getExternalRealPath(fileUri, isSchemeFile, localFile)

        return if (isSchemeFile || externalRealPath.isNotBlank()) {
            getBitmapFromPath(fileUri, isVideo, thumbnailSize, externalRealPath)
        } else {
            getBitmapFromFileId(fileUri, thumbnailSize)
        }
    }

    private fun Context.getExternalRealPath(fileUri: Uri, isSchemeFile: Boolean, localFile: File?): String {
        return when {
            !isSchemeFile && localFile?.exists() == true -> localFile.absolutePath
            fileUri.authority == DOCUMENTS_AUTHORITY -> getRealPathFromExternalStorage(this, fileUri)
            else -> ""
        }
    }

    @Suppress("DEPRECATION")
    private fun getBitmapFromPath(fileUri: Uri, isVideo: Boolean, thumbnailSize: Int, externalRealPath: String): Bitmap? {
        val path = externalRealPath.ifBlank { fileUri.path ?: return null }

        return if (isVideo) {
            ThumbnailUtils.createVideoThumbnail(path, Video.Thumbnails.MICRO_KIND)
        } else {
            extractThumbnail(path, thumbnailSize, thumbnailSize)
        }
    }

    @Suppress("DEPRECATION")
    private fun Context.getBitmapFromFileId(fileUri: Uri, thumbnailSize: Int): Bitmap? {

        val fileId = runCatching {
            ContentUris.parseId(fileUri)
        }.getOrElse {
            fileUri.lastPathSegment?.split(SEGMENT_DELIMITER)?.let { it.firstOrNull()?.toLongOrNull() }
        } ?: return null

        val options = BitmapFactory.Options().apply {
            outWidth = thumbnailSize
            outHeight = thumbnailSize
        }

        return if (contentResolver.getType(fileUri)?.contains("video") == true) {
            Video.Thumbnails.getThumbnail(contentResolver, fileId, Video.Thumbnails.MICRO_KIND, options)
        } else {
            Images.Thumbnails.getThumbnail(contentResolver, fileId, Images.Thumbnails.MICRO_KIND, options)
        }
    }

    @Suppress("DEPRECATION")
    private fun getRealPathFromExternalStorage(context: Context, uri: Uri): String {
        // ExternalStorageProvider
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(SEGMENT_DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split.first()
        val relativePath = split.firstOrNull() ?: return ""
        val external = context.externalMediaDirs
        return when {
            type.equals("primary", ignoreCase = true) -> Environment.getExternalStorageDirectory().toString() + "/" + relativePath
            external.size > 1 -> {
                val filePath = external[1].absolutePath
                filePath.substring(0, filePath.indexOf("Android")) + relativePath
            }
            else -> ""
        }
    }

    /**
     * From file path
     */
    @Deprecated(message = "Only for API 28 and below, otherwise use ThumbnailUtils.createImageThumbnail()")
    private fun extractThumbnail(filePath: String, width: Int, height: Int): Bitmap? {
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, bitmapOptions)

        val widthScale = bitmapOptions.outWidth.toFloat() / width
        val heightScale = bitmapOptions.outHeight.toFloat() / height
        val scale = min(widthScale, heightScale)
        var sampleSize = 1
        while (sampleSize < scale) sampleSize *= 2
        bitmapOptions.inSampleSize = sampleSize
        bitmapOptions.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(filePath, bitmapOptions)
    }

    private fun Context.generateVideoThumbnail(fileUri: Uri): Bitmap? {

        val retriever = runCatching { MediaMetadataRetriever() }.getOrNull() ?: return null

        return runCatching {
            retriever.setDataSource(this, fileUri)

            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationString?.toLongOrNull() ?: 0L
            val sampleTime = duration / 2 * 1_000 // Middle of the video in microseconds

            val videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0

            if (videoWidth == 0 || videoHeight == 0) {
                return@runCatching retriever.getFrameAtTime(sampleTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }

            val originalSize = Size(videoWidth, videoHeight)
            val requestedSize = Size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            val targetSize = calculateTargetSize(originalSize, requestedSize)

            return@runCatching if (SDK_INT >= 30) {
                val params = BitmapParams().apply { preferredConfig = Bitmap.Config.ARGB_8888 }
                retriever.getScaledFrameAtTime(
                    sampleTime,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    targetSize.width,
                    targetSize.height,
                    params
                )
            } else {
                retriever.getFrameAtTime(sampleTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?.scale(width = targetSize.width, height = targetSize.height, filter = false)
            }
        }.getOrElse {
            it.printStackTrace()
            return@getOrElse null
        }.also {
            retriever.release()
        }
    }

    private fun calculateTargetSize(originalSize: Size, requestedSize: Size): Size {
        val originalWidth = originalSize.width
        val originalHeight = originalSize.height
        val requestedWidth = requestedSize.width
        val requestedHeight = requestedSize.height

        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

        val targetWidth: Int
        val targetHeight: Int

        if (originalWidth > originalHeight) {
            targetWidth = requestedWidth
            targetHeight = (requestedWidth / aspectRatio).toInt()
        } else {
            targetHeight = requestedHeight
            targetWidth = (requestedHeight * aspectRatio).toInt()
        }

        return Size(min(targetWidth, originalWidth), min(targetHeight, originalHeight))
    }
}
