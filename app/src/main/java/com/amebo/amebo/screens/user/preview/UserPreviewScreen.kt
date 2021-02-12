package com.amebo.amebo.screens.user.preview

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.common.AvatarGenerator
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.genderDrawable
import com.amebo.amebo.common.extensions.setDrawableEnd
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.InjectableBaseDialogFragment
import com.amebo.amebo.databinding.UserPreviewScreenBinding
import com.amebo.amebo.screens.user.UserScreenViewModel
import com.amebo.core.CoreUtils
import com.amebo.core.domain.User
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

class UserPreviewScreen : InjectableBaseDialogFragment(R.layout.user_preview_screen),
    RequestListener<Drawable> {
    private val binding by viewBinding(UserPreviewScreenBinding::bind)
    private val viewModel by viewModels<UserScreenViewModel>()

    private val user get() = requireArguments().getParcelable<User>(USER)!!

    private lateinit var progressDrawable: CircularProgressDrawable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressDrawable = AppUtil.progressDrawable(requireContext())
        binding.txtName.text = user.name
        loadAvatar()
        updateGender()
        binding.btnRetry.setOnClickListener { viewModel.load() }
        binding.content.setOnClickListener { router.toUser(user, preview = false) }

        viewModel.dataEvent.observe(viewLifecycleOwner, EventObserver(::onUserDataEvent))
        viewModel.initialize(user)
        viewModel.loadConditionally()
    }

    private fun onUserDataEvent(resource: Resource<User.Data>) {
        when (resource) {
            is Resource.Success -> {
                binding.stateLayout.content()
                user.data = resource.content
                setContent(resource.content)
            }
            is Resource.Error -> {
                binding.stateLayout.failure()
                val data = resource.content ?: user.data
                if (data != null) {
                    setContent(data)
                }
            }
            is Resource.Loading -> {
                binding.stateLayout.content()
                val data = resource.content ?: user.data
                if (data != null) {
                    setContent(data)
                }
            }
        }
    }

    private fun setContent(data: User.Data) {
        if (data.timeRegistered > 0L) {
            binding.txtTimeRegistered.text = CoreUtils.timeRegistered(data.timeRegistered)
        }
        if (data.lastSeen != null && data.lastSeen!! > 0L) {
            binding.txtLastSeen.text =
                getString(R.string.x_ago, CoreUtils.howLongAgo(data.lastSeen!!))
        }
        updateGender()
        when (val url = data.image?.url) {
            is String -> {
                binding.displayPhoto.isVisible = true
                Glide.with(binding.displayPhoto)
                    .load(url)
                    .placeholder(progressDrawable)
                    .listener(this)
                    .into(binding.displayPhoto)
            }
        }
    }

    private fun loadAvatar() {
        lifecycleScope.launch {
            try {
                val bitmap = AvatarGenerator.getForUser(requireContext(), user.name)
                binding.displayPhoto.isVisible = true
                Glide.with(binding.displayPhoto)
                    .load(bitmap)
                    .into(binding.displayPhoto)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance()
                    .log("Error AvatarGenerator for '${user.name}': $e")
            }
        }
    }

    private fun updateGender() {
        binding.txtName.setDrawableEnd(
            user.genderDrawable,
            useLineHeight = true
        )
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        progressDrawable.stop()
        binding.displayPhoto.post {
            Glide.with(binding.displayPhoto).clear(binding.displayPhoto)
        }
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        progressDrawable.stop()
        return false
    }

    companion object {
        private const val USER = "USER"
        fun newBundle(user: User) = bundleOf(USER to user)
    }
}