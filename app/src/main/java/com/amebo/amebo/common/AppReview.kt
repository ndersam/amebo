package com.amebo.amebo.common

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import com.amebo.amebo.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import java.lang.ref.WeakReference

object AppReview {
    private const val TWO_WEEKS = 1000L * 60 * 60 * 24 * 14

    var snackBar: WeakReference<Snackbar>? = null

    fun schedule(view: View, pref: Pref): Boolean {
        if (pref.canAskForReview && pref.numOfTimesLaunchedApp >= 20 && System.currentTimeMillis() - pref.timeFirstLaunch >= TWO_WEEKS) {
            view.postDelayed({
                Snackbar.make(view, R.string.enjoying_amebo_question, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.not_really) {
                        Snackbar.make(
                            view,
                            R.string.mind_giving_us_feedback,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(R.string.no_thanks) {}
                            .addAction(R.string.ok_sure) {
                                AppUtil.openInStore(it.context)
                            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onShown(transientBottomBar: Snackbar?) {
                                    snackBar = WeakReference(transientBottomBar)
                                }
                            }).show()
                    }
                    .addAction(R.string.yes) {
                        Snackbar.make(view, R.string.how_about_a_rating, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.no_thanks) {}
                            .addAction(R.string.ok_sure) {
                                AppUtil.openInStore(it.context)
                            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onShown(transientBottomBar: Snackbar?) {
                                    snackBar = WeakReference(transientBottomBar)
                                }
                            }).show()
                    }
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onShown(transientBottomBar: Snackbar?) {
                            snackBar = WeakReference(transientBottomBar)
                        }

                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            pref.canAskForReview = false
                        }
                    })
                    .show()
            }, 1000)

            return true
        }
        return false
    }

    private val Snackbar.contentLayout get() = (view as ViewGroup).getChildAt(0) as SnackbarContentLayout

//    private val Snackbar.messageView get() = contentLayout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    private fun Snackbar.addAction(
        @StringRes
        text: Int, listener: View.OnClickListener
    ): Snackbar {
        val params = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        val wrapper =
            ContextThemeWrapper(context, R.style.Widget_MaterialComponents_Button_TextButton)
        MaterialButton(wrapper).apply {
            this.setBackgroundColor(Color.TRANSPARENT)
            this.stateListAnimator = null
            this.layoutParams = params
            this.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            this.setText(text)
            this.setTextColor(context.asTheme().colorAccent)
            this.setOnClickListener {
                listener.onClick(view)
                dismiss()
            }
            contentLayout.addView(this)
        }

        return this
    }
}