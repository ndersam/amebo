package com.amebo.amebo.screens.signin

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.di.TestRouterModule
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.suite.*
import com.amebo.core.domain.ErrorResponse
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

    lateinit var scenario: ActivityScenario<TestFragmentActivity>
    lateinit var fragment: SignInScreen
    lateinit var userManagementViewModel: UserManagementViewModel
    lateinit var loginEvent: MutableLiveData<Event<Resource<Unit>>>

    @Test
    fun onCreateView_viewsCorrectlyInitializedAndViewModelSetup() {
        initialize()

        onView(withId(R.id.username)).check(matches(isEnabled()))
        onView(withId(R.id.password)).check(matches(isEnabled()))
        onView(withId(R.id.btnSignIn)).check(matches(isDisabled()))
        onView(withId(R.id.progress)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        verify(loginEvent, times(1)).observe(any(), any())
    }

    @Test
    fun onInputChanged_buttonEnableStateChanges() {
        initialize()
        val data = arrayOf(
            "" to "",
            "username" to "",
            "" to "password",
            "username" to "password",
            "u" to "p",
            "  " to "  "
        )

        data.forEach {
            val (username, password) = it

            onView(withId(R.id.username)).perform(replaceText(username))
            onView(withId(R.id.password)).perform(replaceText(password))

            onView(withId(R.id.progress)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.btnSignIn)).check(
                matches(
                    if (username.isNotBlank() && password.isNotBlank()) isEnabled()
                    else isDisabled()
                )
            )
        }
    }

    @Test
    fun onBtnSignInClicked_signInInitiated() {
        initialize()
        val username = " username "
        val password = " password "

        onView(withId(R.id.username)).perform(replaceText(username))
        onView(withId(R.id.password)).perform(replaceText(password))
        onView(withId(R.id.btnSignIn)).perform(click())

        val userNameCaptor = argumentCaptor<String>()
        val passwordCaptor = argumentCaptor<String>()
        verify(userManagementViewModel, times(1)).login(
            userNameCaptor.capture(),
            passwordCaptor.capture()
        )
        assertThat(userNameCaptor.firstValue).isEqualTo(username.trim())
        assertThat(passwordCaptor.firstValue).isEqualTo(password.trim())
    }

    @Test
    fun onSigningIn_viewUpdatedAppropriately() {
        initialize(useLiveDataMocks = false)

        loginEvent.value = Event(Resource.Loading(null))

        onView(withId(R.id.username)).check(matches(isDisabled()))
        onView(withId(R.id.password)).check(matches(isDisabled()))
        onView(withId(R.id.btnSignIn)).check(matches(isDisabled()))
        onView(withId(R.id.progress)).check(matches(isDisplayed()))
    }

    @Test
    fun onSignInFailed_viewUpdatedAppropriately() {
        initialize(useLiveDataMocks = false)

        onView(withId(R.id.username)).perform(replaceText("username"))
        onView(withId(R.id.password)).perform(replaceText("password"))
        val cause = ErrorResponse.Login
        loginEvent.value = Event(Resource.Error(cause, null))

        onView(withId(R.id.username)).check(matches(isEnabled()))
        onView(withId(R.id.password)).check(matches(isEnabled()))
        onView(withId(R.id.btnSignIn)).check(matches(isEnabled()))
        onView(withId(R.id.progress)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun onSignInSuccess_viewUpdatedAppropriately() {
        initialize(useLiveDataMocks = false)

        loginEvent.value = Event(Resource.Success(Unit))

        onView(withId(R.id.progress)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        verify(TestRouterModule.router, times(1)).back()
    }

    @Test
    fun onBackPressed_routeToPreviousScreen() {
        initialize()

        onView(withId(R.id.toolbar)).perform(navigationClick())

        verify(TestRouterModule.router, times(1)).back()
    }

    private fun initialize(useLiveDataMocks: Boolean = true) {
        injectIntoTestApp()
        userManagementViewModel = mock()
        setupViewModelFactory(userManagementViewModel)

        loginEvent = if (useLiveDataMocks) {
            mock()
        } else {
            MutableLiveData()
        }
        whenever(userManagementViewModel.loginEvent).thenReturn(loginEvent)

        fragment = SignInScreen()
        scenario = launchFragmentInTestActivity(fragment)
    }
}