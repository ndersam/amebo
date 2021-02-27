package com.amebo.amebo.screens.newpost

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.hideKeyboard
import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.imagepicker.ImagePickerSharedViewModel
import com.amebo.amebo.screens.rules.RulesScreen
import com.amebo.core.domain.AreYouMuslimDeclarationForm
import com.amebo.core.domain.Form
import com.amebo.core.domain.PostListDataPage
import timber.log.Timber

abstract class FormScreen<T : Form>(@LayoutRes layoutRes: Int) : BaseFragment(layoutRes),
    IFormView.Listener, Pref.Observer, AuthenticationRequired {

    private val prefObservable = Pref.Observable()

    private val imagePickerViewModel by activityViewModels<ImagePickerSharedViewModel>()

    abstract val viewModel: FormViewModel<T>
    protected abstract val formView: IFormView

    abstract fun initializeViews()
    abstract fun initializeViewModelState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(RulesScreen.RESULT_RULES) { _, _ ->
            if (!pref.acceptedNairalandRules) {
                router.back()
            }
        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (!pref.acceptedNairalandRules) {
            router.toPostingRules()
        }
        initializeViews()
        initializeViewModels()
        router.setOnDialogDismissListener(viewLifecycleOwner) { formView.showKeyboard() }
        prefObservable.subscribe(requireContext(), this, R.string.key_edit_actions)
        initializeViewModelState()
        setHasOptionsMenu(true)
    }


    private fun initializeViewModels() {
        imagePickerViewModel.imagesUpdatedEvent.observe(viewLifecycleOwner, EventObserver {
            viewModel.updateImages(it.allNew, it.existingRemoved)
            requireActivity().invalidateOptionsMenu()
        })

        viewModel.imageCountEvent.observe(viewLifecycleOwner, {
            // We're peeking, to relay last image count
            Timber.d("Setting File count")
            formView.setFileCount(it.peekContent())
        })

        viewModel.existingImageRemovalEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onExistingImageItemRemovalEventContent)
        )

        viewModel.formLoadingEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onFormLoadingEventContent)
        )

        viewModel.formSubmissionEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onFormSubmissionEventContent)
        )

        viewModel.muslimDeclarationEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onMuslimDeclarationEvent)
        )
    }


    override fun onPreferenceChanged(key: Int, contextChanged: Boolean) {
        if (key == R.string.key_edit_actions) {
            formView.refreshEditMenu()
        }
    }


    private fun onFormLoadingEventContent(resource: Resource<FormData>) {
        when (resource) {
            is Resource.Loading -> formView.onFormLoading(resource)
            is Resource.Success -> formView.onFormSuccess(resource)
            is Resource.Error -> formView.onFormError(resource)
        }
    }

    private fun onFormSubmissionEventContent(resource: Resource<PostListDataPage>) {
        when (resource) {
            is Resource.Loading -> formView.onSubmissionLoading(resource)
            is Resource.Success -> {
                formView.onSubmissionSuccess(resource)
                onSubmissionSuccess(resource.content)
            }
            is Resource.Error -> formView.onSubmissionError(resource)
        }
    }

    private fun onExistingImageItemRemovalEventContent(resource: Resource<ImageItem.Existing>) {
        when (resource) {
            is Resource.Loading -> formView.onExistingImageRemovalLoading(resource)
            is Resource.Success -> formView.onExistingImageRemovalSuccess(resource)
            is Resource.Error -> formView.onExistingImageRemovalError(resource)
        }
    }

    private fun onMuslimDeclarationEvent(form: AreYouMuslimDeclarationForm) {
        hideKeyboard()
        router.toMuslimDeclaration(form)
    }

    @CallSuper
    open fun onSubmissionSuccess(postListDataPage: PostListDataPage) {
        setFragmentResult(
            FragKeys.RESULT_POST_LIST,
            bundleOf(FragKeys.BUNDLE_POST_LIST to postListDataPage)
        )
    }

    override fun onDestroyView() {
        prefObservable.unsubscribe(requireContext())
        hideKeyboard()
        super.onDestroyView()
    }

    override fun showRules() {
        hideKeyboard()
        router.toPostingRules()
    }

    override fun setPostBody(body: String) {
        viewModel.formData.body = body
    }


    override fun setPostTitle(title: String) {
        viewModel.formData.title = title
    }

    override fun retryLastRequest() = viewModel.retry()


    override fun preview(text: String) {
        hideKeyboard()
        val html = viewModel.preparePreview(requireContext(), text)
        router.toPostPreview(html)
    }

    override fun attachFile() {
        hideKeyboard()
        imagePickerViewModel.setImages(viewModel.existingImages + viewModel.selectedImages)
        router.toImagePicker()
    }

    override fun onDismissDialog() = formView.showKeyboard(onBody = true)


    override fun onDisplayDialog() {
        hideKeyboard()
    }

    override fun openSettings() {
        hideKeyboard()
        router.toPostEditorSettings()
    }

    override fun goBack() {
        router.back()
    }

    override fun submit() = viewModel.submitForm()

    override val canSubmit: Boolean get() = viewModel.canSubmit

    override val canShowKeyboard: Boolean get() = pref.acceptedNairalandRules
}
