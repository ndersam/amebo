package com.amebo.amebo.screens.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amebo.amebo.BuildConfig.VERSION_CODE
import com.amebo.amebo.application.MainActivity
import com.amebo.amebo.common.Pref
import com.amebo.core.Nairaland
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val nairaland: Nairaland,
    private val pref: Pref
) : ViewModel() {

    fun initializeDatabase(activity: AppCompatActivity) = viewModelScope.launch {
        val updatePref = pref.isFirstLaunch || pref.isMigrationNeeded
        if (pref.isFirstLaunch) {
            nairaland.sources.initialize()
            pref.initialize()
        } else if (pref.isMigrationNeeded) {
            pref.migrate()
            nairaland.sources.initialize()
            nairaland.sources.migrate(activity, pref.userName)
        }

        if (updatePref) {
            pref.timeFirstLaunch =
                System.currentTimeMillis() // new preference; wasn't present in previous version
            pref.isFirstLaunch = false
        }
        // In next build check if VERSION_CODE <= 18
        pref.version = VERSION_CODE

        pref.numOfTimesLaunchedApp++
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

}