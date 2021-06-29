package com.amebo.amebo.screens.newpost.modifypost

import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.TestNairalandProvider
import com.amebo.amebo.screens.newpost.FormData
import com.amebo.amebo.suite.MainCoroutineRule
import com.amebo.core.Nairaland
import com.amebo.core.common.Either
import com.amebo.core.domain.AreYouMuslimDeclarationForm
import com.amebo.core.domain.ModifyForm
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

@RunWith(AndroidJUnit4::class)
class ModifyPostScreenViewModelTest {

    private lateinit var nairaland: Nairaland
    private lateinit var viewModel: ModifyPostScreenViewModel
    private val post = TestData.newPost(modifiable = true)
    private lateinit var form: ModifyForm
    private lateinit var areYouMuslimDeclarationForm: AreYouMuslimDeclarationForm

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun before() {
        areYouMuslimDeclarationForm = AreYouMuslimDeclarationForm(
            session = "sdfsdf",
            redirect = "dfsdfd",
            accept = "dfd",
            accepted = false,
            decline = "sdfd"
        )
        form = ModifyForm(
            "form_body", "form_title", titleEditable = true, attachments = mutableListOf(),
            post = post.id.toLong(), redirect = "", session = ""
        )
        nairaland = TestNairalandProvider.newNairalandInstance()
        viewModel =
            ModifyPostScreenViewModel(nairaland, ApplicationProvider.getApplicationContext())
    }


    @Test
    fun onViewModelInitialized_formLoadingEventDispatched() = runBlocking {
        // setup
        val observer: Observer<Event<Resource<FormData>>> = mock()
        val captor = argumentCaptor<Event<Resource<FormData>>>()
        viewModel.formLoadingEvent.observeForever(observer)

        val postCaptor = argumentCaptor<SimplePost>()
        val formLoadingResult = Ok(Either.Left(form))
        whenever(nairaland.sources.forms.modifyPost(postCaptor.capture()))
            .thenReturn(formLoadingResult)

        // action
        viewModel.initialize(post)

        // VERIFY
        assertThat(viewModel.canSubmit).isEqualTo(true)

        // check for Resource.Loading && Resource.Content
        verify(observer, times(2)).onChanged(captor.capture())
        assertThat(captor.firstValue.peekContent()).isInstanceOf(Resource.Loading::class.java)
        assertThat(captor.secondValue.peekContent()).isInstanceOf(Resource.Success::class.java)

        // check nairaland.sources.forms::modifyPost was called with post passed
        assertThat(postCaptor.firstValue).isEqualTo(post)
        verify(nairaland.sources.forms, times(1)).modifyPost(any())

        // check formData is initialized with contents from form
        assertThat(viewModel.formData.body).isEqualTo(form.body)
        assertThat(viewModel.formData.title).isEqualTo(form.title)
        assertThat(viewModel.formData.titleIsEditable).isEqualTo(form.titleEditable)

        // cleanup
        viewModel.formLoadingEvent.removeObserver(observer)
    }


    @Test
    fun onSubmit_formIsFilledOutAndSubmitted() = runBlocking {
        // SETUP
        val observer: Observer<Event<Resource<PostListDataPage>>> = mock()
        viewModel.formSubmissionEvent.observeForever(observer)

        val submissionCaptor = argumentCaptor<Event<Resource<PostListDataPage>>>()
        val modifyFormCaptor = argumentCaptor<ModifyForm>()

        whenever(nairaland.sources.forms.modifyPost(any()))
            .thenReturn(Ok(Either.Left(form)))
        whenever(nairaland.sources.submissions.modifyPost(modifyFormCaptor.capture()))
            .thenReturn(Ok(TestData.newTopicPostList()))

        viewModel.initialize(post)

        // ACTION
        viewModel.formData.body = "bodyFromFormData"
        viewModel.formData.title = "titleFromFormData"
        viewModel.submitForm()

        // VERIFY
        // check form is filled out
        assertThat(modifyFormCaptor.firstValue.body).isEqualTo(viewModel.formData.body)
        assertThat(modifyFormCaptor.firstValue.title).isEqualTo(viewModel.formData.title)

        // check for submission loadingEvent and successEvent
        verify(observer, times(2)).onChanged(submissionCaptor.capture())
        assertThat(submissionCaptor.firstValue.peekContent()).isInstanceOf(Resource.Loading::class.java)
        assertThat(submissionCaptor.secondValue.peekContent()).isInstanceOf(Resource.Success::class.java)

        // cleanup
        viewModel.formSubmissionEvent.removeObserver(observer)
    }
}