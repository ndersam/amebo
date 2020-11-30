package com.amebo.amebo.screens.newpost.modifypost

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.TestModifyPostScreenModule
import com.amebo.amebo.di.TestRouterModule
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.imagepicker.ImagePickerSharedViewModel
import com.amebo.amebo.screens.imagepicker.PostImagesUpdate
import com.amebo.amebo.screens.newpost.FormData
import com.amebo.amebo.suite.assertFragmentResultSet
import com.amebo.amebo.suite.injectIntoTestApp
import com.amebo.amebo.suite.launchFragmentInTestActivity
import com.amebo.amebo.suite.setupViewModelFactory
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.SimplePost
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModifyPostScreenTest {

    private lateinit var scenario: ActivityScenario<TestFragmentActivity>
    private val post = TestData.newPost()
    private val bundle =  ModifyPostScreen.newBundle(post)
    private lateinit var modifyPostView: ModifyPostView

    private lateinit var imagePickerSharedViewModel: ImagePickerSharedViewModel
    private lateinit var userManagementViewModel : UserManagementViewModel
    private lateinit var viewModel: ModifyPostScreenViewModel

    private lateinit var formSubmissionEvent: MutableLiveData<Event<Resource<PostListDataPage>>>
    private lateinit var formLoadingEvent: MutableLiveData<Event<Resource<FormData>>>
    private lateinit var imageCountEvent: MutableLiveData<Event<Int>>
    private lateinit var existingImageRemovalEvent: MutableLiveData<Event<Resource<ImageItem.Existing>>>
    private lateinit var imagesUpdatedEvent: MutableLiveData<Event<PostImagesUpdate>>

    @Before
    fun before() {
        injectIntoTestApp()
    }

    @Test
    fun onCreateView_viewModelIsInitialized() {
        initialize(useFakeLiveData = true)
        val captor = argumentCaptor<SimplePost>()
        verify(viewModel, times(1)).initialize(captor.capture())
        assertThat(captor.firstValue).isEqualTo(post)
    }

    @Test
    fun onSubmissionSuccess_dataIsSetAndRouterGoesToPreviousScreen() {
        // setup
        initialize(useFakeLiveData = false)
        val data = mock<PostListDataPage>()

        // action
        val success = Event(Resource.Success(data))
        formSubmissionEvent.value = success

        // VERIFY
        // router goes to previous screen
        verify(modifyPostView, times(1)).onSubmissionSuccess(success.peekContent())
        verify(TestRouterModule.router, times(1)).back()

        // data is set
        val bundle = scenario.assertFragmentResultSet(FragKeys.RESULT_POST_LIST)
        val result = bundle?.getParcelable<PostListDataPage>(FragKeys.BUNDLE_POST_LIST)
        assertThat(result).isNotNull()
        assertThat(result).isInstanceOf(PostListDataPage::class.java)
        assertThat(result).isEqualTo(data)
    }

    private fun initialize(useFakeLiveData: Boolean = true){
        setupViewModels()
        if (useFakeLiveData){
            initLiveDataMocks()
        } else {
            initLiveDataReal()
        }
        whenever(viewModel.formLoadingEvent).thenReturn(formLoadingEvent)
        whenever(viewModel.formSubmissionEvent).thenReturn(formSubmissionEvent)
        whenever(viewModel.imageCountEvent).thenReturn(imageCountEvent)
        whenever(viewModel.existingImageRemovalEvent).thenReturn(existingImageRemovalEvent)
        whenever(imagePickerSharedViewModel.imagesUpdatedEvent).thenReturn(imagesUpdatedEvent)

        scenario = launchFragmentInTestActivity(ModifyPostScreen(), bundle)
        modifyPostView = TestModifyPostScreenModule.modifyPostView
    }

    private fun initLiveDataMocks() {
        formSubmissionEvent = mock()
        formLoadingEvent = mock()
        imageCountEvent = mock()
        existingImageRemovalEvent = mock()
        imagesUpdatedEvent = mock()
    }

    private fun initLiveDataReal() {
        formSubmissionEvent = MutableLiveData()
        formLoadingEvent = MutableLiveData()
        imageCountEvent = MutableLiveData()
        existingImageRemovalEvent = MutableLiveData()
        imagesUpdatedEvent = MutableLiveData()
    }

    private fun setupViewModels() {
        imagePickerSharedViewModel = mock()
        userManagementViewModel = mock()
        viewModel = mock()

        setupViewModelFactory(imagePickerSharedViewModel)
        setupViewModelFactory(userManagementViewModel)
        setupViewModelFactory(viewModel)
    }
}