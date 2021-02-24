package com.amebo.amebo.screens.photoviewer

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.amebo.amebo.R
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.common.extensions.drawableColor

abstract class BasePhotoViewerView(
    toolbar: Toolbar,
    viewPager: ViewPager2,
    private val adapter: ImagePagerAdapter,
    private val listener: IPhotoViewerView.Listener
) : IPhotoViewerView {
    init {
        viewPager.adapter = adapter
        viewPager.setCurrentItem(listener.currentPosition, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                listener.currentPosition = position
            }
        })

        toolbar.menu.clear()
        if (adapter.usingImageLinks) {
            toolbar.inflateMenu(R.menu.photo_viewer)
        }
        toolbar.setOnMenuItemClickListener(::onMenuItemClick)
        toolbar.drawableColor(toolbar.context.asTheme().colorOnPrimary)
        toolbar.setNavigationOnClickListener { listener.goBack() }
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy_link -> listener.copyToClipBoard()
            R.id.save_image -> listener.saveImage()
            R.id.share_image -> listener.shareImage()
            else -> return false
        }
        return true
    }

    override val currentImageView: View?
        get() {
            val currentFragment =
                adapter.createFragment(listener.currentPosition)
            return currentFragment.view
        }
}