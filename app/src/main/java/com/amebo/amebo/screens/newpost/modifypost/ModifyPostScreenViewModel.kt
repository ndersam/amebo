package com.amebo.amebo.screens.newpost.modifypost

import android.app.Application
import com.amebo.amebo.common.Event
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.newpost.FormViewModel
import com.amebo.amebo.screens.newpost.ModifyPostFormData
import com.amebo.core.Nairaland
import com.amebo.core.common.Either
import com.amebo.core.domain.*
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import javax.inject.Inject

class ModifyPostScreenViewModel @Inject constructor(
    nairaland: Nairaland,
    application: Application
) :
    FormViewModel<ModifyForm>(nairaland, application) {

    override val loadFormFirst: Boolean = true
    override val formData = ModifyPostFormData()
    private lateinit var post: SimplePost


    fun initialize(
        post: SimplePost
    ) {
        this.post = post
        initialize()
    }

    override suspend fun doFetchFormData(): Result<Either<ModifyForm, AreYouMuslimDeclarationForm>, ErrorResponse> {
        val result = nairaland.sources.forms.modifyPost(post)
        if (result is Ok) {
            when (val formWrapper = result.value) {
                is Either.Left -> {
                    formData.titleIsEditable = formWrapper.data.titleEditable
                    existingImages.clear()
                    formWrapper.data.attachments.forEachIndexed { index, attachment ->
                        existingImages.add(ImageItem.Existing(attachment, post.images[index]))
                    }
                    imageCountEventInternal.value = Event(existingImages.size)
                }
            }
        }
        return result
    }

    override suspend fun doSubmitFormData(form: ModifyForm): Result<PostListDataPage, ErrorResponse> {
        return nairaland.sources.submissions.modifyPost(form)
    }
}
