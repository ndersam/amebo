package com.amebo.amebo.screens.rules

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.htmlFromText
import com.amebo.amebo.common.extensions.resize
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.FragmentRulesScreenBinding
import com.amebo.amebo.di.Injectable
import com.amebo.core.extensions.openRawAsString
import javax.inject.Inject


class RulesScreen : DialogFragment(R.layout.fragment_rules_screen), Injectable {

    @Inject
    lateinit var pref: Pref

    private val binding by viewBinding(FragmentRulesScreenBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.textView.htmlFromText(requireContext().openRawAsString(R.raw.rules))
        binding.btnAccept.isVisible = pref.acceptedNairalandRules.not()
        binding.btnDismiss.isVisible = pref.acceptedNairalandRules
        binding.btnDismiss.setOnClickListener { dismiss() }
        binding.btnAccept.setOnClickListener {
            pref.acceptedNairalandRules = true
            dismiss()
        }
    }

    override fun onResume() {
        resize(0.95f)
        super.onResume()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RESULT_RULES, bundleOf(RESULT_RULES to pref.acceptedNairalandRules))
    }

    companion object {
        const val RESULT_RULES = "RESULT_RULES"
    }
}
