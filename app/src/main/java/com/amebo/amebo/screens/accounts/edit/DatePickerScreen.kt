package com.amebo.amebo.screens.accounts.edit

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.PaddedBottomSheetFragment
import com.amebo.amebo.databinding.DatePickerScreenBinding
import com.amebo.core.domain.BirthDate
import com.amebo.core.domain.Day
import com.amebo.core.domain.Month
import com.amebo.core.domain.Year
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*

class DatePickerScreen : PaddedBottomSheetFragment() {
    override val contentResId = R.layout.date_picker_screen

    private val binding by viewBinding(DatePickerScreenBinding::bind)

    private val minYear get() = requireArguments().getParcelable<Year>(MIN_YEAR)!!
    private var selected: BirthDate? = null
        set(value) {
            field = value
            setFragmentResult(
                FragKeys.RESULT_SELECTED_BIRTH_DATE,
                bundleOf(FragKeys.BUNDLE_SELECTED_BIRTH_DATE to value)
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selected = requireArguments().getParcelable(SELECTED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.datePicker.minDate = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
            set(Calendar.YEAR, minYear.value)
        }.timeInMillis

        when (val date = selected) {
            is BirthDate -> {
                binding.datePicker.init(
                    date.year.value,
                    date.month.value - 1,
                    date.day.value,
                    null
                )
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnOkay.setOnClickListener {
            val date = BirthDate(
                day = Day(binding.datePicker.dayOfMonth),
                year = Year(binding.datePicker.year),
                month = Month(binding.datePicker.month + 1, "")
            )
            setFragmentResult(
                FragKeys.RESULT_SELECTED_BIRTH_DATE,
                bundleOf(FragKeys.BUNDLE_SELECTED_BIRTH_DATE to date)
            )
            dismiss()
        }
        binding.btnClear.setOnClickListener {
            setFragmentResult(
                FragKeys.RESULT_SELECTED_BIRTH_DATE,
                bundleOf(FragKeys.BUNDLE_SELECTED_BIRTH_DATE to null)
            )
            dismiss()
        }
    }

    override fun onShowDialog(behavior: BottomSheetBehavior<View>) {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isHideable = false
        behavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

        })
    }

    companion object {
        private const val MIN_YEAR = "START"
        private const val SELECTED = "SELECTED"

        fun newBundle(minYear: Year, date: BirthDate?) =
            bundleOf(MIN_YEAR to minYear, SELECTED to date)
    }
}