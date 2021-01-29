package com.amebo.amebo.screens.newpost.newpost

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.TestRouterModule.Companion.router
import com.amebo.amebo.di.mocks.Mocks
import com.amebo.amebo.di.mocks.Mocks.ImagePickerShared.imagesUpdatedEvent
import com.amebo.amebo.di.mocks.Mocks.NewPostScreen.existingImageRemovalEvent
import com.amebo.amebo.di.mocks.Mocks.NewPostScreen.formLoadingEvent
import com.amebo.amebo.di.mocks.Mocks.NewPostScreen.formSubmissionEvent
import com.amebo.amebo.di.mocks.Mocks.NewPostScreen.getPostEvent
import com.amebo.amebo.di.mocks.Mocks.NewPostScreen.imageCountEvent
import com.amebo.amebo.di.mocks.Mocks.NewPostScreen.muslimDeclarationEvent
import com.amebo.amebo.di.mocks.Mocks.NewPostScreen.newPostView
import com.amebo.amebo.di.mocks.Mocks.PrefModule.pref
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.imagepicker.ImagePickerSharedViewModel
import com.amebo.amebo.screens.imagepicker.PostImagesUpdate
import com.amebo.amebo.screens.newpost.NewPostFormData
import com.amebo.amebo.suite.*
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.TopicPostListDataPage
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewPostScreenTest {
    private lateinit var scenario: ActivityScenario<TestFragmentActivity>
    private lateinit var viewModel: NewPostScreenViewModel
    private lateinit var userManagementViewModel: UserManagementViewModel
    private lateinit var imagePickerSharedViewModel: ImagePickerSharedViewModel

    private val topic = TestData.topics.first()
    private val formData = NewPostFormData(title = topic.title, body = "", followTopic = true)


    @Before
    fun before() {
        viewModel = Mocks.NewPostScreen.createVM()
        userManagementViewModel = Mocks.UserManagement.new()
        imagePickerSharedViewModel = Mocks.ImagePickerShared.new()
        whenever(viewModel.formData).thenReturn(formData)

        injectIntoTestApp()

        setupViewModelFactory(imagePickerSharedViewModel)
        setupViewModelFactory(userManagementViewModel)
        setupViewModelFactory(viewModel)
        pref.isLoggedIn = true

        scenario = launchActivity()
        scenario.setFragment(NewPostScreen(), NewPostScreen.newBundle(topic))
    }


    @Test
    fun newPost_onCreateView_viewModelsInitialized() {
        whenever(viewModel.canSubmit).thenReturn(false)

        verify(viewModel, times(1)).initialize(topic, null)
        assertThat(formLoadingEvent.hasActiveObservers()).isTrue()
        assertThat(formSubmissionEvent.hasActiveObservers()).isTrue()
        assertThat(existingImageRemovalEvent.hasActiveObservers()).isTrue()
        assertThat(imageCountEvent.hasActiveObservers()).isTrue()
        assertThat(imagesUpdatedEvent.hasActiveObservers()).isTrue()
        assertThat(muslimDeclarationEvent.hasActiveObservers()).isTrue()
        assertThat(getPostEvent.hasActiveObservers()).isTrue()
    }

    @Test
    fun onImagesUpdate_viewIsUpdated() {
        val event = Event(2)
        imageCountEvent.value = event

        verify(newPostView, times(1)).setFileCount(event.peekContent())

        scenario.recreate()

        // check if previous event is relayed
        verify(newPostView, times(1)).setFileCount(event.peekContent())
    }

    @Test
    fun onFormEventDispatched_appropriateViewMethodIsCalled() {
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
        // action
        val success = Event(Resource.Success(mock<PostListDataPage>()))
        formSubmissionEvent.value = success
        // verify
        verify(newPostView, times(1)).onSubmissionSuccess(success.peekContent())
        verify(router, times(1)).back()

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
        val event = Event(PostImagesUpdate(emptyList(), emptyList()))
        imagesUpdatedEvent.value = event
        val res = event.peekContent()
        verify(viewModel, times(1)).updateImages(res.allNew, res.existingRemoved)
    }

    @Test
    fun onImagesRemovedEventDispatched_appropriateViewMethodCalled() {
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
    fun onMuslimDeclarationEventDispatched_routeToMuslimDeclaration() {
        muslimDeclarationEvent.value = Event(mock())
        verify(router).toMuslimDeclaration(any())
    }

    @Test
    fun onGetPostEventSuccess_appropriateViewMethodCalled() {
        getPostEvent.value = Event(Resource.Success("data"))
        verify(newPostView).insertText(any())
    }

    @Test
    fun onViewBodyText_viewModelFormDataUpdated() {
        val text = "text"
        scenario.onFragment<NewPostScreen> {
            it.setPostBody(text)
        }
        assertThat(formData.body).isEqualTo(text)
    }

    @Test
    fun onViewTitleText_viewModelFormDataUpdated() {
        val text = "text"
        scenario.onFragment<NewPostScreen> {
            it.setPostTitle(text)
        }
        assertThat(formData.title).isEqualTo(text)
    }

    @Test
    fun onViewFollowTopicUpdated_viewModelFormDataUpdated() {
        val follow = false
        scenario.onFragment<NewPostScreen> {
            it.setFollowTopic(follow)
        }
        assertThat(formData.followTopic).isEqualTo(follow)
    }

    @Test
    fun onShowRulesMenuItemClicked_rulesScreenShown() {
        scenario.onFragment<NewPostScreen> {
            it.onOptionsItemSelected(newMenuItem(R.id.show_rules))
            verify(router, times(1)).toPostingRules()
        }
    }

    @Test
    fun callingRetry_initiatesRequestRetry() {
        scenario.onFragment<NewPostScreen> {
            it.retryLastRequest()
            verify(viewModel, times(1)).retry()
        }
    }

    @Test
    fun onAttachFile_imagesAreCorrectlySetup() {
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
        val selected: List<ImageItem.New> = listOf(mock(), mock(), mock())
        whenever(viewModel.selectedImages).thenReturn(selected)
        val existing: MutableList<ImageItem.Existing> = mutableListOf(mock())
        whenever(viewModel.existingImages).thenReturn(existing)

        scenario.onFragment<NewPostScreen> {
            it.attachFile()
            verify(router, times(1)).toImagePicker()
        }
    }

    @Test
    fun onPreviewClicked_postPreviewOpened() {
        val rawTextCaptor = argumentCaptor<String>()
        val processedTextCaptor = argumentCaptor<String>()
        whenever(viewModel.preparePreview(any(), rawTextCaptor.capture())).thenReturn("processed")

        scenario.onFragment<NewPostScreen> {
            it.preview("text")
            verify(router, times(1)).toPostPreview(processedTextCaptor.capture())
            assertThat(rawTextCaptor.firstValue).isEqualTo("text")
            assertThat(processedTextCaptor.firstValue).isEqualTo("processed")
        }
    }

    @Test
    fun onGoBack_moveToPreviousScreen() {
        scenario.onFragment<NewPostScreen> {
            it.goBack()
            verify(router, times(1)).back()
        }
    }

    @Test
    fun onEditorSettingsClicked_editorSettingsOpened() {
        scenario.onFragment<NewPostScreen> {
            it.openSettings()
            verify(router, times(1)).toPostEditorSettings()
        }
    }

    @Test
    fun onDismissDialog_showKeyboard() {
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
        val data = mock<TopicPostListDataPage>()

        // action
        val success = Event(Resource.Success(data))
        formSubmissionEvent.value = success

        // VERIFY
        // router goes to previous screen
        verify(newPostView, times(1)).onSubmissionSuccess(success.peekContent())
        verify(router, times(1)).back()

        // data is set
        val bundle = scenario.assertFragmentResultSet(FragKeys.RESULT_POST_LIST)
        val result = bundle?.getParcelable<TopicPostListDataPage>(FragKeys.BUNDLE_POST_LIST)
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(TopicPostListDataPage::class.java)
        assertThat(result).isEqualTo(data)
    }

}