package com.amebo.amebo.screens.photoviewer.user

import androidx.annotation.PluralsRes
import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.databinding.FragmentUserPhotoViewerScreenBinding
import com.amebo.amebo.screens.photoviewer.BasePhotoViewerView
import com.amebo.amebo.screens.photoviewer.IPhotoViewerView
import com.amebo.amebo.screens.photoviewer.ImagePagerAdapter
import com.amebo.amebo.screens.photoviewer.LikeResult
import com.amebo.core.common.CoreUtils
import com.amebo.core.domain.User
import java.lang.ref.WeakReference

class UserPostPhotoViewerView(
    binding: FragmentUserPhotoViewerScreenBinding,
    adapter: ImagePagerAdapter,
    pref: Pref,
    private val listener: Listener,
    user: User
) : BasePhotoViewerView(
    binding.toolbar,
    binding.viewPager,
    adapter, listener
) {

    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!

    init {
        // can't like your own photo apparently
        binding.chbxLike.isVisible = user != pref.user
        user.data?.image?.let { image ->
            setData(image)
        }
    }

    private fun setData(image: User.Image) {
        val res = binding.root.context.resources
        fun getQty(count: Int, @PluralsRes pluralRes: Int) = res.getQuantityString(
            pluralRes,
            count,
            count
        )

        fun setLikesViews() {
            binding.bullet.isVisible = image.likes > 0 && image.oldLikes > 0
            if (image.likes > 0) {
                binding.likeCount.text = getQty(image.likes, R.plurals.likes)
            }
            if (image.oldLikes > 0) {
                binding.oldLikeCount.text = getQty(image.oldLikes, R.plurals.old_likes)
            }
        }

        setLikesViews()
        binding.timeUploaded.text =
            res.getString(R.string.time_uploaded, CoreUtils.howLongAgo(image.timestamp))


        binding.chbxLike.setOnCheckedChangeListener(null)
        binding.chbxLike.jumpDrawablesToCurrentState()
        binding.chbxLike.isChecked = image.isLiked
        binding.chbxLike.jumpDrawablesToCurrentState()

        binding.chbxLike.setOnCheckedChangeListener { _, isChecked ->
            image.isLiked = isChecked
            image.likes = if (isChecked) (image.likes + 1) else (image.likes - 1)
            setLikesViews()
            listener.likePhoto(isChecked)
        }
    }

    fun onDataSuccess(resource: Resource.Success<LikeResult>) {
        val image = resource.content.data.image ?: return
        setData(image)
    }

    fun onDataLoading(resource: Resource.Loading<LikeResult>) {

    }

    fun onDataError(resource: Resource.Error<LikeResult>) {

    }

    interface Listener : IPhotoViewerView.Listener {
        fun likePhoto(like: Boolean)
    }
}