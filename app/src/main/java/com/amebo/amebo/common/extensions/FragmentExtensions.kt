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
import androidx.lifecycle.Observer
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
            val viewLifecycleOwnerLiveDataObserver =
                Observer<LifecycleOwner?> {
                    val viewLifecycleOwner = it ?: return@Observer

                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }

            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observeForever(
                    viewLifecycleOwnerLiveDataObserver
                )
            }

            override fun onDestroy(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.removeObserver(
                    viewLifecycleOwnerLiveDataObserver
                )
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
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


fun DialogFragment.resizeWindowOnResume(widthPercent: Float = 0.95F) {
    val window = dialog?.window ?: return
    val size = Point().apply {
        window.windowManager.defaultDisplay.getSize(this)
    }

    val width = size.x
    window.setLayout((width * widthPercent).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    window.setGravity(Gravity.CENTER)
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
