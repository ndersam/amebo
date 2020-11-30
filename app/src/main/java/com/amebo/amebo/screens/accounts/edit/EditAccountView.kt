package com.amebo.amebo.screens.accounts.edit

import android.view.MenuItem
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.amebo.amebo.R
import com.amebo.amebo.common.GlideApp
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.context
import com.amebo.amebo.common.extensions.snack
import com.amebo.amebo.common.extensions.toast
import com.amebo.amebo.databinding.FragmentEditAccountScreenBinding
import com.amebo.core.domain.*
import java.lang.ref.WeakReference

class EditAccountView(
    binding: FragmentEditAccountScreenBinding,
    private val listener: Listener
) {

    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!


    init {
        binding.twitter.doOnTextChanged { text, _, _, _ ->
            listener.setTwitter(text.toString())
        }
        binding.personalText.doOnTextChanged { text, _, _, _ ->
            listener.setPersonalText(text.toString())
        }
        binding.signature.doOnTextChanged { text, _, _, _ ->
            listener.setSignature(text.toString())
        }
        binding.location.doOnTextChanged { text, _, _, _ ->
            listener.setLocation(text.toString())
        }
        binding.gender.setOnClickListener {
            listener.selectGender()
        }
        binding.toolbar.setNavigationOnClickListener {
            listener.onNavigationClicked()
        }
        binding.toolbar.setOnMenuItemClickListener(::onMenuItemClickListener)
        binding.birthDate.setOnClickListener {
            listener.selectBirthDate()
        }
        binding.btnRemoveImage.setOnClickListener {
            listener.removeImage()
        }
        binding.btnSelectImage.setOnClickListener {
            listener.selectImage()
        }
        binding.yim.doOnTextChanged { text, _, _, _ ->
            listener.setYIM(text.toString())
        }
        binding.btnRevertChange.setOnClickListener {
            listener.revertToPreviousImage()
        }
        binding.displayPhoto.setOnClickListener {
            listener.visitPhotoViewer(binding.displayPhoto)
        }
    }

    fun onFormLoadSuccess(success: Resource.Success<EditProfileFormData>) {
        setForm(success.content)
        binding.stateLayout.content()
        setSubmitVisible(true)
    }

    fun onFormLoadError(error: Resource.Error<EditProfileFormData>) {
        setSubmitVisible(false)

    }

    fun onFormLoadLoading(loading: Resource.Loading<EditProfileFormData>) {
        if (loading.content != null) {
            setForm(loading.content)
            binding.stateLayout.content()
        } else {
            setSubmitVisible(false)
            binding.stateLayout.loading()
        }
    }

    fun onDisplayPhotoSuccess(success: Resource.Success<DisplayPhoto>) {
        setPhoto(success.content)
    }

    fun onDisplayPhotoError(error: Resource.Error<DisplayPhoto>) {

    }

    fun onDisplayPhotoLoading(loading: Resource.Loading<DisplayPhoto>) {
        setPhoto(loading.content ?: return)
    }


    fun onFormSubmissionSuccess(success: Resource.Success<User.Data>) {
        binding.toolbar.menu.findItem(R.id.save).isVisible = true
        binding.toolbarProgress.isVisible = false
        binding.context.toast(R.string.profile_updated)
    }

    fun onFormSubmissionError(error: Resource.Error<User.Data>) {
        binding.toolbar.menu.findItem(R.id.save).isVisible = true
        binding.toolbarProgress.isVisible = false
        binding.snack(error.cause)
    }

    fun onFormSubmissionLoading(loading: Resource.Loading<User.Data>) {
        binding.toolbar.menu.findItem(R.id.save).isVisible = false
        binding.toolbarProgress.isVisible = true

    }

    fun onDisplayPhotoRemoved() {
        binding.btnRevertChange.isVisible = true
        binding.btnRemoveImage.isInvisible = true
        onNoDisplayPhoto()
    }

    private fun setForm(form: EditProfileFormData) {
        binding.personalText.setText(form.personalText)
        binding.signature.setText(form.signature)
        binding.location.setText(form.location)
        binding.twitter.setText(form.twitter)
        setGender(form.gender)
        setBirthDate(form.birthDate)
    }

    fun setGender(gender: Gender?) {
        binding.txtGender.setText(
            when (gender) {
                null, Gender.Unknown -> R.string.double_dash
                Gender.Male -> R.string.male
                Gender.Female -> R.string.female
            }
        )
    }

    fun setBirthDate(birthDate: BirthDate?) {
        when (birthDate) {
            is BirthDate -> {
                binding.txtYear.text = birthDate.year.value.toString()
                binding.txtDay.text = birthDate.day.value.toString()
                binding.txtMonth.setText(
                    when (birthDate.month.value) {
                        1 -> R.string.january
                        2 -> R.string.february
                        3 -> R.string.march
                        4 -> R.string.april
                        5 -> R.string.may
                        6 -> R.string.june
                        7 -> R.string.july
                        8 -> R.string.august
                        9 -> R.string.september
                        10 -> R.string.october
                        11 -> R.string.november
                        12 -> R.string.december
                        else -> throw IllegalArgumentException("Unknown month: ${birthDate.month.value}")
                    }
                )
            }
            else -> {
                binding.txtYear.setText(R.string.double_dash)
                binding.txtDay.setText(R.string.double_dash)
                binding.txtMonth.setText(R.string.double_dash)
            }
        }
    }

    private fun setPhoto(photo: DisplayPhoto) {
        when (photo) {
            // selected image
            is DisplayPhotoBitmap -> {
                GlideApp.with(binding.displayPhoto)
                    .asBitmap()
                    .load(photo.bitmap)
                    .into(binding.displayPhoto)
                binding.btnRemoveImage.isInvisible = true
                binding.btnRevertChange.isVisible = true
                binding.txtNoDisplayPhoto.isVisible = false
                binding.displayPhoto.isEnabled = true
            }
            // existing image
            is DisplayPhotoUrl -> {
                GlideApp.with(binding.displayPhoto)
                    .load(photo.url)
                    .into(binding.displayPhoto)

                binding.btnRemoveImage.isVisible = true
                binding.btnRevertChange.isVisible = false
                binding.txtNoDisplayPhoto.isVisible = false
                binding.displayPhoto.isEnabled = true
            }
            is NoDisplayPhoto -> {
                onNoDisplayPhoto()
                binding.btnRemoveImage.isVisible = false
                binding.btnRevertChange.isVisible = false
            }
        }
    }

    private fun onMenuItemClickListener(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> listener.saveChanges()
            else -> return false
        }
        return true
    }

    private fun setSubmitVisible(isVisible: Boolean) {
        binding.toolbar.menu.findItem(R.id.save).isVisible = isVisible
    }

    private fun onNoDisplayPhoto() {
        binding.displayPhoto.setImageResource(R.color.black)
        binding.txtNoDisplayPhoto.isVisible = true
        binding.displayPhoto.isEnabled = false
    }

    interface Listener {
        fun setTwitter(text: String)
        fun setPersonalText(text: String)
        fun setSignature(text: String)
        fun setLocation(text: String)
        fun selectGender()
        fun setYIM(text: String)
        fun removeImage()
        fun selectImage()
        fun selectBirthDate()
        fun visitPhotoViewer(view: View)
        fun onNavigationClicked()
        fun saveChanges()
        fun revertToPreviousImage()
    }

}
