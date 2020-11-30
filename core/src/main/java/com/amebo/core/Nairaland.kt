package com.amebo.core

import android.content.Context
import com.amebo.core.auth.AuthService
import com.amebo.core.converter.DocConverter
import com.amebo.core.data.datasources.DataSources
import net.danlew.android.joda.JodaTimeAndroid
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class Nairaland @Inject internal constructor(
    private val sourcesProvider: Provider<DataSources>,
    private val authServiceProvider: Provider<AuthService>
) {
    private var _sources: DataSources = sourcesProvider.get()
    private var _auth: AuthService = authServiceProvider.get()

    val sources: DataSources get() = _sources
    val auth: AuthService get() = _auth

    fun reset() {
        _sources = sourcesProvider.get()
        _auth = authServiceProvider.get()
    }

    companion object {
        fun init(context: Context) {
            JodaTimeAndroid.init(context)
            DocConverter.initialize(context)
        }
    }
}

