package com.amebo.amebo.screens.newpost.newpost

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.widgets.StateLayout
import com.amebo.amebo.data.TestData
import com.amebo.amebo.databinding.NewPostScreenBinding
import com.amebo.amebo.screens.newpost.NewPostFormData
import com.amebo.amebo.suite.*
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.Topic
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewPostViewTest {

    lateinit var view: NewPostView
    lateinit var pref: Pref
    lateinit var topic: Topic
    lateinit var listener: NewPostView.Listener

    @Before
    fun before() {
        pref = mock()
        whenever(pref.allVisibleEditActions()).thenReturn(emptyList())

        listener = mock()
        topic = TestData.topics.first()
        launchTestFragmentInTestActivity(R.layout.new_post_screen) { fragment, v ->
            val binding = NewPostScreenBinding.bind(v)
            view = NewPostView(fragment, pref, binding, topic, listener)
        }
    }

//    @After
//    fun after() {
//        synchronized(this) {
//            (this as Object).wait()
//        }
//    }

    @Test
    fun onCreateView_initializedAppropriately() = runOnUIThread {
        val formDataArray = arrayOf(
            NewPostFormData(
                "",
                topic.title,
                true
            ),
            NewPostFormData(
                "yada yada yada",
                topic.title,
                true
            ),
            NewPostFormData(
                "yada ydata",
                topic.title,
                false
            )
        )
        formDataArray.forEach {
            view.onFormSuccess(Resource.Success(it))

            onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Content)))
            onView(withId(R.id.edit_title)).check(matches(isDisabled()))
            onView(withId(R.id.edit_title)).check(matches(withText(it.title)))
            onView(withId(R.id.followTopic)).check(matches(isDisplayed()))
            onView(withId(R.id.followTopic)).check(
                matches(
                    if (it.followTopic) isChecked() else isNotChecked()
                )
            )
            onView(withId(R.id.edit_message)).check(matches(withText(it.body)))
        }
    }

    @Test
    fun quotePost_onFormError_viewUpdatesAppropriately() = runOnUIThread {
        view.onFormError(Resource.Error(ErrorResponse.Network, null))
        onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Failure)))
        onView(withId(R.id.ouchView)).check(matches(isDisplayed()))
    }

    @Test
    fun quotePost_onFormLoading_viewUpdatesAppropriately() = runOnUIThread {
        view.onFormLoading(Resource.Loading(null))
        onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Progress)))
        onView(withId(R.id.progress)).check(matches(isDisplayed()))
    }

    @Test
    fun onTitleUpdated_setTitleCalled() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.content() })
        onView(withId(R.id.edit_title)).perform(replaceText("text"))
        verify(listener, times(1)).setPostTitle("text")
    }

    @Test
    fun onBodyUpdated_setBodyCalled() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.content() })
        onView(withId(R.id.edit_message)).perform(replaceText("text"))
        verify(listener, times(1)).setPostBody("text")
    }

    @Test
    fun onToolbarNavClicked_goBackCalled() {
        onView(withId(R.id.toolbar)).perform(navigationClick())
        verify(listener, times(1)).goBack()
    }

    @Test
    fun onRetryClicked_retryLastRequestCalled() {
        onView(withId(R.id.ouchView)).perform(ouchViewButtonClick())
        verify(listener, times(1)).retryLastRequest()
    }

    @Test
    fun stateLayoutSetupCorrectly() {
        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.content() })
        onView(withId(R.id.content)).check(matches(isDisplayed()))
        onViews(withId(R.id.progress), withId(R.id.ouchView)).check(matches(isGone()))

        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.loading() })
        onView(withId(R.id.progress)).check(matches(isDisplayed()))
        onViews(withId(R.id.content), withId(R.id.ouchView)).check(matches(isGone()))

        onView(withId(R.id.stateLayout)).perform(stateLayoutAction { it.failure() })
        onView(withId(R.id.ouchView)).check(matches(isDisplayed()))
        onViews(withId(R.id.content), withId(R.id.progress)).check(matches(isGone()))
    }

    @Test
    fun onSubmitMenuItemClicked_initiateSubmission() {
        onView(withContentDescription(R.string.submit)).perform(click())
        verify(listener).submit()
    }

    @Test
    fun onRedoMenuItemClicked_redo() {
        onView(withContentDescription(R.string.redo)).perform(click())
    }

    @Test
    fun onUndoMenuItemClicked_undo() {
        onView(withContentDescription(R.string.undo)).perform(click())
    }

    @Test
    fun onSubmissionLoadingEvent_IndefiniteSnackBar_shown() {
        runOnUIThread {
            view.onFormSuccess(Resource.Success(NewPostFormData()))
            view.onSubmissionLoading(Resource.Loading(null))
        }
    }

    private fun runOnUIThread(block: () -> Unit) {
        handler.post(block)
    }

    private val handler = Handler(Looper.getMainLooper())
}
