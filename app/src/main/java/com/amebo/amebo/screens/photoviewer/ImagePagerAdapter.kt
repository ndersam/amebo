package com.amebo.amebo.screens.photoviewer

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter

class ImagePagerAdapter(
    fragment: Fragment,
    private val imageLinks: List<String>?,
    private val imageUris: List<Uri>?,
    private val transitionName: String
) : FragmentStatePagerAdapter(
    fragment.childFragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    val usingImageLinks get() = imageLinks != null

    override fun getCount(): Int {
        return imageLinks?.size ?: imageUris?.size ?: 0
    }

    override fun getItem(position: Int): Fragment {
        if (imageLinks != null)
            return ImageFragment.newInstance(imageLinks[position], transitionName)
        if (imageUris != null)
            return ImageFragment.newInstance(imageUris[position], transitionName)
        throw IllegalStateException("You shouldn't be here")
    }
}