package com.amebo.amebo.screens.newpost.quotepost

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.InjectableBaseDialogFragment
import com.amebo.amebo.databinding.FragmentPostPickerBinding
import com.amebo.core.domain.QuotablePost

class PostPickerScreen : InjectableBaseDialogFragment(R.layout.fragment_post_picker),
    ItemAdapter.Listener {

    private val posts get() = requireArguments().getParcelableArrayList<QuotablePost>(POSTS)!!
    private val binding by viewBinding(FragmentPostPickerBinding::bind)

    private var adapter: ItemAdapter? = null
    override val resizeView: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ItemAdapter(posts, this)
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override val listenerLifecycle: Lifecycle
        get() = viewLifecycleOwner.lifecycle

    override val useDeviceEmojis: Boolean
        get() = pref.useDeviceEmojis

    override fun onPostSelected(quotablePost: QuotablePost) {
        setFragmentResult(
            FragKeys.RESULT_QUOTABLE_POST,
            bundleOf(FragKeys.BUNDLE_QUOTABLE_POST to quotablePost)
        )
        dismiss()
    }

    companion object {
        private const val POSTS = "POSTS"

        fun bundle(posts: List<QuotablePost>) = bundleOf(POSTS to ArrayList<QuotablePost>(posts))
    }
}