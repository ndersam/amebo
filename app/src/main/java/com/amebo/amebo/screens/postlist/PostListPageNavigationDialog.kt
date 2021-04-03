package com.amebo.amebo.screens.postlist

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.cursorAtEnd
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseBottomSheetDialogFragment
import com.amebo.amebo.databinding.PostlistPageNavigationDialogBinding

class PostListPageNavigationDialog :
    BaseBottomSheetDialogFragment(R.layout.postlist_page_navigation_dialog) {

    private val binding: PostlistPageNavigationDialogBinding by viewBinding(
        PostlistPageNavigationDialogBinding::bind
    )
    private val lastPage get() = requireArguments().getInt(LAST_PAGE) + 1
    private val page get() = requireArguments().getInt(PAGE) + 1

    private var selectedPage: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectedPage = page
        binding.editLastPage.setText(lastPage.toString())
        binding.btnPrevPage.isEnabled = selectedPage > 1
        binding.btnNextPage.isEnabled = selectedPage < lastPage

        binding.editPage.setText(selectedPage.toString())
        binding.editPage.cursorAtEnd()
        binding.editPage.doOnTextChanged { text, _, _, _ ->
            val parsedNum = text.toString().toIntOrNull()
            setPage(parsedNum, setText = false)
        }
        binding.editPage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO && binding.btnVisitPage.isEnabled) {
                setResult()
                true
            } else {
                false
            }
        }

        binding.btnPrevPage.setOnClickListener {
            // if invalid maxPage, jump to maxValidValue instead of decrementing
            val page = selectedPage - 1
            setPage(if (selectedPage > lastPage) lastPage else page)
        }
        binding.btnNextPage.setOnClickListener {
            // if invalid minPage, jump to minValidValue instead of incrementing
            val page = selectedPage + 1
            setPage(if (selectedPage < 1) 1 else page)
        }
        binding.btnPrevPage.setOnLongClickListener {
            setPage(1)
            true
        }
        binding.btnNextPage.setOnLongClickListener {
            setPage(lastPage)
            true
        }
        binding.btnVisitPage.setOnClickListener {
            setResult()
        }
    }

    private fun setResult() {
        setFragmentResult(
            FragKeys.RESULT_SELECTED_PAGE,
            bundleOf(FragKeys.BUNDLE_SELECTED_PAGE to selectedPage - 1) // added this when obtaining arguments from bundle
        )
        dismiss()
    }

    private fun setPage(page: Int?, setText: Boolean = true) {
        when (page) {
            is Int -> {
                selectedPage = page
                if (setText) {
                    binding.editPage.setText(page.toString())
                    binding.editPage.cursorAtEnd()
                }
                binding.btnPrevPage.isEnabled = page > 1
                binding.btnNextPage.isEnabled = page < lastPage
                binding.btnVisitPage.isEnabled = page in 1..lastPage
                binding.editPage.imeOptions =
                    if (page in 1..lastPage) EditorInfo.IME_ACTION_GO else EditorInfo.IME_ACTION_NONE
            }
            else -> {
                binding.editPage.imeOptions = EditorInfo.IME_ACTION_NONE
                binding.btnVisitPage.isEnabled = false
            }
        }
    }

    companion object {
        private const val PAGE = "page"
        private const val LAST_PAGE = "lastPage"
        fun newBundle(page: Int, lastPage: Int) = bundleOf(PAGE to page, LAST_PAGE to lastPage)
    }
}