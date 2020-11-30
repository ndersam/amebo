package com.amebo.amebo.screens.imagepicker

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.invoke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.SpacesItemDecoration
import com.amebo.amebo.common.extensions.convertDpToPx
import com.amebo.amebo.common.extensions.hasMediaAccess
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.ImagePickerScreenBinding
import com.amebo.core.Values
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean


class ImagePickerScreen : BaseFragment(R.layout.image_picker_screen), GalleryAdapter.Listener,
    AuthenticationRequired {

    private val binding by viewBinding(ImagePickerScreenBinding::bind)
    private val viewModel by activityViewModels<ImagePickerSharedViewModel>()
    private val canAddMoreImages get() = adapter.itemCount < Values.MAX_IMAGE_UPLOAD
    private lateinit var adapter: GalleryAdapter

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            viewModel.addImages(requireActivity(), uris)
        }

    private var pendingPickImage = false
    private val getPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result && pendingPickImage) {
                pendingPickImage = false
                pickImage()
            } else if (!result) {
                showRationale()
            }
        }


    private fun checkPermission(setPendingRequest: Boolean = false): Boolean {
        if (requireContext().hasMediaAccess.not()) {
            pendingPickImage = setPendingRequest
            getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return false
        }
        return true
    }

    private fun showRationale() {
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Snackbar.make(requireView(), R.string.error_storage_permission, Snackbar.LENGTH_LONG)
                .show()
        }
    }


    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener(::onOptionsItemSelected)
        binding.toolbar.setNavigationOnClickListener { router.back() }

        adapter = GalleryAdapter(this, this)

        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            SpacesItemDecoration(
                requireContext().convertDpToPx(
                    8F
                ).toInt()
            )
        )
        binding.emptyView.setOnClickListener { pickImage() }

        viewModel.observe(
            viewLifecycleOwner,
            EventObserver(::setState)
        )
        checkPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackBar = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_image) {
            pickImage()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setState(state: ViewState) {
        when (state) {
            is ViewState.ImagesAvailable -> onImagesAvailable(state)
            is ViewState.Error -> onErrorAddingImageState(state)
        }
    }

    private fun onImagesAvailable(state: ViewState.ImagesAvailable) {
        state.images.forEach { adapter.addImage(it) }
        updateState()
    }

    private fun onErrorAddingImageState(state: ViewState.Error) {
        when (state.reason) {
            ViewState.Error.Reason.UNKNOWN -> {
                Snackbar.make(
                    binding.root,
                    R.string.error_adding_images, Snackbar.LENGTH_SHORT
                ).show()
            }
            ViewState.Error.Reason.TOO_LARGE -> {
                Snackbar.make(
                    binding.root,
                    R.string.error_big_file,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun addImage(item: ImageItem, position: Int) {
        adapter.addImage(item, position)
        updateState()
    }

    private fun doRemoveImage(position: Int) {
        adapter.removeImage(position)
        updateState()
    }


    private fun updateState() {
        if (adapter.itemCount == 0) {
            binding.stateLayout.empty()
        } else {
            binding.stateLayout.content()
        }
        requireActivity().invalidateOptionsMenu()
    }

    private var snackBar: Snackbar? = null
    override fun removeImage(
        item: ImageItem,
        position: Int
    ) {
        doRemoveImage(position)
        val remove = AtomicBoolean(true)
        snackBar = Snackbar.make(
            requireView(),
            R.string.image_deleted,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.undo) {
            remove.set(false)
        }.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                try {
                    if (remove.get()) {
                        viewModel.removeImage(item)
                    } else {
                        addImage(item, position)
                    }
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
            }
        })
        snackBar!!.show()
    }

    private fun pickImage() {
        snackBar?.dismiss()
        if (checkPermission(true)) {
            if (canAddMoreImages) {
                getContent("image/*")
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.you_can_only_attach_4_files,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }


}
