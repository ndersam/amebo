package com.amebo.amebo.screens.accounts.edit

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.setDrawableEnd
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.PaddedBottomSheetFragment
import com.amebo.amebo.databinding.GenderPickerScreenBinding
import com.amebo.core.domain.Gender

class GenderPickerScreen : PaddedBottomSheetFragment() {
    override val contentResId = R.layout.gender_picker_screen
    private val binding by viewBinding(GenderPickerScreenBinding::bind)

    companion object {
        private const val GENDER = "GENDER"
        fun newBundle(gender: Gender?) = bundleOf(GENDER to gender?.name)
    }

    private val gender get(): Gender? {
        return Gender.valueOf(arguments?.getString(GENDER) ?: return null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when(gender){
            Gender.Female -> {
                binding.female.setDrawableEnd(R.drawable.ic_done_24dp)
            }
            Gender.Male -> {
                binding.male.setDrawableEnd(R.drawable.ic_done_24dp)
            }
            else -> {
                binding.unSelected.setDrawableEnd(R.drawable.ic_done_24dp)
            }
        }
        binding.male.setOnClickListener { setResult(Gender.Male) }
        binding.female.setOnClickListener { setResult(Gender.Female) }
        binding.unSelected.setOnClickListener { setResult(null) }
    }

    private fun setResult(gender: Gender?) {
        setFragmentResult(FragKeys.RESULT_GENDER, bundleOf(FragKeys.BUNDLE_GENDER to gender?.name))
        dismiss()
    }
}