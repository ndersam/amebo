package com.amebo.amebo.common.extensions

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.amebo.amebo.R
import com.amebo.amebo.application.MainActivity
import com.google.android.material.snackbar.Snackbar


fun Context.toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


fun CoordinatorLayout.snack(message: Int, vararg actions: Pair<Int, View.OnClickListener>) {
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    actions.forEach { snackBar.setAction(it.first, it.second) }
    snackBar.show()
}

fun Context.shareText(text: String, @StringRes titleRes: Int = R.string.share_link) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(
        Intent.createChooser(
            shareIntent,
            getString(titleRes)
        )
    )
}

inline fun Context.runWithDeepLinkingDisabled(callback: () -> Unit) {
    // disable deepLinking
    val pm = packageManager
    val component = ComponentName(this, MainActivity::class.java)
    pm.setComponentEnabledSetting(
        component,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )

    callback()

    // reEnableDeepLinking
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
        pm.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }, 500)
}

fun Context.forwardToBrowser(intent: Intent) = runWithDeepLinkingDisabled {
    val webIntent = Intent(Intent.ACTION_VIEW).apply {
        data = intent.data
    }
    startActivity(webIntent)
}

/**
 * @return an [Intent] that opens a dialog of email apps present of the device
 */
fun Context.emailAppIntent(): Intent? {
    val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
    val packageManager = applicationContext.packageManager

    val activitiesHandlingEmails = packageManager.queryIntentActivities(emailIntent, 0)
    if (activitiesHandlingEmails.isNotEmpty()) {
        // use the first email package to create the chooserIntent
        val firstEmailPackageName = activitiesHandlingEmails.first().activityInfo.packageName
        val firstEmailInboxIntent = packageManager.getLaunchIntentForPackage(firstEmailPackageName)
        val emailAppChooserIntent = Intent.createChooser(firstEmailInboxIntent, "")

        // created UI for other email packages and add them to the chooser
        val emailInboxIntents = mutableListOf<LabeledIntent>()
        for (i in 1 until activitiesHandlingEmails.size) {
            val activityHandlingEmail = activitiesHandlingEmails[i]
            val packageName = activityHandlingEmail.activityInfo.packageName
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            emailInboxIntents.add(
                LabeledIntent(
                    intent,
                    packageName,
                    activityHandlingEmail.loadLabel(packageManager),
                    activityHandlingEmail.icon
                )
            )
        }
        val extraEmailInboxIntents = emailInboxIntents.toTypedArray()
        return emailAppChooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraEmailInboxIntents)
    } else {
        return null
    }
}

val Context.hasMediaAccess: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


fun Context.dpToPx(dp: Int): Int {
    val density = resources.displayMetrics.density
    return (density * dp + 0.5f).toInt()
}

fun Context.spToPx(sp: Int): Float {
    return sp * resources.displayMetrics.scaledDensity
}

fun Context.contextShowKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun Context.copyTextToClipboard(text: String, label: String = "") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}