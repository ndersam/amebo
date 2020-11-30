package com.amebo.amebo.screens.feed

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.amebo.core.Nairaland
import javax.inject.Inject
import javax.inject.Provider

class FeedWorkerFactory @Inject constructor(private val provider: Provider<Nairaland>) :
    WorkerFactory() {
    private val nairaland by lazy { provider.get() }

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            FeedWorker::class.java.name -> FeedWorker(
                appContext,
                workerParameters,
                nairaland
            )
            else -> null
        }
    }

}