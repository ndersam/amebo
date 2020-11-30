package com.amebo.amebo.screens.mail.inbox

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.suite.injectIntoTestApp
import com.amebo.amebo.suite.launchFragmentInTestActivity
import com.amebo.amebo.suite.setupViewModelFactory
import com.amebo.core.domain.Session
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InboxScreenTest {

    lateinit var userManagementViewModel: UserManagementViewModel
    lateinit var viewModel: InboxScreenViewModel
    lateinit var scenario: ActivityScenario<TestFragmentActivity>

    lateinit var sessionEvent: MutableLiveData<Session>
    lateinit var dismissEvent: MutableLiveData<Event<Resource<Unit>>>
    lateinit var userLoggedOutEvent: MutableLiveData<Event<Unit>>

    @Before
    fun before() {
        injectIntoTestApp()
    }

    @Test
    fun onViewCreated_eventsSubscribed() {
        initialize()

        verify(dismissEvent).observe(any(), any())
        verify(sessionEvent).observe(any(), any())
    }

    @Test
    fun onDismissClicked_dismissalInitiated() {
        initialize()

        onView(withId(R.id.btnDismiss)).perform(click())
        verify(viewModel).dismiss()
    }

    private fun initialize(useMocks: Boolean = true) {
        if (useMocks) {
            dismissEvent = mock()
            sessionEvent = mock()
        } else {
            dismissEvent = MutableLiveData()
            sessionEvent = MutableLiveData()
        }
        userLoggedOutEvent = mock()
        userManagementViewModel = mock()
        viewModel = mock()
        setupViewModelFactory(userManagementViewModel)
        setupViewModelFactory(viewModel)
        whenever(viewModel.dismissMailEvent).thenReturn(dismissEvent)
        whenever(userManagementViewModel.sessionEvent).thenReturn(sessionEvent)
        whenever(userManagementViewModel.userLoggedOutEvent).thenReturn(userLoggedOutEvent)

        scenario = launchFragmentInTestActivity(InboxScreen())
    }
}

