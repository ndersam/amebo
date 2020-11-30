package com.amebo.amebo.screens.accounts.edit

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.invoke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentEditAccountScreenBinding
import com.amebo.core.domain.BirthDate
import com.amebo.core.domain.DisplayPhoto
import com.amebo.core.domain.Gender
import com.amebo.core.domain.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditAccountScreen : BaseFragment(R.layout.fragment_edit_account_screen),
    EditAccountView.Listener, AuthenticationRequired {

    private val binding by viewBinding(FragmentEditAccountScreenBinding::bind)
    private lateinit var editAccountView: EditAccountView
    private val viewModel by viewModels<EditAccountScreenViewModel>()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            viewModel.setImage(uri ?: return@registerForActivityResult)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_GENDER) { _, bundle ->
            val name = bundle.getString(FragKeys.BUNDLE_GENDER)
            val gender = if (name != null) Gender.valueOf(name) else null
            viewModel.editProfileFormData?.gender = gender
            editAccountView.setGender(gender)
        }
        setFragmentResultListener(FragKeys.RESULT_SELECTED_BIRTH_DATE) { _, bundle ->
            val date: BirthDate? = bundle.getParcelable(FragKeys.BUNDLE_SELECTED_BIRTH_DATE)
            viewModel.editProfileFormData?.birthDate = date
            editAccountView.setBirthDate(date)
        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initializeViews()
        viewModel.displayPhotoEvent.observe(
            viewLifecycleOwner,
            EventObserver(::handleDisplayPhotoResource)
        )
        viewModel.editProfileFormEvent.observe(
            viewLifecycleOwner,
            EventObserver(::handleEditProfileFormResource)
        )
        viewModel.editProfileSubmissionEvent.observe(
            viewLifecycleOwner,
            EventObserver(::handleEditProfileFormSubmissionResource)
        )
        viewModel.imageRemovedEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                editAccountView.onDisplayPhotoRemoved()
            }
        )
        viewModel.loadDisplayPhoto()
        viewModel.loadForm()
    }

    private fun initializeViews() {
        editAccountView = EditAccountView(binding, this)
    }

    private fun handleEditProfileFormResource(resource: Resource<EditProfileFormData>) {
        when (resource) {
            is Resource.Success -> editAccountView.onFormLoadSuccess(resource)
            is Resource.Error -> editAccountView.onFormLoadError(resource)
            is Resource.Loading -> editAccountView.onFormLoadLoading(resource)
        }
    }

    private fun handleDisplayPhotoResource(resource: Resource<DisplayPhoto>) {
        when (resource) {
            is Resource.Success -> editAccountView.onDisplayPhotoSuccess(resource)
            is Resource.Error -> editAccountView.onDisplayPhotoError(resource)
            is Resource.Loading -> editAccountView.onDisplayPhotoLoading(resource)
        }
    }

    private fun handleEditProfileFormSubmissionResource(resource: Resource<User.Data>) {
        when (resource) {
            is Resource.Success -> {
                editAccountView.onFormSubmissionSuccess(resource)
                router.back()
            }
            is Resource.Error -> editAccountView.onFormSubmissionError(resource)
            is Resource.Loading -> editAccountView.onFormSubmissionLoading(resource)
        }
    }


    override fun setTwitter(text: String) {
        viewModel.editProfileFormData?.twitter = text
    }

    override fun setPersonalText(text: String) {
        viewModel.editProfileFormData?.personalText = text
    }

    override fun setSignature(text: String) {
        viewModel.editProfileFormData?.signature = text
    }

    override fun setLocation(text: String) {
        viewModel.editProfileFormData?.location = text
    }

    override fun selectGender() {
        router.toGenderPicker(viewModel.editProfileFormData?.gender)
    }

    override fun visitPhotoViewer(view: View) {
        when(val uri = viewModel.selectedImageUri){
            is Uri -> {
                router.toPhotoViewerUri(uri, view, ViewCompat.getTransitionName(view)!!)
                return
            }
        }
        when(val url = viewModel.existingImageUrl){
            is String -> {
                router.toPhotoViewer(url, view, ViewCompat.getTransitionName(view)!!)
            }
        }
    }

    override fun onNavigationClicked() {
        router.back()
    }

    override fun saveChanges() = viewModel.updateProfile()


    override fun setYIM(text: String) {
        viewModel.editProfileFormData?.yim = text
    }

    override fun selectImage() = getContent("image/*")


    override fun removeImage() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.remove_display_photo_question)
            .setMessage(R.string.explain_display_photo_removal)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.removeImage()
            }.setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun revertToPreviousImage() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.restore_old_display_photo_question)
            .setMessage(R.string.explain_restore_old_display_photo)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.revertToOldImage()
            }.setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun selectBirthDate() {
        when (val form = viewModel.editProfileFormData) {
            is EditProfileFormData -> {
                router.toDatePicker(minYear = form.earliestYear, date = form.birthDate)
            }
        }
    }


}
