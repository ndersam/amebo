package com.amebo.amebo.screens.search

import android.view.View
import android.view.View.FIND_VIEWS_WITH_TEXT
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amebo.amebo.R
import com.amebo.amebo.databinding.SearchScreenBinding
import com.amebo.amebo.suite.TestFragment
import com.amebo.amebo.suite.getString
import com.amebo.amebo.suite.launchTestFragment
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchViewTest {

    private lateinit var searchView: SearchView
    private lateinit var scenario: FragmentScenario<TestFragment>
    private lateinit var listener: SearchView.Listener
    private lateinit var binding: SearchScreenBinding

    @Before
    fun before() {
        listener = mock()
        scenario = launchTestFragment(R.layout.search_screen) { fragment, view ->
            binding = SearchScreenBinding.bind(view)
            searchView = SearchView(fragment, binding, listener)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @Test
    fun onSearchBoxTextChange_suggestionsAreRequested() {
        val text = "text"
        binding.searchBox.setText(text)
        verify(listener, times(1)).findSearchSuggestions(text)

        // recyclerView content
        assertEquals(1, binding.recyclerView.adapter!!.itemCount)
        val views = arrayListOf<View>()
        onView(withId(R.id.recyclerView)).perform(click())
        binding.recyclerView.findViewHolderForAdapterPosition(0)!!.itemView.findViewsWithText(
            views,
            getString(R.string.results_for, text),
            FIND_VIEWS_WITH_TEXT
        )
        assertEquals(1, views.size)
    }
}