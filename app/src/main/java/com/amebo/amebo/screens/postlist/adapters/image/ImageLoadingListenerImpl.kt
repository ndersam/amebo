package com.amebo.amebo.screens.postlist.adapters.image
import android.widget.ImageView
import androidx.fragment.app.Fragment
import java.util.concurrent.atomic.AtomicBoolean

class ImageLoadingListenerImpl(
    private val fragment: Fragment,
    /**
     * Callback that accepts (postPosition, imagePosition)
     */
    private val wasSelectedImagePosition: (Int, Int) -> Boolean
) : ImageLoadingListener {
    private val enterTransitionStarted = AtomicBoolean()

    override fun onLoadCompleted(view: ImageView, postPosition: Int, imagePosition: Int) {
        // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
        if (!wasSelectedImagePosition(postPosition, imagePosition)) {
            return
        }
        if (enterTransitionStarted.getAndSet(true)) {
            return
        }
        fragment.startPostponedEnterTransition()
    }
}