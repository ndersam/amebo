package com.amebo.amebo.common.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        check(lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) { "Should not attempt to get bindings when Fragment views are destroyed." }

        return viewBindingFactory(thisRef.requireView()).also {
            this@FragmentViewBindingDelegate.binding = it
        }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

fun Fragment.hideKeyboard() {
    val mgr = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = requireActivity().currentFocus
    currentFocus?.let {
        mgr.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}

fun Fragment.showKeyboard() {
    (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.also { inputMethodManager ->
            view?.rootView?.apply { post { inputMethodManager.showSoftInput(this, 0) } }
                ?: inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
}


fun DialogFragment.resizeWindowOnResume(widthPercent: Float = 0.95F) {
    val window = dialog?.window ?: return
    val size = Point().apply {
        window.windowManager.defaultDisplay.getSize(this)
    }

    val width = size.x
    window.setLayout((width * widthPercent).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    window.setGravity(Gravity.CENTER)
}

fun BottomSheetDialog.makeFullScreen(activity: Activity) {

    fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    val bottomSheet =
        findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return
    val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
    val layoutParams = bottomSheet.layoutParams
    val windowHeight = getWindowHeight()
    if (layoutParams != null) {
        layoutParams.height = windowHeight
    }
    bottomSheet.layoutParams = layoutParams
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
}


@Suppress("DEPRECATION")
fun DialogFragment.resize(percent: Float) {
    val window = dialog?.window ?: return
    val size = Point()
    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        requireContext().display!!
    } else {
        val mgr = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mgr.defaultDisplay
    }
    display.getSize(size)
    window.setLayout((size.x * percent).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    window.setGravity(Gravity.CENTER)
}
