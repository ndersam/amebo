package com.amebo.amebo.common.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max


@Throws(IOException::class)
fun Uri.getBitmap(context: Context, reqHeight: Int = 512, reqWidth: Int = 512): Pair<Bitmap, Int> {
    val afd = context.contentResolver.openAssetFileDescriptor(this, "r")!!
    val bitmapByteCount = afd.length.toInt()

    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFileDescriptor(afd.fileDescriptor, null, this)

        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        inJustDecodeBounds = false
        val scaled = BitmapFactory.decodeFileDescriptor(afd.fileDescriptor, null, this)
        afd.close()

        scaled to bitmapByteCount
    }
}

@Throws(IOException::class)
fun Uri.getBitmapOriginal(context: Context): Pair<Bitmap, Int> {
    val afd = context.contentResolver.openAssetFileDescriptor(this, "r")!!
    val bitmapByteCount = afd.length.toInt()

    val image = BitmapFactory.decodeFileDescriptor(afd.fileDescriptor)
    afd.close()
    return image to bitmapByteCount
}

/**
 * @return [Pair] of Height to Width
 */
private fun calculateOutDimensions(
    reqHeight: Int,
    reqWidth: Int,
    inHeight: Int,
    inWidth: Int
): Pair<Int, Int> {
    val maxSize: Int = max(reqHeight, reqWidth)
    val outWidth: Int
    val outHeight: Int
    if (inWidth > inHeight) {
        outWidth = maxSize
        outHeight = inHeight * maxSize / inWidth
    } else {
        outHeight = maxSize
        outWidth = inWidth * maxSize / inHeight
    }
    return outHeight to outWidth
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

/**
 * https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework/20402190#answer-20470572
 */
fun Uri.getPath(context: Context): String {

    var documentId: String? = null
    context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            documentId = cursor.getString(0)
            documentId = documentId!!.substring(documentId!!.lastIndexOf(":") + 1)
        }
    }

    checkNotNull(documentId)
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Images.Media._ID + " = ? ", arrayOf(documentId), null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        }
    }

    // If it's a remote path
    val path = getRemoteFilePath(this, context)
    if (path != null) {
        return path
    }

    throw IllegalStateException("Unable to find path for `${this}`")
}


private fun getRemoteFilePath(
    uri: Uri,
    context: Context
): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        if (cursor.moveToFirst()) {
            val name = cursor.getString(nameIndex)
            val file = File(context.cacheDir, name)
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                val outputStream = FileOutputStream(file)
                var read: Int
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable: Int = inputStream.available()

                val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                Timber.d("File Size: ${file.length()}")
                inputStream.close()
                outputStream.close()
                return file.path
            } catch (e: IOException) {
                Timber.e(e)
            }
        }

    }

    return null
}