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
        var router: Router = mock()
            private set

        private fun reset() {
            router = mock()
        }
    }


    @Provides
    fun provideRouterFactory(): RouterFactory = TestRouterFactory


    private object TestRouterFactory : RouterFactory {
        override fun create(activity: FragmentActivity): Router {
            reset()
            return router
        }

        override fun create(fragment: Fragment): Router {
            reset()
            return router
        }

    }
}