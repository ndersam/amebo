package com.amebo.amebo.screens.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.android.AndroidInjection
import javax.inject.Inject


class SplashActivity : AppCompatActivity() {

    private val viewModel by lazy { createViewModel<SplashViewModel>(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        viewModel.initializeDatabase(this)
    }


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private inline fun <reified T : ViewModel> createViewModel(owner: ViewModelStoreOwner): T =
        T::class.java.let { clazz ->
            ViewModelProvider(owner, viewModelFactory).get(clazz)
        }
}
