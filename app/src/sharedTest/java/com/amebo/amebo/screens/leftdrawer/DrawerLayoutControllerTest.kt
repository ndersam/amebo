package com.amebo.amebo.screens.leftdrawer

import android.graphics.Bitmap
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.routing.TabItem
import com.amebo.amebo.di.TestRouterModule
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.suite.*
import com.amebo.core.domain.DisplayPhoto
import com.amebo.core.domain.DisplayPhotoBitmap
import com.amebo.core.domain.DisplayPhotoUrl
import com.amebo.core.domain.Session
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawerLayoutControllerTest {
    private lateinit var viewModel: UserManagementViewModel
    private lateinit var scenario: FragmentScenario<TestFragment>
    private lateinit var controller: DrawerLayoutController
    private lateinit var pref: TestPref
    private lateinit var view: DrawerLayoutView
    private lateinit var sessionEvent: MutableLiveData<Session>
    private lateinit var displayPhotoEvent: MutableLiveData<Event<Resource<DisplayPhoto>>>


    @Test
    fun onInit_initializationDoneProperly() {
        initialize()

        verify(sessionEvent, times(1)).observe(any(), any())
        verify(displayPhotoEvent, times(1)).observe(any(), any())
        verify(viewModel, times(1)).loadDisplayPhotoOrAvatar()
        verify(view, times(1)).listener = controller
    }

    @Test
    fun onDisplayPhotoUrlSet_viewUpdated() {
        initialize(false)

        val url = "link"
        displayPhotoEvent.value = Event(Resource.Success(DisplayPhotoUrl(url = url)))

        val captor = argumentCaptor<String>()
        verify(view, times(1)).setDisplayPhoto(captor.capture())
        assertThat(captor.firstValue).isEqualTo(url)
    }

    @Test
    fun onDisplayPhotoBitmapFromAvatarSet_viewUpdated() {
        initialize(false)

        val bitmap = mock<Bitmap>()
        displayPhotoEvent.value = Event(Resource.Success(DisplayPhotoBitmap(bitmap = bitmap)))

        val captor = argumentCaptor<Bitmap>()
        verify(view, times(1)).setDisplayPhoto(captor.capture())
        assertThat(captor.firstValue).isEqualTo(bitmap)
    }

    @Test
    fun onSessionUpdated_viewUpdated() {
        initialize(false)

        val session = Session()
        sessionEvent.value = session

        val captor = argumentCaptor<Session>()
        verify(view, times(1)).setNotification(captor.capture())
        assertThat(captor.firstValue).isEqualTo(session)
    }


    @Test
    fun onDrawerHeaderClicked_showAccountList() {
        initialize()

        controller.onHeaderClicked()

        verify(TestRouterModule.router, times(1)).toAccountList()
    }

    @Test
    fun onDisplayPhotoClicked_toEditProfileIfLoggedInOnly() {
        initialize(isLoggedIn = true)

        controller.onImageClicked()

        verify(TestRouterModule.router, times(1)).toEditProfile()
    }

    @Test
    fun onDisplayPhotoClicked_noRoutingToEditProfile_whenSignedOut() {
        initialize(isLoggedIn = false)

        controller.onImageClicked()

        verify(TestRouterModule.router, times(0)).toEditProfile()
    }

    @Test
    fun onItemSelected_routeToDrawerItem() {
        initialize()

        val item = TabItem.RecentPosts
        controller.onItemSelected(item)

        val captor = argumentCaptor<TabItem>()
        verify(TestRouterModule.router, times(1)).toTabItem(captor.capture())
        assertThat(captor.firstValue).isEqualTo(item)
        assertThat(captor.secondValue).isEqualTo(TabItem.Topics)
    }

    @Test
    fun onHandleBackPress_reactAppropriately() {
        val externalBackPressHandled = arrayOf(
            false,
            false,
            true,
            true
        )
        val routerBackPressHandled = arrayOf(
            false,
            true,
            false,
            true
        )

        for (i in 0 until 4) {
            initialize()
//            controller.onBackPress = { externalBackPressHandled[i] }
            whenever(TestRouterModule.router.back()).thenReturn(routerBackPressHandled[i])


//            assertThat(controller.handleBackPress())
//                .isEqualTo(!externalBackPressHandled[i] && !routerBackPressHandled[i])

        }
    }

    @Test
    fun onSettingsClicked_routeToSettings() {
        initialize()

        controller.onSettingsClicked()

        verify(TestRouterModule.router, times(1)).toSettings()
    }


    private fun initialize(useMocks: Boolean = true, isLoggedIn: Boolean = false) {
        injectIntoTestApp()
        viewModel = mock()
        if (useMocks) {
            sessionEvent = mock()
            displayPhotoEvent = mock()
        } else {
            sessionEvent = MutableLiveData()
            displayPhotoEvent = MutableLiveData()
        }
        view = mock()
        pref = TestPref(isLoggedIn = isLoggedIn)

        whenever(viewModel.sessionEvent).thenReturn(sessionEvent)
        whenever(viewModel.displayPhotoEvent).thenReturn(displayPhotoEvent)
        setupViewModelFactory(viewModel)

        scenario = launchTestFragment(R.layout.activity_main) { fragment, _ ->
//            controller = DrawerLayoutController(
//                fragment = fragment,
//                pref = pref,
//                router = TestRouterModule.router,
//                userManagementViewModel = viewModel,
//                view = this.view
//            )
        }
    }


}