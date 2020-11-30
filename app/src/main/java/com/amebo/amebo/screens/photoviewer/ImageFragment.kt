package com.amebo.amebo.screens.photoviewer

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.common.extensions.getBitmapOriginal
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentImageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageFragment : BaseFragment(R.layout.fragment_image), RequestListener<Drawable> {

    private val binding: FragmentImageBinding by viewBinding(FragmentImageBinding::bind)
    private val toolbarOwner get() = (parentFragment as? ToolbarOwner)
    private lateinit var progressDrawable: CircularProgressDrawable
    private val viewModel by activityViewModels<PhotoSharedViewModel>()


    override fun onViewCreated(savedInstanceState: Bundle?) {
        with(binding.photoView) {
            transitionName = requireArguments().getString(TRANSITION_NAME, null)!!
            setOnClickListener { toggleToolbarVisibility() }

            progressDrawable = AppUtil.progressDrawable(requireContext()).apply {
                setColorSchemeColors(requireContext().asTheme().colorBackground)
                start()
            }
            val link = requireArguments().getString(LINK, null)
            val contentUri = requireArguments().getParcelable<Uri>(CONTENT_URI)
            when {
                link is String -> {
                    Glide.with(this)
                        .load(link)
                        .placeholder(progressDrawable)
                        .listener(this@ImageFragment)
                        .into(this)
                }
                contentUri is Uri -> {
                    lifecycleScope.launch {

                        val bitmap = withContext(Dispatchers.IO) {
                            contentUri.getBitmapOriginal(requireContext()).first
                        }

                        Glide.with(binding.photoView)
                            .load(bitmap)
                            .placeholder(progressDrawable)
                            .listener(this@ImageFragment)
                            .into(binding.photoView)
                    }
                }
                else -> {
                    throw IllegalStateException("At least URI or URL must be passed as argument")
                }
            }
        }
    }


    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        progressDrawable.stop()
        parentFragment?.startPostponedEnterTransition()
        viewModel.currentImageDrawable = null
        // let glide handle resource load
        return false
    }

    fun toggleToolbarVisibility() {
        toolbarOwner?.toggleToggleToolbar()
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        progressDrawable.stop()
        parentFragment?.startPostponedEnterTransition()
        viewModel.currentImageDrawable = if (resource != null) Event(resource) else null
        // let glide handle resource load
        return false
    }

    companion object {
        private const val LINK = "link"
        private const val TRANSITION_NAME = "transitionName"
        private const val CONTENT_URI = "uri"
        fun newInstance(link: String, transitionName: String) = ImageFragment().apply {
            arguments = bundleOf(LINK to link, TRANSITION_NAME to transitionName)
        }

        fun newInstance(localUri: Uri, transitionName: String) = ImageFragment().apply {
            arguments = bundleOf(CONTENT_URI to localUri, TRANSITION_NAME to transitionName)
        }
    }
}