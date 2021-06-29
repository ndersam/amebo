package com.amebo.amebo.screens.newpost.newpost

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.toResource
import com.amebo.amebo.screens.newpost.FormViewModel
import com.amebo.amebo.screens.newpost.NewPostFormData
import com.amebo.core.Nairaland
import com.amebo.core.common.Either
import com.amebo.core.domain.*
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewPostScreenViewModel @Inject constructor(nairaland: Nairaland, application: Application) :
    FormViewModel<NewPostForm>(nairaland, application) {

    override val formData = NewPostFormData()
    private lateinit var topic: Topic
    private var post: SimplePost? = null

    private lateinit var _posts: List<QuotablePost>
    val quotablePosts get() = _posts

    private var _getPostEvent = MutableLiveData<Event<Resource<String>>>()
    val getPostEvent: LiveData<Event<Resource<String>>> = _getPostEvent

    override val loadFormFirst: Boolean get() = true

    fun initialize(
        topic: Topic,
        post: SimplePost?
    ) {
        this.topic = topic
        this.post = post
        initialize()
    }

    override suspend fun doFetchFormData(): Result<Either<NewPostForm, AreYouMuslimDeclarationForm>, ErrorResponse> {
        val result = if (post == null) {
            nairaland.sources.forms.newPost(topic.id.toString())
        } else {
            nairaland.sources.forms.quotePost(post!!)
        }
        if (result is Ok && result.value is Either.Left) {
            val form = (result.value as Either.Left<NewPostForm>).data
            _posts = form.quotablePosts
        }

        return result
    }


    override fun prepareFormForSubmission(): NewPostForm {
        return super.prepareFormForSubmission().apply {
            this.follow = formData.followTopic
        }
    }

    fun getPost(post: QuotablePost) {
        viewModelScope.launch {
            _getPostEvent.value = Event(Resource.Loading())
            _getPostEvent.value =
                Event(nairaland.sources.forms.getQuotablePostContent(post).toResource(null))
        }
    }

    override suspend fun doSubmitFormData(form: NewPostForm): Result<PostListDataPage, ErrorResponse> {
        return nairaland.sources.submissions.newPost(form)
    }
}
