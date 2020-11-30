package com.amebo.amebo.screens.newpost.modifypost

import androidx.core.os.bundleOf
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.NewPostScreenBinding
import com.amebo.amebo.screens.newpost.FormScreen
import com.amebo.amebo.screens.newpost.IFormView
import com.amebo.core.domain.ModifyForm
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.SimplePost
import com.amebo.core.domain.Topic
import javax.inject.Inject
import javax.inject.Provider

class ModifyPostScreen : FormScreen<ModifyForm>(R.layout.new_post_screen) {

    val binding: NewPostScreenBinding by viewBinding(NewPostScreenBinding::bind)
    val topic get() = requireArguments().getParcelable<Topic>(TOPIC)!!
    private val post get() = requireArguments().getParcelable<SimplePost>(POST)!!

    override val viewModel by viewModels<ModifyPostScreenViewModel>()

    @Inject
    lateinit var formViewProvider: Provider<ModifyPostView>

    override lateinit var formView: IFormView

    override fun initializeViews() {
        formView = formViewProvider.get()
    }

    override fun initializeViewModelState() = viewModel.initialize(post)


    override fun onSubmissionSuccess(postListDataPage: PostListDataPage) {
        super.onSubmissionSuccess(postListDataPage)
        router.back()
    }

    companion object {
        private const val TOPIC = "topic"
        private const val POST = "quotedPost"

        fun newBundle(post: SimplePost) = bundleOf(POST to post, TOPIC to post.topic)
    }
}
