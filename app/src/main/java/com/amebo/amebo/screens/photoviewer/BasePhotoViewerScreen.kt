package com.amebo.amebo.screens.photoviewer

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import androidx.activity.invoke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.hasMediaAccess
import com.amebo.amebo.common.fragments.BaseFragment
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider


abstract class BasePhotoViewerScreen(@LayoutRes layoutRes: Int) : BaseFragment(layoutRes),
    ToolbarOwner, IPhotoViewerView.Listener {

    @Inject
    lateinit var viewProvider: Provider<IPhotoViewerView>

    lateinit var photoView: IPhotoViewerView


    override var currentPosition: Int
        get() = requireArguments().getInt(CURRENT_POSITION, 0)
        set(value) {
            requireArguments().putInt(CURRENT_POSITION, value)
            setResult()
        }


    val viewModel by activityViewModels<PhotoSharedViewModel>()

    val imageLinks get() = requireArguments().getStringArray(LINKS)?.toList()

    @Suppress("UNCHECKED_CAST")
    val imageUris: List<Uri>?
        get() = when (val array = requireArguments().getParcelableArray(URIS)) {
            null -> null
            else -> array.toList() as List<Uri>
        }


    val transitionName get() = requireArguments().getString(TRANSITION_NAME)!!

    private val getPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                doSaveImage()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareSharedElementTransition(savedInstanceState)
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        setResult() // set result regardless of if position in viewPager is changed
        photoView = viewProvider.get()
    }

    override fun goBack() {
        router.back()
    }

    override fun saveImage() {
        if (requireContext().hasMediaAccess) {
            doSaveImage()
        } else {
            getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun doSaveImage() {
        val imageLinks = imageLinks ?: return
        viewModel.saveImage(requireView(), imageLinks[currentPosition])
    }

    override fun copyToClipBoard() {
        val imageLinks = imageLinks ?: return
        viewModel.copyToClipBoard(requireView(), imageLinks[currentPosition])
    }

    override fun shareImage() {
        viewModel.shareImage(requireView())
    }

    private fun setResult() {
        setFragmentResult(
            FragKeys.RESULT_LAST_IMAGE_POSITION,
            bundleOf(FragKeys.BUNDLE_LAST_IMAGE_POSITION to currentPosition)
        )
    }

    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private fun prepareSharedElementTransition(savedInstanceState: Bundle?) {
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.image_shared_element_transition).apply {
                Timber.d("D: ${this.targets}, ${this.transitionProperties}")
                addListener(object : Transition.TransitionListener {
                    private var start = 0L
                    override fun onTransitionStart(transition: Transition?) {
                        start = System.currentTimeMillis()
                    }

                    override fun onTransitionEnd(transition: Transition?) {
                        Timber.d("Transition Ended: ${System.currentTimeMillis() - start}")
                    }

                    override fun onTransitionCancel(transition: Transition?) {
                        Timber.d("Transition Cancelled")
                    }

                    override fun onTransitionPause(transition: Transition?) {
                    }

                    override fun onTransitionResume(transition: Transition?) {
                    }

                })
            }
        // A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
//        setEnterSharedElementCallback(
//            object : SharedElementCallback() {
//                override fun onMapSharedElements(
//                    names: List<String>,
//                    sharedElements: MutableMap<String, View>
//                ) {
//                    // Locate the image view at the primary fragment (the ImageFragment that is currently
//                    // visible). To locate the fragment, call instantiateItem with the selection position.
//                    // At this stage, the method will simply return the fragment at the position and will
//                    // not create a new one.
//                    val view = photoView.currentImageView ?: return
//
//                    // Map the first shared element name to the child ImageView.
//                    sharedElements[names[0]] = view.findViewById(R.id.photoView)
//                }
//            })

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }
        sharedElementReturnTransition = null
    }


    companion object {
        protected const val CURRENT_POSITION = "position"
        protected const val LINKS = "links"
        protected const val TRANSITION_NAME = "transitionName"
        protected const val URIS = "uris"

        fun newBundle(links: List<String>, transitionName: String, position: Int) = bundleOf(
            LINKS to links.toTypedArray(),
            TRANSITION_NAME to transitionName,
            CURRENT_POSITION to position
        )

        fun newBundleForUris(uris: List<Uri>, transitionName: String, position: Int) = bundleOf(
            URIS to uris.toTypedArray(),
            TRANSITION_NAME to transitionName,
            CURRENT_POSITION to position
        )
    }
}
