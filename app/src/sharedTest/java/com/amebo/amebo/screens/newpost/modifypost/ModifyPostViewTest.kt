package com.amebo.amebo.screens.newpost.modifypost

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.common.widgets.StateLayout
import com.amebo.amebo.data.TestData
import com.amebo.amebo.databinding.NewPostScreenBinding
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.screens.newpost.IFormView
import com.amebo.amebo.screens.newpost.ModifyPostFormData
import com.amebo.amebo.suite.*
import com.amebo.core.domain.Topic
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModifyPostViewTest {
    lateinit var view: ModifyPostView
    lateinit var pref: Pref
    lateinit var topic: Topic
    lateinit var listener: IFormView.Listener

    @Before
    fun before() {
        pref = mock()
        whenever(pref.allVisibleEditActions()).thenReturn(emptyList())

        listener = mock()
        topic = TestData.topics.first()
        launchTestFragmentInTestActivity(R.layout.new_post_screen) { fragment, view ->
            val binding = NewPostScreenBinding.bind(view)
            this.view = ModifyPostView(fragment, pref, binding, topic, listener)
        }
    }

    @Test
    fun onCreateView_viewInitializedCorrectly() {
        onView(withId(R.id.edit_title)).check(matches(isDisabled()))
        onView(withId(R.id.edit_title)).check(matches(withText(topic.title)))
        onView(withId(R.id.followTopic)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.modify_post)))
    }

    @Test
    fun onFormDataSuccess_viewUpdatedAppropriately() {
        arrayOf(
            ModifyPostFormData("body", "title", titleIsEditable = true),
            ModifyPostFormData("body2", "title2", titleIsEditable = false)
        ).forEach { data ->
            view.onFormSuccess(Resource.Success(data))
            val isEnabled = if (data.titleIsEditable) isEnabled() else isDisabled()
            onView(withId(R.id.edit_title)).check(matches(isEnabled))
            onView(withId(R.id.edit_title)).check(matches(withText(data.title)))
            onView(withId(R.id.edit_message)).check(matches(withText(data.body)))
            onView(withId(R.id.stateLayout)).check(matches(withState(StateLayout.State.Content)))
        }
    }



    @Test
    fun stateLayoutSetupCorrectly(){
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

}