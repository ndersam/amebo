package com.amebo.core.extensions

import android.content.Context
import androidx.annotation.RawRes
import java.io.IOException
import java.io.InputStream

@Throws(IOException::class)
fun Context.openRawAsString(filename: String): String {
    val stream: InputStream = resources.openRawResource(
        resources.getIdentifier(
            filename,
            "raw", packageName
        )
    )
    val bytes = ByteArray(stream.available())
    stream.read(bytes, 0, stream.available())
    return String(bytes)
}

@Throws(IOException::class)
fun Context.openRawAsString(@RawRes rawRes: Int): String {
    val stream: InputStream = resources.openRawResource(rawRes)
    val bytes = ByteArray(stream.available())
    stream.read(bytes, 0, stream.available())
    return String(bytes)
}