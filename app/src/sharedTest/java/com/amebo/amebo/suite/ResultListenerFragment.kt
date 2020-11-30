package com.amebo.amebo.suite

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener

class ResultListenerFragment : Fragment() {
     var resultBundle: Bundle? = null

    private val requestKey get() = requireArguments().getString(REQUEST_KEY)!!

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(requestKey) { _, bundle ->
            resultBundle = bundle
        }
    }
}