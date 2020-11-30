package com.amebo.amebo.screens.postlist.topicloader

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.amebo.amebo.R
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentTopicLoaderScreenBinding
import com.amebo.core.domain.TopicPostListDataPage

class TopicLoaderScreen : BaseFragment(R.layout.fragment_topic_loader_screen) {

    private val binding by viewBinding(FragmentTopicLoaderScreenBinding::bind)

    private val postId get() = requireArguments().getString(POST)!!

    private val viewModel by viewModels<TopicLoaderScreenViewModel>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { router.back() }
        binding.ouchView.setButtonClickListener { viewModel.load(postId) }

        viewModel.event.observe(viewLifecycleOwner, Observer(::onResource))
        viewModel.load(postId)
    }

    private fun onResource(resource: Resource<TopicPostListDataPage>) {
        when (resource) {
            is Resource.Success -> {
                router.toTopic(resource.content.topic, replace = true, data = resource.content)
            }
            is Resource.Error -> {
                binding.ouchView.setState(resource.cause)
                binding.stateLayout.failure()
            }
            is Resource.Loading -> binding.stateLayout.loading()
        }
    }

    companion object {
        private const val POST = "POST_ID"

        fun newBundle(postId: String) = bundleOf(POST to postId)
    }
}