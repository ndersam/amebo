package com.amebo.amebo.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.restart
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.amebo.amebo.di.Injectable
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.core.domain.AnonymousAccount
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

abstract class BaseFragment(layoutRes: Int) : Fragment(layoutRes), Injectable, HasAndroidInjector {

    @Inject
    lateinit var routerFactory: RouterFactory

    lateinit var router: Router

    @Inject
    lateinit var pref: Pref

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    inline fun <reified VM : ViewModel> viewModels(): Lazy<VM> {
        return lazy {
            VM::class.java.let { clazz ->
                ViewModelProvider(this, viewModelFactory).get(clazz)
            }
        }
    }

    inline fun <reified VM : ViewModel> activityViewModels(): Lazy<VM> {
        return lazy {
            VM::class.java.let { clazz ->
                ViewModelProvider(requireActivity(), viewModelFactory).get(clazz)
            }
        }
    }

    inline fun <reified T : ViewModel> createViewModel(owner: ViewModelStoreOwner): T =
        T::class.java.let { clazz ->
            ViewModelProvider(owner, viewModelFactory).get(clazz)
        }


    val userManagementViewModel by activityViewModels<UserManagementViewModel>()

    init {
        lifecycleScope.launchWhenStarted {
            userManagementViewModel
                .userLoggedOutEvent
                .observe(viewLifecycleOwner,
                    EventObserver { onUserLoggedOutSomeWhereElse() }
                )
        }
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        router = routerFactory.create(this)
        return view
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (this is AuthenticationRequired && pref.isLoggedOut) {
            router.back()
        } else {
            onViewCreated(savedInstanceState)
        }
    }

    abstract fun onViewCreated(savedInstanceState: Bundle?)


    private fun onUserLoggedOutSomeWhereElse() {
        val username = pref.userName ?: return
        userManagementViewModel.setUser(AnonymousAccount)
        Toast.makeText(requireContext(), "$username logged out. Sign in again", Toast.LENGTH_SHORT)
            .show()
        requireActivity().restart()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        // https://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE")
        super.onSaveInstanceState(outState)
    }


    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
