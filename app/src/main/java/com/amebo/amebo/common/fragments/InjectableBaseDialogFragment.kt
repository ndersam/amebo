package com.amebo.amebo.common.fragments

import android.graphics.Point
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.amebo.amebo.di.Injectable
import javax.inject.Inject

abstract class InjectableBaseDialogFragment(@LayoutRes layoutRes: Int) : DialogFragment(layoutRes),
    Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var pref: Pref

    @Inject
    lateinit var routerFactory: RouterFactory

    lateinit var router: Router

    open val resizeView: Boolean = true

    inline fun <reified T : ViewModel> createViewModel(owner: ViewModelStoreOwner): T =
        T::class.java.let { clazz ->
            ViewModelProvider(owner, viewModelFactory).get(clazz)
        }

    inline fun <reified T : ViewModel> viewModels() = lazy { createViewModel<T>(this) }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        router = routerFactory.create(this)
        return view
    }

    override fun onResume() {
        resize(if (resizeView) 0.85f else 1.0f)
        super.onResume()
    }

    private fun resize(percent: Float) {
        val window: Window? = dialog?.window ?: return
        val size = Point()
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout((size.x * percent).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
    }
}