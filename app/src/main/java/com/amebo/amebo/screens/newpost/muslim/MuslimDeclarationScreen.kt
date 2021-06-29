package com.amebo.amebo.screens.newpost.muslim

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.snack
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.MuslimDeclarationScreenBinding
import com.amebo.core.common.extensions.openRawAsString
import com.amebo.core.domain.AreYouMuslimDeclarationForm

class MuslimDeclarationScreen : BaseFragment(R.layout.muslim_declaration_screen),
    AuthenticationRequired {

    private val binding by viewBinding(MuslimDeclarationScreenBinding::bind)
    private val viewModel by viewModels<MuslimDeclarationScreenViewModel>()
    private val form get() = requireArguments().getParcelable<AreYouMuslimDeclarationForm>(FORM)!!

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.txtStatement.text = HtmlCompat.fromHtml(
            requireContext().openRawAsString(R.raw.muslim_declaration),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.toolbar.setNavigationOnClickListener { router.back() }
        binding.rbtnGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rbtnAccept -> {
                    form.accepted = true
                }
                R.id.rbtnDecline -> {
                    form.accepted = false
                }
                else -> throw IllegalStateException("Unknown checkId '${checkedId}'")
            }
            binding.submit.isEnabled = true
        }
        binding.submit.setOnClickListener {
            viewModel.submit(form)
        }

        viewModel.submissionEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onSubmissionEvent)
        )
    }

    private fun onSubmissionEvent(resource: Resource<SubmissionResult>) {
        when (resource) {
            is Resource.Success -> {
                router.back()
            }
            is Resource.Error -> {
                binding.snack(resource.cause)
                binding.submit.isEnabled = true
            }
            is Resource.Loading -> {
                binding.submit.isEnabled = false
            }
        }
    }

    companion object {
        private const val FORM = "FORM"
        fun newBundle(form: AreYouMuslimDeclarationForm) = bundleOf(FORM to form)
    }
}
