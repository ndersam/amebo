package com.amebo.amebo.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.amebo.amebo.di.Injectable
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

abstract class InjectablePaddedBottomSheetFragment : PaddedBottomSheetFragment(),
    HasAndroidInjector, Injectable {

    @Inject
    lateinit var pref: Pref

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var routerFactory: RouterFactory

    lateinit var router: Router

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    inline fun <reified T : ViewModel> createViewModel(owner: ViewModelStoreOwner): T =
        T::class.java.let { clazz ->
            ViewModelProvider(owner, viewModelFactory).get(clazz)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        router = routerFactory.create(this)
        return view
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}