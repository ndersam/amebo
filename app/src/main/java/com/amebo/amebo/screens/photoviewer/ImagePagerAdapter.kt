package com.amebo.amebo.screens.photoviewer

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


class ImagePagerAdapter(
    fragment: Fragment,
    private val imageLinks: List<String>?,
    private val imageUris: List<Uri>?,
    private val transitionName: String
) : FragmentStateAdapter(
    fragment
) {
    val usingImageLinks get() = imageLinks != null

    override fun getItemCount(): Int {
        return imageLinks?.size ?: imageUris!!.size
    }

    override fun createFragment(position: Int): Fragment {
        if (imageLinks != null)
            return ImageFragment.newInstance(imageLinks[position], transitionName)
        if (imageUris != null)
            return ImageFragment.newInstance(imageUris[position], transitionName)
        throw IllegalStateException("You shouldn't be here")
    }
}