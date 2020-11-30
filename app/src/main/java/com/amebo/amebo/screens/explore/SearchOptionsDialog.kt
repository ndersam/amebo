package com.amebo.amebo.screens.explore

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.InjectableBaseDialogFragment
import com.amebo.amebo.databinding.DialogSearchOptionsBinding
import com.amebo.core.domain.Board


class SearchOptionsDialog : InjectableBaseDialogFragment(R.layout.dialog_search_options) {

    private val binding by viewBinding(DialogSearchOptionsBinding::bind)

    private var selectedBoard: Board? = null
        set(value) {
            field = value
            requireArguments().putParcelable(SELECTED_BOARD, value)
            binding.btnSelectBoard.text = field?.name ?: getString(R.string.all_boards)
        }

    private var onlyTopicPosts = false
        set(value) {
            if (field != value) {
                requireArguments().putBoolean(ONLY_TOPICS, value)
                field = value
                binding.chbxOnlyTopicPosts.isChecked = field
            }
        }

    private var onlyImagePosts = false
        set(value) {
            if (field != value) {
                requireArguments().putBoolean(ONLY_IMAGES, value)
                field = value
                binding.chbxOnlyImagePosts.isChecked = field
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_SELECTED_BOARD) { _, bundle ->
            selectedBoard = bundle.getParcelable(FragKeys.BUNDLE_SELECTED_BOARD)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnCancel.setOnClickListener { cancel() }
        binding.btnOkay.setOnClickListener { okay() }
        binding.btnSelectBoard.setOnClickListener { showBoardList() }
        binding.chbxOnlyImagePosts.setOnCheckedChangeListener { _, isChecked ->
            onlyImagePosts = isChecked
        }
        binding.chbxOnlyTopicPosts.setOnCheckedChangeListener { _, isChecked ->
            onlyTopicPosts = isChecked
        }

        // Initial view state
        onlyImagePosts = requireArguments().getBoolean(ONLY_IMAGES)
        onlyTopicPosts = requireArguments().getBoolean(ONLY_TOPICS)
        selectedBoard = requireArguments().getParcelable(SELECTED_BOARD)
    }


    private fun showBoardList() {
        router.toSelectBoardDialog(selectedBoard)
    }

    private fun okay() {
        setFragmentResult(
            FragKeys.RESULT_SEARCH_OPTIONS, bundleOf(
                FragKeys.BUNDLE_ONLY_IMAGES to onlyImagePosts,
                FragKeys.BUNDLE_ONLY_TOPICS to onlyTopicPosts,
                FragKeys.BUNDLE_SELECTED_BOARD to selectedBoard
            )
        )
        dismiss()
    }

    private fun cancel() {
        dismiss()
    }

    companion object {
        private const val SELECTED_BOARD = "selectedBoard"
        private const val ONLY_IMAGES = "onlyImages"
        private const val ONLY_TOPICS = "onlyTopics"
        fun newBundle(onlyTopics: Boolean, onlyImages: Boolean, selectedBoard: Board?) = bundleOf(
            SELECTED_BOARD to selectedBoard,
            ONLY_IMAGES to onlyImages,
            ONLY_TOPICS to onlyTopics
        )
    }

}