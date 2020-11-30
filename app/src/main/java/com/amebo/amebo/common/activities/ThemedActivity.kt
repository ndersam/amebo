package com.amebo.amebo.common.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.asTheme
import com.amebo.amebo.common.extensions.restart
import javax.inject.Inject

abstract class ThemedActivity : AppCompatActivity(), Pref.Observer {
    private val themeObservable = Pref.Observable()
    private var pendingThemeChange = false
    private var hasResumed = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    inline fun <reified T : ViewModel> createViewModel(owner: ViewModelStoreOwner): T =
        T::class.java.let { clazz ->
            ViewModelProvider(owner, viewModelFactory).get(clazz)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        asTheme().apply()
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        themeObservable.subscribe(this, this, R.string.key_current_theme, R.string.key_dark_mode)
    }

    override fun onResume() {
        super.onResume()
        hasResumed = true
        if (pendingThemeChange) {
            pendingThemeChange = false
            restart()
        }
    }

    override fun onPause() {
        super.onPause()
        hasResumed = false
    }

    override fun onDestroy() {
        super.onDestroy()
        themeObservable.unsubscribe(this)
    }

    override fun onPreferenceChanged(key: Int, contextChanged: Boolean) {
        if (key == R.string.key_current_theme || key == R.string.key_dark_mode) {
            if (hasResumed) {
                restart()
            } else {
                pendingThemeChange = true
            }
        }
    }

}