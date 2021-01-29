package com.amebo.amebo.application

import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

@RestrictTo(RestrictTo.Scope.TESTS)
class TestFragmentActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    lateinit var fragment: Fragment

    fun setFragment(fragment: Fragment, bundle: Bundle? = null) {
        fragment.arguments = bundle
        this.fragment = fragment
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, fragment, "FRAG")
            .commitNowAllowingStateLoss()
    }
}
