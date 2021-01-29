package com.amebo.amebo.suite

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize

class TestFragment : Fragment() {

    private val layoutResId get() = requireArguments().getInt("layoutResId")
    private val callback get() = requireArguments().getParcelable<OnViewCreatedCallback>("callback")

    companion object {
        fun newBundle(@LayoutRes layoutResId: Int, viewCallback: OnViewCreatedCallback? = null) =
            bundleOf("layoutResId" to layoutResId, "callback" to viewCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        callback?.onViewCreated(this, view)
    }

    @Parcelize
    open class OnViewCreatedCallback : Parcelable {
        open fun onViewCreated(fragment: Fragment, view: View) {

        }
    }
}