package com.amebo.amebo.screens.newpost.newpost

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.snack
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.NewPostScreenBinding
import com.amebo.amebo.screens.newpost.FormScreen
import com.amebo.amebo.screens.newpost.IFormView
import com.amebo.core.domain.NewPostForm
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.SimplePost
import com.amebo.core.domain.Topic
import javax.inject.Inject
import javax.inject.Provider

class NewPostScreen : FormScreen<NewPostForm>(R.layout.new_post_screen), NewPostView.Listener {

    val binding: NewPostScreenBinding by viewBinding(NewPostScreenBinding::bind)
    val topic get() = requireArguments().getParcelable<Topic>(TOPIC)!!
    val post get() = requireArguments().getParcelable<SimplePost>(POST)

    @Inject
    lateinit var formViewProvider: Provider<NewPostView>

    override val viewModel by viewModels<NewPostScreenViewModel>()

    override lateinit var formView: IFormView

    override val showQuotedPostAction: Boolean = true

    override fun initializeViews() {
        formView = formViewProvider.get()
    }

    override fun initializeViewModelState() {
        viewModel.initialize(topic, post)
    }

    override fun setFollowTopic(followTopic: Boolean) {
        viewModel.formData.followTopic = followTopic
    }

    override fun onSubmissionSuccess(postListDataPage: PostListDataPage) {
        super.onSubmissionSuccess(postListDataPage)
        router.back()
    }

    override fun quotePost() {
        router.toPostPicker(viewModel.quotablePosts)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_QUOTABLE_POST) { _, bundle ->
            viewModel.getPost(bundle.getParcelable(FragKeys.BUNDLE_QUOTABLE_POST)!!)
        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        viewModel.getPostEvent.observe(viewLifecycleOwner, EventObserver(::onGetPostEvent))
        super.onViewCreated(savedInstanceState)
    }

    private fun onGetPostEvent(resource: Resource<String>) {
        when (resource) {
            is Resource.Success -> {
                formView.insertText(resource.content)
            }
            is Resource.Error -> {
                binding.root.snack(resource.cause)
            }
            is Resource.Loading -> {

            }
        }
    }

    companion object {
        private const val TOPIC = "topic"
        private const val POST = "quotedPost"

        fun newBundle(post: SimplePost) = bundleOf(POST to post, TOPIC to post.topic)
        fun newBundle(topic: Topic) = bundleOf(TOPIC to topic)
    }
}
