package com.amebo.amebo.suite

import android.app.Application
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.amebo.amebo.R
import com.amebo.amebo.TestApp
import com.amebo.amebo.application.TestFragmentActivity
import com.amebo.amebo.di.TestAppInjector
import com.amebo.amebo.di.TestViewModelModule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

fun ActivityScenario<TestFragmentActivity>.setFragment(
    fragment: Fragment,
    bundle: Bundle? = null
): ActivityScenario<TestFragmentActivity> = onActivity { it.setFragment(fragment, bundle) }


inline fun <reified T : Fragment> ActivityScenario<TestFragmentActivity>.onFragment(crossinline listener: (T) -> Unit) {
    onActivity { listener(it.fragment as T) }
}

inline fun <reified T : ViewModel> setupViewModelFactory(viewModel: T) {
    whenever(TestViewModelModule.viewModelFactory.create(T::class.java)).thenReturn(viewModel)
}

fun ActivityScenario<*>.finish() {
    onActivity { it.finish() }
}

fun launchTestFragment(
    @LayoutRes layoutResId: Int,
    onViewCreatedCallback: ((Fragment, View) -> Unit)? = null
): FragmentScenario<TestFragment> {
    val callback = object : TestFragment.OnViewCreatedCallback() {
        override fun onViewCreated(fragment: Fragment, view: View) {
            onViewCreatedCallback?.invoke(fragment, view)
        }
    }
    return FragmentScenario.launch(
        TestFragment::class.java,
        TestFragment.newBundle(layoutResId, callback),
        R.style.AppTheme_Default,
        null
    )
}

fun launchFragmentInTestActivity(
    fragment: Fragment,
    bundle: Bundle? = null
): ActivityScenario<TestFragmentActivity> {
    val scenario = launchActivity<TestFragmentActivity>()
    scenario.setFragment(fragment, bundle)
    return scenario
}

fun launchTestFragmentInTestActivity(
    @LayoutRes layoutResId: Int,
    onViewCreatedCallback: ((Fragment, View) -> Unit)? = null
): ActivityScenario<TestFragmentActivity> {
    val callback = object : TestFragment.OnViewCreatedCallback() {
        override fun onViewCreated(fragment: Fragment, view: View) {
            onViewCreatedCallback?.invoke(fragment, view)
        }
    }
    val scenario = launchActivity<TestFragmentActivity>()
    scenario.setFragment(TestFragment(), TestFragment.newBundle(layoutResId, callback))

    return scenario
}

fun getString(@StringRes stringResId: Int, vararg args: Any) =
    ApplicationProvider.getApplicationContext<Application>().getString(stringResId, args)


fun injectIntoTestApp() {
    val app = ApplicationProvider.getApplicationContext<TestApp>()
    TestAppInjector.inject(app)
}

fun newMenuItem(@IdRes itemId: Int): MenuItem {
    val item = mock<MenuItem>()
    whenever(item.itemId).thenReturn(itemId)
    return item
}

fun ActivityScenario<TestFragmentActivity>.assertFragmentResultSet(key: String): Bundle?{
    setFragment(ResultListenerFragment(), bundleOf(ResultListenerFragment.REQUEST_KEY to key))
    var bundle : Bundle? = null
    onFragment<ResultListenerFragment> {
        assertThat(it.resultBundle).isNotNull()
        bundle = it.resultBundle
    }
    return bundle
}

fun testLifecycle(): Lifecycle {
    val lifecycle = LifecycleRegistry(mock())
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    return lifecycle
}