package com.amebo.amebo.screens.newpost

import com.amebo.amebo.common.Resource
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.newpost.editor.PostEditor
import com.amebo.core.domain.PostListDataPage

interface IFormView {
    fun onFormSuccess(success: Resource.Success<FormData>)
    fun onFormError(error: Resource.Error<FormData>)
    fun onFormLoading(loading: Resource.Loading<FormData>)
    fun onSubmissionSuccess(success: Resource.Success<PostListDataPage>)
    fun onSubmissionError(error: Resource.Error<PostListDataPage>)
    fun onSubmissionLoading(loading: Resource.Loading<PostListDataPage>)
    fun onExistingImageRemovalSuccess(success: Resource.Success<ImageItem.Existing>)
    fun onExistingImageRemovalLoading(loading: Resource.Loading<ImageItem.Existing>)
    fun onExistingImageRemovalError(error: Resource.Error<ImageItem.Existing>)
    fun setFileCount(count: Int)
    fun showKeyboard(onBody: Boolean = false)
    fun redo()
    fun undo()
    fun canUndo(): Boolean
    fun canRedo(): Boolean
    fun refreshEditMenu()
    fun insertText(content: String)

    interface Listener: PostEditor.Listener {
        fun setPostBody(body: String)
        fun setPostTitle(title: String)
        fun retryLastRequest()
        fun goBack()
        fun showRules()
        fun submit()
        val canSubmit: Boolean
    }
}