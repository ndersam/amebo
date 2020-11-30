package com.amebo.amebo.common

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.runWithDeepLinkingDisabled
import com.amebo.amebo.common.extensions.setColor
import com.amebo.core.CoreUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.net.URL
import java.util.regex.Pattern


object AppUtil {
    private val YOUTUBE_RE = Pattern.compile(
        "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
    )

    private fun isYoutubeUrl(url: String): Boolean {
        return try {
            val urlObj = URL(url)
            (arrayOf("youtube.com", "youtu.be", "www.youtube.com", "www.youtu.be").contains(
                urlObj.host
            ))
        } catch (ex: Exception) {
            false
        }
    }

    fun parseYoutubeUrl(url: String): String? {
        if (!isYoutubeUrl(url)) {
            return null
        }
        val matcher =
            YOUTUBE_RE.matcher(url) //url is youtube url for which you want to extract the id.
        if (matcher.find()) {
            return matcher.group()
        }
        return null
    }

    fun openKeyboard(view: View) {
        if (view.requestFocus()) {
            val context = view.context
            val mgr = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            mgr.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethodManager.SHOW_FORCED,
                0
            )
            mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun closeKeyboard(view: View) {
        val mgr = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.hideSoftInputFromWindow(view.windowToken, 0)
    }


    @JvmOverloads
    @JvmStatic
    fun progressDrawable(
        context: Context,
        radius: Float = 40f,
        stroke: Float = 5f,
        @ColorInt color: Int? = null
    ): CircularProgressDrawable {
        val drawable = CircularProgressDrawable(context)
        drawable.strokeWidth = stroke
        drawable.centerRadius = radius
        if (color != null) {
            drawable.setColor(color)
        } else {
            val theme = context.asTheme()
            drawable.setColor(if (theme.isDark) Color.WHITE else Color.BLACK)
        }
        return drawable
    }

    fun loadImage(image: ImageView, url: String, placeholder: Drawable) {
        Glide.with(image)
            .load(url)
            .placeholder(placeholder)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(image)
            .waitForLayout()
    }

    @Suppress("deprecation")
    suspend fun saveImage(
        context: Context,
        url: String,
        directory: String = Environment.DIRECTORY_PICTURES,
        mimeType: String = "image/jpeg"
    ): String? = withContext(Dispatchers.IO) {

        val cached = Glide.with(context)
            .downloadOnly()
            .load(url)
            .submit()
            .get()
        val srcInputStream = FileInputStream(cached)

        val imageOutStream: OutputStream
        val filename = CoreUtils.dateTimeToday + ".jpeg"
        val destinationPath: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, directory)
            }
            val mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val contentResolver = context.contentResolver
            contentResolver.run {
                val uri = contentResolver.insert(mediaContentUri, values) ?: return@withContext null
                imageOutStream = openOutputStream(uri) ?: return@withContext null
                destinationPath = uri.toString()
            }
        } else {
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(directory),
                "amebo"
            ).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            val destFile = File(storageDir, filename)
            destinationPath = destFile.absolutePath
            imageOutStream = FileOutputStream(destFile)
        }

        imageOutStream.use {
            srcInputStream.copyTo(it)
        }
        destinationPath
    }

    @Suppress("deprecation")
    fun addToGallery(context: Context, path: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                val file = File(path)
                val uri = Uri.fromFile(file)
                data = uri
            })
        }
    }


    fun shareImage(context: Context, drawable: Drawable): Boolean {
        val bmpUri = getLocalBitmapUri(context, drawable) ?: return false
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, bmpUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "image/*"
        }
        val chooser = Intent.createChooser(
            shareIntent,
            context.getString(R.string.share_image)
        )

        // For permission denial issue
        // https://stackoverflow.com/questions/45893294/permission-denial-with-file-provider-through-intent
        context.packageManager
            .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            .forEach {
                val packageName: String = it.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    bmpUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        context.startActivity(chooser)
        return true
    }

    /**
     * Downloads bitmap from drawable into a local cache directory and returns the uri of the
     * created file
     */
    private fun getLocalBitmapUri(context: Context, drawable: Drawable): Uri? {
        val bmp = (drawable as? BitmapDrawable)?.bitmap ?: return null

        // Store in 'cache/images'
        // 'images' must be added as a cache-path in res/xml/file_provider
        try {
            val directory = File(context.cacheDir, "images").apply {
                if (exists().not()) mkdirs()
            }
            val file = File(directory, "share_image_" + System.currentTimeMillis() + ".png")
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                FileProvider.getUriForFile(context, context.getString(R.string.fileprovider), file)
            else
                Uri.fromFile(file)
        } catch (e: IOException) {
            Timber.e(e)
        }
        return null
    }

    fun openInCustomTabs(context: Context, url: String) {
        context.runWithDeepLinkingDisabled {
            CustomTabsIntent.Builder().setToolbarColor(context.asTheme().colorPrimary)
                .build()
                .launchUrl(context, Uri.parse(url))
        }
    }

    fun largeNumberFormatter(count: Int) = when {
        count >= 1_000_000 -> "${count / 1_000_000}m"
        count >= 1_000 -> "${count / 1_000}k"
        else -> count.toString()
    }

    fun openInStore(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=" + context.packageName)
        )
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
                )
            )
        }
    }
}