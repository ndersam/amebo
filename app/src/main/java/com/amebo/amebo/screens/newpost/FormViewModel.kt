package com.amebo.amebo.screens.newpost

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.R
import com.amebo.amebo.common.EmojiGetter
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.getBitmapOriginal
import com.amebo.amebo.common.extensions.toResource
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.core.Nairaland
import com.amebo.core.common.Either
import com.amebo.core.common.Values
import com.amebo.core.common.extensions.openRawAsString
import com.amebo.core.converter.DocConverter
import com.amebo.core.domain.*
import com.github.michaelbull.result.*
import kotlinx.coroutines.launch

abstract class FormViewModel<T : Form>(val nairaland: Nairaland, application: Application) :
    AndroidViewModel(
        application
    ) {

    private var form: T? = null
    private var muslimDeclarationForm: AreYouMuslimDeclarationForm? = null
    private var isLastRequestFormSubmission: Boolean = false

    protected abstract val loadFormFirst: Boolean
    abstract val formData: FormData

    /**
     * Image count update
     */
    private val imageCount get() = selectedImages.size + existingImages.size
    protected val imageCountEventInternal = MutableLiveData<Event<Int>>()
    val imageCountEvent: LiveData<Event<Int>> = imageCountEventInternal

    /**
     * Removal of [ImageItem.Existing]
     */
    private val _existingImageRemovalEvent = MutableLiveData<Event<Resource<ImageItem.Existing>>>()
    val existingImageRemovalEvent: LiveData<Event<Resource<ImageItem.Existing>>> =
        _existingImageRemovalEvent

    /**
     * Handling Muslim declaration for `Islams for Muslims` board
     */
    private val _muslimDeclarationEvent = MutableLiveData<Event<AreYouMuslimDeclarationForm>>()
    val muslimDeclarationEvent: LiveData<Event<AreYouMuslimDeclarationForm>> =
        _muslimDeclarationEvent

    /**
     * Form loading
     */
    private val _formLoadingEvent = MutableLiveData<Event<Resource<FormData>>>()
    val formLoadingEvent: LiveData<Event<Resource<FormData>>> = _formLoadingEvent

    /**
     * Form submission
     */
    private val _formSubmissionEvent = MutableLiveData<Event<Resource<PostListDataPage>>>()
    val formSubmissionEvent: LiveData<Event<Resource<PostListDataPage>>> = _formSubmissionEvent


    // Images
    var selectedImages: List<ImageItem.New> = emptyList()
        private set
    var existingImages: MutableList<ImageItem.Existing> = mutableListOf()
        protected set
    private val imagePaths: List<String>
        get() = existingImages.map { it.url } + selectedImages.map { it.path }


    open val canSubmit: Boolean
        get() {
            // if submitting --> false
            _formSubmissionEvent.value?.peekContent().let {
                if (it is Resource.Loading) {
                    return false
                }
            }
            if (loadFormFirst && form == null) {
                return false
            }
            if (form?.titleEditable == true && formData.title.isBlank()) {
                return false
            }
            if (formData.body.isNotBlank() || imageCount > 0) {
                return true
            }
            return false
        }

    private fun fetchForm() {
        // setLoading
        _formLoadingEvent.value = Event(
            Resource.Loading(
                if (form == null) null else formData
            )
        )

        viewModelScope.launch {
            doFetchFormData()
                .onFailure {

                    isLastRequestFormSubmission = false
                    _formLoadingEvent.value =
                        Event(Resource.Error(it, if (form == null) null else formData))
                }
                .onSuccess { result ->
                    val resource = when (result) {
                        is Either.Left -> {
                            val form = result.data
                            this@FormViewModel.form = form
                            formData.body = form.body
                            formData.title = form.title
                            Resource.Success(formData)
                        }
                        // if user requires declaration for islam
                        is Either.Right -> {
                            _muslimDeclarationEvent.value = Event(result.data)
                            return@launch
                        }
                    }
                    isLastRequestFormSubmission = false
                    _formLoadingEvent.value = Event(resource)
                }

        }
    }

    fun submitForm() {
        _formSubmissionEvent.value = Event(Resource.Loading(null))

        viewModelScope.launch {
            if (!fetchFormDataDuringSubmission()) return@launch
            val result = doSubmitFormData(prepareFormForSubmission())
            isLastRequestFormSubmission = true
            _formSubmissionEvent.value = Event(result.toResource(null))
        }
    }

    fun retry() {
        if (isLastRequestFormSubmission) {
            submitForm()
        } else {
            fetchForm()
        }
    }


    fun updateImages(
        selectedImages: List<ImageItem.New>,
        removedImages: List<ImageItem.Existing>
    ) {
        this.selectedImages = selectedImages
        this.existingImages.removeAll(removedImages)
        imageCountEventInternal.value = Event(imageCount)
        removeExistingImage(removedImages)
    }

    private fun removeExistingImage(removedImages: List<ImageItem.Existing>) =
        viewModelScope.launch {
            removedImages.forEach { item ->

                // setLoading
                _existingImageRemovalEvent.value = Event(Resource.Loading(item))

                when (val result =
                    nairaland.sources.submissions.removeAttachment(item.attachment)) {
                    is Ok -> {
                        _existingImageRemovalEvent.value = Event(Resource.Success(content = item))
                    }
                    is Err -> {
                        _existingImageRemovalEvent.value = Event(
                            Resource.Error(cause = result.error, content = item)
                        )

                        /**
                         * FIXME
                         * If re-adding the deleted image would make [imageCount] larger than
                         * [Values.MAX_IMAGE_UPLOAD], remove the last [ImageItem.New]
                         */
                        with(this@FormViewModel) {
                            if (imageCount + 1 > Values.MAX_IMAGE_UPLOAD) {
                                this.selectedImages = ArrayList(this.selectedImages).apply {
                                    removeAt(size - 1)
                                }
                            }
                            this.existingImages.add(item)
                            imageCountEventInternal.value = Event(imageCount)
                        }
                    }
                }
            }
        }


    protected fun initialize() {
        // re-post successfully loaded form data, if available
        _formLoadingEvent.value?.let { event ->
            val resource = event.peekContent()
            if (resource is Resource.Success) {
                _formLoadingEvent.value = Event(Resource.Success(resource.content))
                return
            }
        }

        if (loadFormFirst) {
            fetchForm()
        } else {
            _formLoadingEvent.value = Event(Resource.Success(formData))
        }
    }


    /**
     * Returns true if form has been successfully loaded, otherwise loads form.
     * Returns false, if a form loading attempt fails.
     */
    private suspend fun fetchFormDataDuringSubmission(): Boolean {
        if (form != null) return true
        return when (val result = doFetchFormData()) {
            is Ok -> {
                when (val formWrapper = result.value) {
                    is Either.Left -> {
                        this.form = formWrapper.data
                        true
                    }
                    is Either.Right -> {
                        _muslimDeclarationEvent.value = Event(formWrapper.data)
                        false
                    }
                }
            }
            is Err -> {
                isLastRequestFormSubmission = true
                _formSubmissionEvent.value = Event(Resource.Error(result.error, null))
                false
            }
        }
    }

    protected open fun prepareFormForSubmission(): T {
        val form = this.form ?: throw IllegalStateException("This should not happen")
        form.body = formData.body
        form.title = formData.title
        form.bundle =
            ImageBundle(selectedImages.map { it.name to it.uri.getBitmapOriginal(getApplication()).first })
        return form
    }

    fun preparePreview(context: Context, body: String): String {
        val imageData = imagePaths.joinToString("") {
            if (it.startsWith("http://") || it.startsWith("https://")) {
                "<p><img src=\"${it}\"/></p>"
            } else {
                "<p><img src=\"file://${it}\"/></p>"
            }
        }

        val css = context.openRawAsString(R.raw.nairaland)
        var content = DocConverter.bbCodeToHtml(body)
        content = EmojiGetter.prepForWebView(content)
        content = content.replace("[hr]", "<hr>")

        return """
            <!DOCTYPE html>
            <style>
            $css
            div.narrow {
              min-height: 200px;
            }
            </style>
            <html> 
                <body> 
                <table summary="posts">
                <tbody>
                <tr>
                    <td class="l w pd">
                       <div class="narrow"> $content </div>
                        $imageData
                    </td>
                </tr>
                </tbody>
                </table>
                </body>
            </html>
        """.trimIndent()
    }


    abstract suspend fun doFetchFormData(): Result<Either<T, AreYouMuslimDeclarationForm>, ErrorResponse>

    abstract suspend fun doSubmitFormData(form: T): Result<PostListDataPage, ErrorResponse>

}
