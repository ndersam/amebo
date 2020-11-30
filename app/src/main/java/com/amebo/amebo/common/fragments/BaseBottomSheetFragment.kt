package com.amebo.amebo.common.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


abstract class BaseBottomSheetFragment(private val contentResId: Int) :
    BottomSheetDialogFragment() {

    protected lateinit var behaviour: BottomSheetBehavior<View>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(contentResId, container, false)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val sheet =
                    this.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                behaviour = BottomSheetBehavior.from(sheet)
            }
        }
    }

}