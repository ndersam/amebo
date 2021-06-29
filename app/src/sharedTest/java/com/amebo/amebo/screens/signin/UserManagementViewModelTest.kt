package com.amebo.amebo.screens.signin

import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Resource
import com.amebo.amebo.di.TestNairalandProvider
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.suite.MainCoroutineRule
import com.amebo.amebo.suite.TestPref
import com.amebo.core.Nairaland
import com.amebo.core.domain.NairalandSessionObservable
import com.amebo.core.domain.RealUserAccount
import com.amebo.core.domain.User
import com.amebo.core.domain.UserAccount
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
class UserManagementViewModelTest {
    lateinit var viewModel: UserManagementViewModel
    lateinit var observable: NairalandSessionObservable
    lateinit var pref: TestPref
    lateinit var nairaland: Nairaland

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun before() {
        nairaland = TestNairalandProvider.newNairalandInstance()
        pref = TestPref()
        observable = mock()
        viewModel = UserManagementViewModel(
            observable,
            nairaland,
            pref,
            ApplicationProvider.getApplicationContext()
        )
    }

    @Test
    fun testLogin(): Unit = runBlocking {
        whenever(nairaland.auth.login(any(), any())).thenReturn(Ok(Unit))
        val observer = mock<Observer<Event<Resource<Unit>>>>()
        viewModel.loginEvent.observeForever(observer)

        viewModel.login("string", "string")

        verify(nairaland, times(1)).reset()

        val captor = argumentCaptor<Event<Resource<Unit>>>()
        verify(observer, times(2)).onChanged(captor.capture())
        assertThat(captor.firstValue.peekContent()).isInstanceOf(Resource.Loading::class.java)
        assertThat(captor.secondValue.peekContent()).isInstanceOf(Resource.Success::class.java)
    }

    @Test
    fun testUser(): Unit = runBlocking {
        val observer = mock<Observer<Event<Pair<RealUserAccount, Boolean>>>>()
        viewModel.removeUserAccountEvent.observeForever(observer)

        viewModel.removeUser(RealUserAccount(User("random"), true), true)

        verify(nairaland.sources.accounts, times(1)).removeUser(any())
        verify(nairaland, times(1)).reset()
        verify(observer, times(1)).onChanged(any())
    }

    @Test
    fun testLoadAccounts(): Unit = runBlocking {
        whenever(nairaland.sources.accounts.loadAccountUsers()).thenReturn(listOf(mock(), mock()))
        val observer = mock<Observer<Event<List<UserAccount>>>>()
        viewModel.accountListEvent.observeForever(observer)

        viewModel.loadAccounts()

        verify(observer, times(1)).onChanged(any())
    }

    @Test
    fun testUserLoggedOut() = runBlocking {
        val observer = mock<Observer<Event<Unit>>>()
        viewModel.userLoggedOutEvent.observeForever(observer)

        verify(observer, times(1)).onChanged(any())
    }
}