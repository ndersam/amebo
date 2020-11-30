package com.amebo.amebo.screens.topiclist

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseBottomSheetFragment
import com.amebo.amebo.databinding.DialogTopiclistPageSelectionBinding
import com.amebo.amebo.di.Injectable
import com.amebo.core.domain.BaseTopicListDataPage
import com.amebo.core.domain.TopicList
import javax.inject.Inject


class TopicListPageSelectionDialog :
    BaseBottomSheetFragment(R.layout.dialog_topiclist_page_selection),
    TopicListPageSelectionDialogView.Listener, Injectable {

    @Inject
    lateinit var pref: Pref

    companion object {
        private const val PAGE = "page"
        private const val LAST_PAGE = "lastPage"
        private const val TOPIC_LIST = "topicList"
        private const val TOPIC_LIST_DATA_PAGE = "topicListDataPage"

        fun newBundle(
            topicList: TopicList,
            baseTopicListDataPage: BaseTopicListDataPage,
            page: Int,
            lastPage: Int
        ) =
            bundleOf(
                TOPIC_LIST to topicList,
                PAGE to page,
                LAST_PAGE to lastPage,
                TOPIC_LIST_DATA_PAGE to baseTopicListDataPage
            )
    }

    private val binding by viewBinding(DialogTopiclistPageSelectionBinding::bind)
    private val page: Int get() = requireArguments().getInt(PAGE) + 1
    private val lastPage by lazy { requireArguments().getInt(LAST_PAGE) + 1 }
    private val topicList get() = requireArguments().getParcelable<TopicList>(TOPIC_LIST)!!
    private val baseTopicListDataPage
        get() = requireArguments().getParcelable<BaseTopicListDataPage>(TOPIC_LIST_DATA_PAGE)!!

    private lateinit var dialogView: TopicListPageSelectionDialogView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialogView = TopicListPageSelectionDialogView(
            currentPage = page,
            lastPage = lastPage,
            topicList = topicList,
            dataPage = baseTopicListDataPage,
            binding = binding,
            listener = this
        )
    }

    override val isLoggedIn: Boolean get() = pref.isLoggedIn

    override fun onNewTopicClick() {
        setFragmentResult(FragKeys.RESULT_ACTION_NEW_TOPIC, bundleOf())
        dismiss()
    }

    override fun onFollowBoardClick() {
        setFragmentResult(FragKeys.RESULT_ACTION_FOLLOW_BOARD, bundleOf())
    }

    override fun onVisitPageClick() {
        setFragmentResult(
            FragKeys.RESULT_TOPIC_LIST_META, bundleOf(
                FragKeys.BUNDLE_SELECTED_PAGE to (dialogView.currentPageSelection - 1) // to  zero-indexed counting
            )
        )
        dismiss()
    }

}