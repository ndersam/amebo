package com.amebo.amebo.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.min


object AvatarGenerator {

    private fun String.sha1Num(): BigInteger {
        val md = MessageDigest.getInstance("sha1")
        return BigInteger(1, md.digest(toByteArray()))
    }

    suspend fun getForUser(context: Context, username: String): Bitmap {
        return when (val bitmap = getBitmapIfExists(context, username)) {
            is Bitmap -> bitmap
            else -> generate(context, username)
        }
    }

    /**
     * Source? Where did I copy this from again?
     * @param size - number of pixels left or right, must be in range 5 .. 10
     */
    suspend fun generate(
        context: Context,
        string: String,
        size: Int = 5,
        bitmapSize: Int = 100,
        save: Boolean = true
    ): Bitmap = withContext(Dispatchers.IO) {
        if (size !in 5..10) {
            throw IllegalArgumentException("Size mush be in range 5 .. 10 but passed '${size}'.")
        }
        val hash = string.sha1Num()
        val hashStr = hash.toString(16)
        val hashBin = hash.toString(2)


        // matrix of zeros, dimension = size x size
        val table: Array<Array<Char>> = Array(size) {
            Array(size) { '0' }
        }

        /**The Encoding:
         * every 3rd bit: dots starting from upper-left to lower-right
         * bit 136-143: hue
         * bit 144-151: sat
         * bit 152-159: val (bright)
         *
         * Note that the "acceptable" ranges below were decided empirically
         */
        /**The Encoding:
         * every 3rd bit: dots starting from upper-left to lower-right
         * bit 136-143: hue
         * bit 144-151: sat
         * bit 152-159: val (bright)
         *
         * Note that the "acceptable" ranges below were decided empirically
         */
        var idx = 0
        val ceilMid = (size / 2) + (size % 2) // ceiling of  `double(size)/2 `
        for (i in 0 until size) {
            for (j in 0 until ceilMid) {
                table[i][j] = hashBin[idx]
                table[i][size - 1 - j] = hashBin[idx]
                idx += 3
            }
        }

        val hue = hashStr.substring(34, 36).toInt(16).toDouble() / 256

        // acceptable sat: 45 - 100
        val sat = hashStr.substring(36, 38).toInt(16).toDouble() / 256 * 55 + 45

        // acceptable val: 45-80, depending on sat
        val vRange = if (sat < 60)
            (sat - 45) / 15 * 5 + 25
        else
            (100 - sat) / 40 * 15 + 15

        val vBase = when {
            sat < 70 -> (sat - 45) / 25 * 5 + 45
            sat > 85 -> (sat - 85) / 15 * 5 + 60
            else -> (sat - 70) / 15 * 10 + 50
        }


        val value = hashStr.substring(38, min(40, hashStr.length)).toInt(16)
            .toDouble() / 256 * vRange + vBase
        val color =
            Color.HSVToColor(
                floatArrayOf(
                    hue.toFloat(),
                    sat.toFloat() / 100,
                    value.toFloat() / 100
                )
            )

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val builder = StringBuilder()
        for (i in 0 until size) {
            for (j in 0 until size) {
                builder.append(if (table[i][j] == '1') " O " else "  ")
                bitmap.setPixel(i, j, if (table[i][j] == '1') color else Color.WHITE)
            }
            builder.appendLine()
        }
        Timber.d(builder.toString())

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmapSize, bitmapSize, false)
        if (save) {
            saveImage(context, scaledBitmap, string)
        }
        scaledBitmap
    }

    private fun getAvatarDir(context: Context) =
        File(context.getExternalFilesDir(null), "avatar").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    @Throws(IOException::class)
    private fun saveImage(context: Context, bitmap: Bitmap, name: String) {
        val avatarDir = getAvatarDir(context)
        val destFile = File(avatarDir, "$name.png")
        val fOut = FileOutputStream(destFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.close()
        Timber.d(destFile.absolutePath)
    }

    private fun isBitmapExists(context: Context, name: String): Boolean {
        val file = File(getAvatarDir(context), "$name.png")
        return file.exists()
    }

    private fun getBitmap(context: Context, name: String): Bitmap {
        val file = File(getAvatarDir(context), "$name.png")
        val bmOptions = BitmapFactory.Options()
        return BitmapFactory.decodeFile(file.absolutePath, bmOptions)
    }

    private suspend fun getBitmapIfExists(context: Context, name: String): Bitmap? =
        withContext(Dispatchers.IO) {
            if (isBitmapExists(context, name)) getBitmap(context, name) else null
        }
}