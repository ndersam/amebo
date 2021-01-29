package com.amebo.amebo.screens.newpost.modifypost

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.data.TestData
import com.amebo.amebo.di.TestModifyPostScreenModule
import com.amebo.amebo.di.TestRouterModule
import com.amebo.amebo.di.mocks.Mocks
import com.amebo.amebo.di.mocks.Mocks.ModifyPostScreen.formSubmissionEvent
import com.amebo.amebo.di.mocks.Mocks.ModifyPostScreen.vm
import com.amebo.amebo.suite.assertFragmentResultSet
import com.amebo.amebo.suite.injectIntoTestApp
import com.amebo.amebo.suite.launchFragmentInTestActivity
import com.amebo.amebo.suite.setupViewModelFactory
import com.amebo.core.domain.PostListDataPage
import com.amebo.core.domain.SimplePost
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModifyPostScreenTest {

    private lateinit var scenario: ActivityScenario<TestFragmentActivity>
    private val post = TestData.newPost()
    private val bundle = ModifyPostScreen.newBundle(post)
    private lateinit var modifyPostView: ModifyPostView


    @Before
    fun before() {
        injectIntoTestApp()
    }

    @Test
    fun onCreateView_viewModelIsInitialized() {
        initialize(mockLiveData = true)
        val captor = argumentCaptor<SimplePost>()
        verify(vm, times(1)).initialize(captor.capture())
        assertThat(captor.firstValue).isEqualTo(post)
    }

    @Test
    fun onSubmissionSuccess_dataIsSetAndRouterGoesToPreviousScreen() {
        // setup
        initialize(mockLiveData = false)
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

    private fun initialize(mockLiveData: Boolean = true) {
        Mocks.ModifyPostScreen.createVM(mockLiveData)
        setupViewModelFactory(Mocks.ImagePickerShared.new())
        setupViewModelFactory(Mocks.UserManagement.new())
        setupViewModelFactory(vm)

        scenario = launchFragmentInTestActivity(ModifyPostScreen(), bundle)
        modifyPostView = TestModifyPostScreenModule.modifyPostView
    }
}