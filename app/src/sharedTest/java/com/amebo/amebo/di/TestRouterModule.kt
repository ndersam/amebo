package com.amebo.amebo.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
class TestRouterModule {
    companion object {
        lateinit var router: Router
            private set
    }


    @Provides
    fun provideRouterFactory(): RouterFactory = TestRouterFactory


    private object TestRouterFactory : RouterFactory {
        override fun create(activity: FragmentActivity): Router {
            router = mock()
            return router
        }

        override fun create(fragment: Fragment): Router {
            router = mock()
            return router
        }

    }
}