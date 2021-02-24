package com.amebo.amebo.screens.photoviewer

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.extensions.getBitmapOriginal
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentImageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
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
        binding.photoView.transitionName = requireArguments().getString(TRANSITION_NAME, null)!!
        binding.root.setOnClickListener { toggleToolbarVisibility() }
        binding.photoView.setOnClickListener { toggleToolbarVisibility() }
        binding.btnRetry.setOnClickListener { load() }
        progressDrawable = AppUtil.progressDrawable(requireContext(), color = Color.WHITE)
        load()
    }


    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        binding.photoView.isVisible = false
        binding.btnRetry.isVisible = true
        progressDrawable.stop()
        parentFragment?.startPostponedEnterTransition()
        viewModel.currentImageDrawable = null
        // let glide handle resource load
        return false
    }

    private fun toggleToolbarVisibility() {
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
        // https://stackoverflow.com/questions/55980750/gif-is-not-playing-after-shared-element-transition-glide-v-4-8-0
        if (resource is GifDrawable) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                resource.setVisible(true, true)
            }
        }
        parentFragment?.startPostponedEnterTransition()
        viewModel.currentImageDrawable = if (resource != null) Event(resource) else null
        // let glide handle resource load
        return false
    }


    private fun load() {
        binding.photoView.isVisible = true
        binding.btnRetry.isVisible = false
        progressDrawable.start()
        val link = requireArguments().getString(LINK, null)
        val contentUri = requireArguments().getParcelable<Uri>(CONTENT_URI)
        when {
            link is String -> {
                Glide.with(this)
                    .load(link)
                    .placeholder(progressDrawable)
                    .listener(this@ImageFragment)
                    .into(binding.photoView)
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