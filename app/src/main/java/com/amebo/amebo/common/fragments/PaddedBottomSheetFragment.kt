package com.amebo.amebo.common.fragments

import android.app.Dialog
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.amebo.amebo.R
import com.amebo.amebo.common.asTheme
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


abstract class PaddedBottomSheetFragment : BottomSheetDialogFragment() {

    @get:LayoutRes
    open val contentResId: Int = R.layout.padded_bottom_sheet
    private var frameLayout: FrameLayout? = null
    private var txtTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(contentResId, container, false)!!
        if (contentResId == R.layout.padded_bottom_sheet) {
            txtTitle = view.findViewById(R.id.title)
            frameLayout = view.findViewById(R.id.frame_layout)
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val sheet =
                    this.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                val behaviour = BottomSheetBehavior.from(sheet)
                onShowDialog(behaviour)
            }
        }.also {
            it.setCanceledOnTouchOutside(true)
        }
    }

    open fun onShowDialog(behavior: BottomSheetBehavior<View>) {

    }

    override fun getTheme(): Int {
        return requireContext().asTheme().bottomSheetDialogThemeRes
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setPadding(view)
        val coordinatorLayout = view.parent.parent as ViewGroup
        val background =
            coordinatorLayout.findViewById<View>(com.google.android.material.R.id.touch_outside)
        background.setBackgroundColor(requireContext().asTheme().bottomSheetTouchOutSideColor)
    }

    private fun setPadding(view: View) {
        val parent = view.parent as View
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val layoutParams = parent.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(
                resources.getDimensionPixelSize(R.dimen.medium),
                0,
                resources.getDimensionPixelSize(R.dimen.medium),
                resources.getDimensionPixelSize(R.dimen.medium)
            )
            parent.layoutParams = layoutParams
            val thisParams = view.layoutParams as ViewGroup.MarginLayoutParams
            thisParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.medium)
        }
        parent.setBackgroundColor(
            requireContext().asTheme().bottomSheetTouchOutSideColor
        )
        parent.backgroundTintList = ColorStateList(arrayOf(), intArrayOf())
    }

    fun <T : ViewBinding> inflate(
        factory: (LayoutInflater, ViewGroup, Boolean) -> T,
        @StringRes title: Int
    ): T {
        return inflate(factory, requireContext().getString(title))
    }

    fun <T : ViewBinding> inflate(
        factory: (LayoutInflater, ViewGroup, Boolean) -> T,
        title: String
    ): T {
        val binding = factory(layoutInflater, frameLayout!!, false)
        frameLayout!!.addView(binding.root)
        txtTitle!!.text = title
        return binding
    }
}