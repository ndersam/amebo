package com.amebo.amebo.screens.reportpost

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.hideKeyboard
import com.amebo.amebo.common.extensions.htmlFromText
import com.amebo.amebo.common.extensions.snack
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.ReportPostScreenBinding
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.SimplePost
import com.amebo.core.extensions.openRawAsString

class ReportPostScreen : BaseFragment(R.layout.report_post_screen) {

    private val binding by viewBinding(ReportPostScreenBinding::bind)
    private val viewModel by viewModels<ReportPostSharedScreenViewModel>()
    private val post get() = requireArguments().getParcelable<SimplePost>(POST)!!
    private var isPostExpanded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPostExpanded = false
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.submit -> viewModel.submit()
                else -> return@setOnMenuItemClickListener false
            }
            true
        }
        binding.toolbar.setNavigationOnClickListener { router.back() }

        binding.editBody.doOnTextChanged { text, _, _, _ ->
            viewModel.data.reason = text.toString()
            requireActivity().invalidateOptionsMenu()
        }
        binding.txtRules.htmlFromText(requireContext().openRawAsString(R.raw.rules))
        binding.postBody.htmlFromText(post.text)
        binding.postHeader.setOnClickListener {
            isPostExpanded = !isPostExpanded
            if (isPostExpanded) {
                ViewCompat.animate(binding.postHeaderArrow)
                    .rotation(arrowRotationAngleEnd)
                    .start()
                binding.postHeaderText.text = post.author.name
            } else {
                binding.postHeaderText.setText(R.string.post_being_reported)
                ViewCompat.animate(binding.postHeaderArrow)
                    .rotation(arrowRotationAngleStart)
                    .start()
            }
            setPostExpanded()
        }

        viewModel.initialize(post)
        viewModel.formLoadEvent.observe(
            viewLifecycleOwner,
            EventObserver(::handleFormLoadContent)
        )
        viewModel.submissionEvent.observe(
            viewLifecycleOwner,
            EventObserver(::handleSubmissionContent)
        )
    }

    override fun onStop() {
        hideKeyboard()
        super.onStop()
    }


    private fun handleFormLoadContent(data: ReportPostFormData) {
        binding.editBody.setText(data.reason)
        setPostExpanded()
    }

    private fun handleSubmissionContent(resource: Resource<PostListDataPage>) {
        when (resource) {
            is Resource.Loading -> {
                invalidateOptionsMenu()
            }
            is Resource.Success -> {
                setFragmentResult(
                    FragKeys.RESULT_POST_LIST,
                    bundleOf(FragKeys.BUNDLE_POST_LIST to resource.content)
                )
                router.back()
            }
            is Resource.Error -> {
                invalidateOptionsMenu()
                binding.snack(resource.cause)
            }
        }
    }

    private fun invalidateOptionsMenu() {
        binding.toolbar.menu.findItem(R.id.submit).isEnabled = viewModel.canSubmit
    }

    private fun setPostExpanded() {
        val anim = ViewCompat.animate(binding.postBody)
        if (isPostExpanded) {
            anim.alpha(1f).withEndAction { binding.postBody.isVisible = true }
        } else {
            anim.alpha(0f).withEndAction { binding.postBody.isVisible = false }
        }
    }

    companion object {
        private const val POST = "post"
        private const val arrowRotationAngleEnd = 180f
        private const val arrowRotationAngleStart = 0f
        fun newBundle(post: SimplePost) = bundleOf(POST to post)
    }

}
