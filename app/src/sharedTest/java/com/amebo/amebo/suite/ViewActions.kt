package com.amebo.amebo.suite

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.amebo.amebo.common.widgets.OuchView
import com.amebo.amebo.common.widgets.StateLayout
import org.hamcrest.Matcher

fun navigationClick() = object : ViewAction {
    override fun getDescription(): String {
        return "yada yada"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(Toolbar::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        val toolbar = view as Toolbar
        toolbar.children.first().performClick() // first view is the navigation image button
    }
}

fun makeVisible() = object : ViewAction {
    override fun getDescription(): String {
        return "Make visible"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(View::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        view?.isVisible = true
    }
}


fun pullRefresh() = object : ViewAction {
    override fun getDescription(): String {
        return "yadayada"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(SwipeRefreshLayout::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        val swipeRefreshLayout = view as SwipeRefreshLayout
        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true
        }
    }
}

fun ouchViewButtonClick() = object : ViewAction {
    override fun getDescription(): String {
        return "yadayada"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(OuchView::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        val ouchView = view as OuchView
        ouchView.performButtonClick()
    }
}

fun stateLayoutAction(callback: (StateLayout) -> Unit) = object : ViewAction {
    override fun getDescription(): String {
        return "StateLayout action"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(StateLayout::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        callback(view as StateLayout)
    }
}

fun onViewHolderItem(matcher: Matcher<View>, action: ViewAction) = object : ViewAction {
    override fun getDescription(): String {
        return action.description
    }

    override fun getConstraints(): Matcher<View> {
        return matcher
    }

    override fun perform(uiController: UiController?, view: View?) {
        action.perform(uiController, view)
    }
}

fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View?) {
            uiController?.loopMainThreadForAtLeast(delay)
        }

        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "wait for " + delay + "milliseconds"
        }
    }
}

fun customViewAction(listener: (View)-> Unit) = object : ViewAction {
    override fun getDescription(): String {
        return "Perform custom action"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(Toolbar::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
       listener(view!!)
    }
}