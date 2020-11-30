package com.amebo.amebo.screens.newpost.newpost

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.TestNewPostScreenModule
import com.amebo.amebo.di.TestRouterModule
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.imagepicker.ImagePickerSharedViewModel
import com.amebo.amebo.screens.imagepicker.PostImagesUpdate
import com.amebo.amebo.screens.newpost.FormData
import com.amebo.amebo.screens.newpost.NewPostFormData
import com.amebo.amebo.suite.*
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.TopicPostListDataPage
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewPostScreenTest {

    lateinit var scenario: ActivityScenario<TestFragmentActivity>
    private lateinit var viewModel: NewPostScreenViewModel
    private lateinit var userManagementViewModel: UserManagementViewModel
    private lateinit var imagePickerSharedViewModel: ImagePickerSharedViewModel
    private lateinit var formLoadingEvent: MutableLiveData<Event<Resource<FormData>>>
    private lateinit var formSubmissionEvent: MutableLiveData<Event<Resource<PostListDataPage>>>
    private lateinit var imageCountEvent: MutableLiveData<Event<Int>>
    private lateinit var existingImageRemovalEvent: MutableLiveData<Event<Resource<ImageItem.Existing>>>
    private lateinit var imagesUpdatedEvent: MutableLiveData<Event<PostImagesUpdate>>

    private val topic = TestData.topics.first()
    private val formData = NewPostFormData(title = topic.title, body = "", followTopic = true)
    private val newPostView get() = TestNewPostScreenModule.newPostView


    @Test
    fun newPost_onCreateView_viewModelsInitialized() {
        initialize()

        whenever(viewModel.canSubmit).thenReturn(false)

        verify(viewModel, times(1)).formLoadingEvent
        verify(viewModel, times(1)).formSubmissionEvent
        verify(viewModel, times(1)).existingImageRemovalEvent
        verify(viewModel, times(1)).imageCountEvent
        verify(imagePickerSharedViewModel, times(1)).imagesUpdatedEvent
        verify(viewModel, times(1)).initialize(topic, null)

        verify(formLoadingEvent, times(1)).observe(any(), any())
        verify(formSubmissionEvent, times(1)).observe(any(), any())
        verify(existingImageRemovalEvent, times(1)).observe(any(), any())
        verify(imageCountEvent, times(1)).observe(any(), any())
        verify(imagesUpdatedEvent, times(1)).observe(any(), any())
    }

    @Test
    fun onImagesUpdate_viewIsUpdated() {
        initialize(useFakeLiveData = false)
        val event = Event(2)
        imageCountEvent.value = event

        verify(newPostView, times(1)).setFileCount(event.peekContent())

        scenario.recreate()

        // check if previous event is relayed
        verify(newPostView, times(1)).setFileCount(event.peekContent())
    }

    @Test
    fun onFormEventDispatched_appropriateViewMethodIsCalled() {
        initialize(useFakeLiveData = false)

        val success = Event(Resource.Success(formData))
        formLoadingEvent.value = success
        verify(newPostView, times(1)).onFormSuccess(success.peekContent())

        val loading = Event(Resource.Loading(null))
        formLoadingEvent.value = loading
        verify(newPostView, times(1)).onFormLoading(loading.peekContent())

        val error = Event(Resource.Error(ErrorResponse.Network, null))
        formLoadingEvent.value = error
        verify(newPostView, times(1)).onFormError(error.peekContent())
    }

    @Test
    fun onFormSubmissionEventDispatched_appropriateViewMethodCalled() {
        initialize(useFakeLiveData = false)

        // action
        val success = Event(Resource.Success(mock<PostListDataPage>()))
        formSubmissionEvent.value = success
        // verify
        verify(newPostView, times(1)).onSubmissionSuccess(success.peekContent())
        verify(TestRouterModule.router, times(1)).back()

        // action
        val loading: Event<Resource<PostListDataPage>> = Event(Resource.Loading(null))
        formSubmissionEvent.value = loading
        // verify
        verify(
            newPostView,
            times(1)
        ).onSubmissionLoading(loading.peekContent() as Resource.Loading<PostListDataPage>)

        // action
        val error = Event(Resource.Error(ErrorResponse.Network, null))
        formSubmissionEvent.value = error
        // verify
        verify(newPostView, times(1)).onSubmissionError(error.peekContent())
    }

    @Test
    fun onPostImagesUpdateEventDispatched_fragmentsViewModelIsUpdated() {
        initialize(useFakeLiveData = false)

        val event = Event(PostImagesUpdate(emptyList(), emptyList()))
        imagesUpdatedEvent.value = event
        val res = event.peekContent()
        verify(viewModel, times(1)).updateImages(res.allNew, res.existingRemoved)
    }

    @Test
    fun onImagesRemovedEventDispatched_appropriateViewMethodCalled() {
        initialize(useFakeLiveData = false)

        val success: Event<Resource<ImageItem.Existing>> = Event(Resource.Success(mock()))
        existingImageRemovalEvent.value = success
        verify(
            newPostView,
            times(1)
        ).onExistingImageRemovalSuccess(success.peekContent() as Resource.Success<ImageItem.Existing>)

        val loading: Event<Resource<ImageItem.Existing>> = Event(Resource.Loading(null))
        existingImageRemovalEvent.value = loading
        verify(
            newPostView,
            times(1)
        ).onExistingImageRemovalLoading(loading.peekContent() as Resource.Loading<ImageItem.Existing>)

        val error: Event<Resource<ImageItem.Existing>> = Event(Resource.Error(mock(), null))
        existingImageRemovalEvent.value = error
        verify(
            newPostView,
            times(1)
        ).onExistingImageRemovalError(error.peekContent() as Resource.Error<ImageItem.Existing>)
    }

    @Test
    fun onViewBodyText_viewModelFormDataUpdated() {
        initialize()
        val text = "text"
        scenario.onFragment<NewPostScreen> {
            it.setPostBody(text)
        }
        assertThat(formData.body).isEqualTo(text)
    }

    @Test
    fun onViewTitleText_viewModelFormDataUpdated() {
        initialize()
        val text = "text"
        scenario.onFragment<NewPostScreen> {
            it.setPostTitle(text)
        }
        assertThat(formData.title).isEqualTo(text)
    }

    @Test
    fun onViewFollowTopicUpdated_viewModelFormDataUpdated() {
        initialize()
        val follow = false
        scenario.onFragment<NewPostScreen> {
            it.setFollowTopic(follow)
        }
        assertThat(formData.followTopic).isEqualTo(follow)
    }

    @Test
    fun onSubmitMenuItemClicked_initiateSubmission() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.onOptionsItemSelected(newMenuItem(R.id.submit))
            verify(viewModel, times(1)).submitForm()
        }
    }

    @Test
    fun onRedoMenuItemClicked_redo() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.onOptionsItemSelected(newMenuItem(R.id.redo))
            verify(newPostView, times(1)).redo()
        }
    }

    @Test
    fun onUndoMenuItemClicked_undo() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.onOptionsItemSelected(newMenuItem(R.id.undo))
            verify(newPostView, times(1)).undo()
        }
    }

    @Test
    fun onShowRulesMenuItemClicked_rulesScreenShown() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.onOptionsItemSelected(newMenuItem(R.id.show_rules))
            verify(TestRouterModule.router, times(1)).toPostingRules()
        }
    }

    @Test
    fun callingRetry_initiatesRequestRetry() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.retryLastRequest()
            verify(viewModel, times(1)).retry()
        }
    }

    @Test
    fun onAttachFile_imagesAreCorrectlySetup() {
        initialize()
        val selected: List<ImageItem.New> = listOf(mock(), mock(), mock())
        whenever(viewModel.selectedImages).thenReturn(selected)

        val existing: MutableList<ImageItem.Existing> = mutableListOf(mock())
        whenever(viewModel.existingImages).thenReturn(existing)

        scenario.onFragment<NewPostScreen> {
            it.attachFile()
            verify(viewModel, times(1)).selectedImages
            verify(viewModel, times(1)).existingImages

            // images correctly passed
            val imagesCaptor = argumentCaptor<List<ImageItem>>()
            verify(imagePickerSharedViewModel, times(1)).setImages(imagesCaptor.capture())
            assertThat(imagesCaptor.firstValue).containsExactlyElementsIn(existing + selected)
        }
    }

    @Test
    fun onAttachFile_openImagePicker() {
        initialize()
        val selected: List<ImageItem.New> = listOf(mock(), mock(), mock())
        whenever(viewModel.selectedImages).thenReturn(selected)
        val existing: MutableList<ImageItem.Existing> = mutableListOf(mock())
        whenever(viewModel.existingImages).thenReturn(existing)

        scenario.onFragment<NewPostScreen> {
            it.attachFile()
            verify(TestRouterModule.router, times(1)).toImagePicker()
        }
    }

    @Test
    fun onPreviewClicked_postPreviewOpened() {
        initialize()
        val rawTextCaptor = argumentCaptor<String>()
        val processedTextCaptor = argumentCaptor<String>()
        whenever(viewModel.preparePreview(any(), rawTextCaptor.capture())).thenReturn("processed")

        scenario.onFragment<NewPostScreen> {
            it.preview("text")
            verify(TestRouterModule.router, times(1)).toPostPreview(processedTextCaptor.capture())
            assertThat(rawTextCaptor.firstValue).isEqualTo("text")
            assertThat(processedTextCaptor.firstValue).isEqualTo("processed")
        }
    }

    @Test
    fun onGoBack_moveToPreviousScreen() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.goBack()
            verify(TestRouterModule.router, times(1)).back()
        }
    }

    @Test
    fun onEditorSettingsClicked_editorSettingsOpened() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.openSettings()
            verify(TestRouterModule.router, times(1)).toPostEditorSettings()
        }
    }

    @Test
    fun onDismissDialog_showKeyboard() {
        initialize()
        scenario.onFragment<NewPostScreen> {
            it.onDismissDialog()
            val onBodyCaptor = argumentCaptor<Boolean>()
            verify(newPostView, times(1)).showKeyboard(onBodyCaptor.capture())
            assertThat(onBodyCaptor.firstValue).isEqualTo(true)
        }
    }

    @Test
    fun onSubmissionSuccess_dataIsSetAndRouterGoesToPreviousScreen() {
        // setup
        initialize(useFakeLiveData = false)
        val data = mock<TopicPostListDataPage>()

        // action
        val success = Event(Resource.Success(data))
        formSubmissionEvent.value = success

        // VERIFY
        // router goes to previous screen
        verify(newPostView, times(1)).onSubmissionSuccess(success.peekContent())
        verify(TestRouterModule.router, times(1)).back()

        // data is set
        val bundle = scenario.assertFragmentResultSet(FragKeys.RESULT_POST_LIST)
        val result = bundle?.getParcelable<TopicPostListDataPage>(FragKeys.BUNDLE_POST_LIST)
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(TopicPostListDataPage::class.java)
        assertThat(result).isEqualTo(data)
    }

    private fun setupViewModels() {
        setupViewModelFactory(imagePickerSharedViewModel)
        setupViewModelFactory(userManagementViewModel)
        setupViewModelFactory(viewModel)
    }

    private fun initMocks(useFakeLiveData: Boolean = true) {
        viewModel = mock()
        userManagementViewModel = mock()
        imagePickerSharedViewModel = mock()
        if (useFakeLiveData) {
            formLoadingEvent = mock()
            formSubmissionEvent = mock()
            imageCountEvent = mock()
            existingImageRemovalEvent = mock()
            imagesUpdatedEvent = mock()
        } else {
            formLoadingEvent = MutableLiveData()
            formSubmissionEvent = MutableLiveData()
            imageCountEvent = MutableLiveData()
            existingImageRemovalEvent = MutableLiveData()
            imagesUpdatedEvent = MutableLiveData()
        }

        whenever(viewModel.formLoadingEvent).thenReturn(formLoadingEvent)
        whenever(viewModel.formSubmissionEvent).thenReturn(formSubmissionEvent)
        whenever(viewModel.imageCountEvent).thenReturn(imageCountEvent)
        whenever(viewModel.existingImageRemovalEvent).thenReturn(existingImageRemovalEvent)
        whenever(imagePickerSharedViewModel.imagesUpdatedEvent).thenReturn(imagesUpdatedEvent)

        whenever(viewModel.formData).thenReturn(formData)
    }


    private fun initialize(useFakeLiveData: Boolean = true) {
        initMocks(useFakeLiveData)
        injectIntoTestApp()
        setupViewModels()
        scenario = launchActivity()
        scenario.setFragment(NewPostScreen(), NewPostScreen.newBundle(topic))
    }
}