package com.amebo.amebo.screens.photoviewer

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.drawableColor
import com.amebo.amebo.common.asTheme

abstract class BasePhotoViewerView(
    toolbar: Toolbar,
    private val viewPager: PhotoViewViewPager,
    private val adapter: ImagePagerAdapter,
    private val listener: IPhotoViewerView.Listener
) : IPhotoViewerView {
    init {
        viewPager.adapter = adapter
        viewPager.currentItem = listener.currentPosition
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
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
                adapter.instantiateItem(viewPager, viewPager.currentItem) as Fragment
            return currentFragment.view
        }
}