package com.amebo.amebo.screens.newpost.newpost

import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.TestNairalandProvider
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.newpost.FormData
import com.amebo.amebo.suite.MainCoroutineRule
import com.amebo.core.Nairaland
import com.amebo.core.common.Either
import com.amebo.core.domain.Attachment
import com.amebo.core.domain.NewPostForm
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.SimplePost
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NewPostScreenViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: NewPostScreenViewModel
    private lateinit var nairaland: Nairaland
    private lateinit var newPostForm: NewPostForm
    private var topic = TestData.topics.first()
    private var post = TestData.newPost()

    @Before
    fun before() {
        newPostForm = NewPostForm("body", "topic", 10, "title", "session")
        nairaland = TestNairalandProvider.newNairalandInstance()
        viewModel = NewPostScreenViewModel(nairaland, ApplicationProvider.getApplicationContext())
    }


    @Test
    fun newPost_onViewModelInitialized_formSuccessEventDispatched() {
        // setup
        val observer: Observer<Event<Resource<FormData>>> = mock()
        val captor = argumentCaptor<Event<Resource<FormData>>>()
        viewModel.formLoadingEvent.observeForever(observer)

        // action
        viewModel.initialize(topic, null)

        // verify
        assertThat(viewModel.canSubmit).isEqualTo(false)
        verify(observer, times(1)).onChanged(captor.capture())
        assertThat(captor.firstValue.peekContent()).isInstanceOf(Resource.Success::class.java)

        // cleanup
        viewModel.formLoadingEvent.removeObserver(observer)
    }

    @Test
    fun quotePost_onViewModelInitialized_formLoadingEventDispatched() = runBlocking {
        // SETUP
        val observer: Observer<Event<Resource<FormData>>> = mock()
        viewModel.formLoadingEvent.observeForever(observer)

        val captor = argumentCaptor<Event<Resource<FormData>>>()
        val postCaptor = argumentCaptor<SimplePost>()
        whenever(nairaland.sources.forms.quotePost(postCaptor.capture()))
            .thenReturn(Ok(Either.Left(newPostForm)))

        // action
        viewModel.initialize(post.topic, post)

        // =======
        // VERIFY
        // =======

        // form is loaded
        verify(nairaland.sources.forms, times(1)).quotePost(any())
        assertThat(postCaptor.firstValue).isEqualTo(post)

        // formData filled out
        assertThat(newPostForm.body).isEqualTo(viewModel.formData.body)
        assertThat(newPostForm.title).isEqualTo(viewModel.formData.title)
        assertThat(viewModel.canSubmit).isEqualTo(true)

        // form loading and success event's dispatched
        verify(observer, times(2)).onChanged(captor.capture())
        assertThat(captor.firstValue.peekContent()).isInstanceOf(Resource.Loading::class.java)
        assertThat(captor.secondValue.peekContent()).isInstanceOf(Resource.Success::class.java)

        // cleanup
        viewModel.formLoadingEvent.removeObserver(observer)
    }

    @Test
    fun newPost_onSubmissionTriggered_formIsFetchedFilledOutAndSubmitted() = runBlocking {
        // setup
        val observer: Observer<Event<Resource<PostListDataPage>>> = mock()
        viewModel.formSubmissionEvent.observeForever(observer)

        val topicIdCaptor = argumentCaptor<String>()
        val newFormCaptor = argumentCaptor<NewPostForm>()
        val submissionCaptor = argumentCaptor<Event<Resource<PostListDataPage>>>()

        // for call from viewModel::initialize
        whenever(nairaland.sources.forms.newPost(topicIdCaptor.capture()))
            .thenReturn(Ok(Either.Left(newPostForm)))

        // capture form submitted
        whenever(nairaland.sources.submissions.newPost(newFormCaptor.capture()))
            .thenReturn(Ok(TestData.newTopicPostList()))

        viewModel.initialize(topic, null)

        // ACTION
        viewModel.formData.body = "bodyFromFormData"
        viewModel.formData.title = "titleFromFormData"
        viewModel.submitForm()

        //========
        // VERIFY
        // =======

        // form is loaded with correct topicId
        verify(nairaland.sources.forms, times(1)).newPost(any())
        assertThat(topicIdCaptor.firstValue).isEqualTo(topic.id.toString())

        // check form is filled out
        assertThat(newFormCaptor.firstValue.body).isEqualTo(viewModel.formData.body)
        assertThat(newFormCaptor.firstValue.title).isEqualTo(viewModel.formData.title)
        // .. and submission call made
        verify(nairaland.sources.submissions, times(1)).newPost(any())

        // check for loadingEvent and successEvent
        verify(observer, times(2)).onChanged(submissionCaptor.capture())
        assertThat(submissionCaptor.firstValue.peekContent()).isInstanceOf(Resource.Loading::class.java)
        assertThat(submissionCaptor.secondValue.peekContent()).isInstanceOf(Resource.Success::class.java)

        // cleanup
        viewModel.formSubmissionEvent.removeObserver(observer)
    }

    @Test
    fun onUpdateImages_removedItemRemovalAttemptMade() = runBlocking {
        // SETUP

        val observer = mock<Observer<Event<Resource<ImageItem.Existing>>>>()
        viewModel.existingImageRemovalEvent.observeForever(observer)

        val attachment = mock<Attachment>()
        val existingItem = mock<ImageItem.Existing>()
        val removedItems = listOf(existingItem, existingItem)
        whenever(existingItem.attachment).thenReturn(attachment)

        val attachmentCaptor = argumentCaptor<Attachment>()
        whenever(nairaland.sources.submissions.removeAttachment(attachmentCaptor.capture()))
            .thenReturn(Ok(Unit))

        // ACTION
        viewModel.updateImages(emptyList(), removedItems)

        // VERIFY

        // that relevant submissions method called
        verify(nairaland.sources.submissions, times(2)).removeAttachment(any())
        assertThat(attachmentCaptor.firstValue).isEqualTo(attachment)
        assertThat(attachmentCaptor.secondValue).isEqualTo(attachment)

        // that loading and success events dispatched
        val eventCaptor = argumentCaptor<Event<Resource<ImageItem.Existing>>>()
        verify(observer, times(4)).onChanged(eventCaptor.capture())
        for (i in 0 until (eventCaptor.allValues.size / 2)) {
            val firstValue = eventCaptor.allValues[0 * 2]
            val secondValue = eventCaptor.allValues[0 * 2 + 1]
            assertThat(firstValue.peekContent()).isInstanceOf(Resource.Loading::class.java)
            assertThat(secondValue.peekContent()).isInstanceOf(Resource.Success::class.java)
        }

        viewModel.existingImageRemovalEvent.removeObserver(observer)
    }

    @Test
    fun onUpdateImages_imageCountEventPosted() = runBlocking {
        // SETUP
        val observer = mock<Observer<Event<Int>>>()
        viewModel.imageCountEvent.observeForever(observer)

        // list of `new image count`
        val cases = listOf(3,3,1,0)


        whenever(nairaland.sources.submissions.removeAttachment(any()))
            .thenReturn(Ok(Unit))

        // ACTION
        cases.forEach { newCount ->
            val newImages = (0 until newCount).map { mock<ImageItem.New>()}
            // newPostScreen need not have images to remove
            viewModel.updateImages(newImages, emptyList())
        }

        // VERIFY
        verify(observer, times(cases.size)).onChanged(any())
        viewModel.imageCountEvent.removeObserver(observer)
    }
}