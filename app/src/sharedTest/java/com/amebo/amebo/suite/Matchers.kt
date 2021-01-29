package com.amebo.amebo.suite

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.util.Preconditions.checkState
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.amebo.amebo.common.widgets.StateLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


fun snackBarText() = withId(com.google.android.material.R.id.snackbar_text)

fun withState(state: StateLayout.State): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            return (view as StateLayout).state == state
        }

        override fun describeTo(description: Description) {
            description.appendText("StateLayout should have $state state")
        }
    }
}

fun isDisabled(): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            return !view.isEnabled
        }

        override fun describeTo(description: Description) {
            description.appendText("View should be disabled")
        }
    }
}

fun isGone(): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            return view.isGone
        }

        override fun describeTo(description: Description) {
            description.appendText("View should be Gone")
        }
    }
}

fun isRefreshing() = isRefreshing(true)
fun isNotRefreshing() = isRefreshing(false)

fun isRefreshing(isRefreshing: Boolean): Matcher<View> {
    return object : BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {
        override fun matchesSafely(view: SwipeRefreshLayout): Boolean {
            return view.isRefreshing == isRefreshing
        }

        override fun describeTo(description: Description) {
            description.appendText("SwipeRefreshLayout should be ${if (isRefreshing) "refreshing" else " not refreshing"}")
        }
    }
}

fun withToolbarText(title: String, subtitle: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            val view = view as Toolbar
            return view.title == title && view.subtitle == subtitle
        }

        override fun describeTo(description: Description) {
            description.appendText("Toolbar title should be ${title} and subtitle should be ${subtitle}")
        }
    }
}

fun withToolbarSubtitle(subtitle: String = ""): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            val view = view as Toolbar
            return view.subtitle == subtitle
        }

        override fun describeTo(description: Description) {
            description.appendText("Toolbar subtitle should be ${subtitle}")
        }
    }
}

fun withToolbarTitle(title: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            val toolbar = view as Toolbar
            return toolbar.title == title
        }

        override fun describeTo(description: Description) {
            description.appendText("Toolbar title should be $title")
        }
    }
}

fun withCollapsingToolbarTitle(title: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            val toolbar = view as CollapsingToolbarLayout
            return toolbar.title == title
        }

        override fun describeTo(description: Description) {
            description.appendText("CollapsingToolbarLayout title should be $title")
        }
    }
}

fun withToolbarTitle(titleRes: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            val toolbar = view as Toolbar
            return toolbar.title == toolbar.context.getString(titleRes)
        }

        override fun describeTo(description: Description) {
            description.appendText("Toolbar title Res should be $titleRes")
        }
    }
}

fun withItemCount(size: Int): Matcher<View> {
    var itemCount: Int? = null
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            val rv = view as RecyclerView
            itemCount = rv.adapter!!.itemCount
            return rv.adapter!!.itemCount == size
        }

        override fun describeTo(description: Description) {
            description.appendText("Recycler view size should be $size")
            when (val got = itemCount) {
                is Int -> {
                    description.appendText(" but got $got")
                }
            }
        }
    }
}

fun withHolderPosition(position: Int): Matcher<RecyclerView.ViewHolder> {
    var currentIndex = 0
    return object : TypeSafeMatcher<RecyclerView.ViewHolder>() {
        public override fun matchesSafely(view: RecyclerView.ViewHolder): Boolean {
            return currentIndex++ == position
        }

        override fun describeTo(description: Description) {
            description.appendText("Unable to find Recycler.ViewHolder at $position")
        }
    }
}


fun withIndexOfType(
    matcher: Matcher<View?>,
    index: Int
): Matcher<View?> {
    return object : TypeSafeMatcher<View?>() {
        var currentIndex = 0
        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        override fun matchesSafely(view: View?): Boolean {
            if (matcher.matches(view)) {
                return currentIndex++ == index
            }
            return false
        }
    }
}


fun onViews(first: Matcher<View>, vararg other: Matcher<View>): List<ViewInteraction> {
    return (arrayOf(first) + other).map { onView(first) }
}

fun List<ViewInteraction>.check(matcher: Matcher<View>): List<ViewInteraction> {
    return map { it.check(ViewAssertions.matches(matcher)) }
}

fun List<ViewInteraction>.check(assertion: ViewAssertion): List<ViewInteraction> {
    return map { it.check(assertion) }
}

fun withCustomConstraints(
    action: ViewAction,
    constraints: Matcher<View>
): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return constraints
        }

        override fun getDescription(): String {
            return action.description
        }

        override fun perform(uiController: UiController?, view: View?) {
            action.perform(uiController, view)
        }
    }
}

fun withNavigationIcon(@DrawableRes resourceId: Int) =
    object : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has navigation icon $resourceId")
        }

        override fun matchesSafely(item: Toolbar): Boolean {
            return sameBitmap(item.context, item.navigationIcon, resourceId)
        }

    }

fun withImageDrawable(resourceId: Int): Matcher<View?>? {
    return object : BoundedMatcher<View?, ImageView>(ImageView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has image drawable resource $resourceId")
        }

        override fun matchesSafely(imageView: ImageView): Boolean {
            return sameBitmap(imageView.context, imageView.drawable, resourceId)
        }
    }
}

private fun sameBitmap(context: Context, drawable: Drawable?, resourceId: Int): Boolean {
    var drawable: Drawable? = drawable
    var otherDrawable = context.resources.getDrawable(resourceId)
    if (drawable == null || otherDrawable == null) {
        return false
    }
    if (drawable is StateListDrawable && otherDrawable is StateListDrawable) {
        drawable = drawable.getCurrent()
        otherDrawable = otherDrawable.getCurrent()
    }
    if (drawable is BitmapDrawable) {
        val bitmap = drawable.bitmap
        val otherBitmap = (otherDrawable as BitmapDrawable).bitmap
        return bitmap.sameAs(otherBitmap)
    }
    return false
}

fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(
        RecyclerView::class.java
    ) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val viewHolder = view.findViewHolderForAdapterPosition(position)
                ?: // has no item on such position
                return false
            return itemMatcher.matches(viewHolder.itemView)
        }
    }
}

class RecyclerViewMatcher(private val recyclerId: Int) {
    fun atPosition(position: Int): Matcher<View> {
        return atPositionOnView(position, UNSPECIFIED)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var recycler: RecyclerView? = null
            var holder: RecyclerView.ViewHolder? = null
            override fun describeTo(description: Description) {
                checkState(resources != null, "resource should be init by matchesSafely()")
                if (recycler == null) {
                    description.appendText("RecyclerView with " + getResourceName(recyclerId))
                    return
                }
                if (holder == null) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                if (targetViewId == UNSPECIFIED) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                description.appendText(
                    String.format(
                        "in RecyclerView (%s) at position %s and with %s",
                        getResourceName(recyclerId),
                        position,
                        getResourceName(targetViewId)
                    )
                )
            }

            private fun getResourceName(id: Int): String {
                return try {
                    "R.id." + resources!!.getResourceEntryName(id)
                } catch (ex: Resources.NotFoundException) {
                    String.format("resource id %s - name not found", id)
                }
            }

            public override fun matchesSafely(view: View): Boolean {
                resources = view.resources
                recycler = view.rootView.findViewById(recyclerId)
                if (recycler == null) return false
                holder = recycler!!.findViewHolderForAdapterPosition(position)
                if (holder == null) return false
                return if (targetViewId == UNSPECIFIED) {
                    view === holder!!.itemView
                } else {
                    view === holder!!.itemView.findViewById<View>(targetViewId)
                }
            }
        }
    }

    companion object {
        const val UNSPECIFIED = -1
    }
}
//fun isKeyboardOpenedShellCheck(): Boolean {
//    val checkKeyboardCmd = "dumpsys input_method | grep mInputShown"
//
//    try {
//        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//            .executeShellCommand(checkKeyboardCmd).contains("mInputShown=true")
//    } catch (e: IOException) {
//        throw RuntimeException("Keyboard check failed", e)
//    }
//}