package com.amebo.amebo.common.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amebo.amebo.R
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.databinding.PaddedBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class PaddedBottomSheetDialog(context: Context, protected val container: ViewGroup) :
    BottomSheetDialog(
        context,
        context.asTheme().bottomSheetDialogThemeRes
    ) {
    private var binding: PaddedBottomSheetBinding
    protected val root: ViewGroup

    var title: String
        get() {
            return binding.title.text.toString()
        }
        set(value) {
            binding.title.text = value
        }

    init {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.padded_bottom_sheet, container, false)
        binding = PaddedBottomSheetBinding.bind(view)
        root = binding.frameLayout
        setContentView(binding.root)
        setOnShowListener {
            addPadding(view)
        }
    }

    final override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
        super.setOnShowListener(listener)
    }

    private fun addPadding(view: View) {
        (view.parent as View).setBackgroundColor(Color.TRANSPARENT)
        val resources = context.resources
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val parent = view.parent as View
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
    }

    final override fun setContentView(view: View) {
        super.setContentView(view)
    }

    final override fun setContentView(layoutResId: Int) {
        super.setContentView(layoutResId)
    }

    final override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
    }

}