//package com.amebo.amebo.screens.topiclist
//
//import androidx.lifecycle.lifecycleScope
//import androidx.test.core.app.ActivityScenario
//import androidx.test.core.app.launchActivity
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions.*
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.amebo.amebo.R
//import com.amebo.amebo.application.TestFragmentActivity
//import com.amebo.amebo.databinding.DialogTopiclistPageSelectionBinding
//import com.amebo.amebo.suite.TestFragment
//import com.amebo.amebo.suite.isDisabled
//import com.amebo.core.domain.*
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class TopicListPageSelectionDialogViewDelegateTest {
//
//    private lateinit var scenario: ActivityScenario<TestFragmentActivity>
//    private lateinit var fragment: TestFragment
//    private lateinit var delegate: TopicListPageSelectionDialogView
//    private lateinit var binding: DialogTopiclistPageSelectionBinding
//
//    @Before
//    fun before() {
//        scenario = launchActivity()
//        fragment = TestFragment()
//        fragment.lifecycleScope.launchWhenResumed {
//            binding = DialogTopiclistPageSelectionBinding.bind(fragment.requireView())
////            delegate = TopicListPageSelectionDialogView(binding)
//        }
//        scenario.onActivity {
//            it.setFragment(
//                fragment,
//                TestFragment.newBundle(R.layout.dialog_topiclist_page_selection)
//            )
//        }
//    }
//
//
//    @Test
//    fun featuredTopicsListMetaInformationDisplayedCorrectly() {
//        val current = 1
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//
//        onView(withId(R.id.topicList)).check(matches(withText(R.string.featured)))
////        onView(withId(R.id.layoutSort)).check(matches(withEffectiveVisibility(Visibility.GONE)))
//        onView(withId(R.id.btnVisitPage)).check(matches(isEnabled()))
//        onView(withId(R.id.editPage)).check(matches(withText(current.toString())))
//        onView(withId(R.id.btnPrevPage)).check(matches(isDisabled()))
//        onView(withId(R.id.btnNextPage)).check(matches(isEnabled()))
//    }
//
//    @Test
//    fun realBoardMetaInformationDisplayedCorrectly() {
//        val current = 1
//        val last = 5
//        val board = Board("Politics", "politics")
//        val sort = TopicListSorts.UPDATED
//        val sortList = TopicListSorts.BoardSorts
//        val dataPage = BoardsDataPage(
//            emptyList(),
//            current,
//            last,
//            emptyList(),
//            "",
//            0,
//            0,
//            false,
//            emptyList(),
//            ""
//        )
//        delegate.setBaseTopicListData(board, dataPage)
//        delegate.setPageInformation(current, last)
//
//        onView(withId(R.id.topicList)).check(matches(withText(board.name)))
////        onView(withId(R.id.layoutSort)).check(matches(isDisplayed()))
////        onView(withId(R.id.boardSort)).check(matches(isDisplayed()))
////        onView(withId(R.id.followedBoardSort)).check(matches(withEffectiveVisibility(Visibility.GONE)))
////        onView(withId(R.id.boardSort)).check(matches(hasChildCount(sortList.size)))
////        binding.boardSort.children.forEachIndexed { index, view ->
////            val btn = view as RadioButton
////            val thisSort = sortList[index]
////            assertEquals(btn.text.toString(), thisSort.name)
////            assertEquals(btn.isChecked, thisSort == sort)
////        }
//
//        onView(withId(R.id.btnVisitPage)).check(matches(isEnabled()))
//        onView(withId(R.id.editPage)).check(matches(withText(current.toString())))
//        onView(withId(R.id.btnPrevPage)).check(matches(isDisabled()))
//        onView(withId(R.id.btnNextPage)).check(matches(isEnabled()))
//    }
//
//    @Test
//    fun followedBoardMetaInformationDisplayedCorrectly() {
//        val current = 1
//        val last = 5
//        val sort = TopicListSorts.CREATION
//        val sortList = TopicListSorts.FollowedBoardsSorts
//        delegate.setBaseTopicListData(
//            FollowedBoards,
//            FollowedBoardsDataPage(emptyList(), current, last, mutableListOf())
//        )
//        delegate.setPageInformation(current, last)
//
//        onView(withId(R.id.topicList)).check(matches(withText(R.string.followed_boards)))
////        onView(withId(R.id.layoutSort)).check(matches(isDisplayed()))
////        onView(withId(R.id.boardSort)).check(matches(withEffectiveVisibility(Visibility.GONE)))
////        onView(withId(R.id.followedBoardSort)).check(matches(isDisplayed()))
////        onView(withId(R.id.followedBoardSort)).check(matches(hasChildCount(sortList.size)))
////        binding.followedBoardSort.children.forEachIndexed { index, view ->
////            val btn = view as RadioButton
////            val thisSort = sortList[index]
////            assertEquals(btn.text.toString(), thisSort.name)
////            assertEquals(btn.isChecked, thisSort == sort)
////        }
//
//        onView(withId(R.id.btnVisitPage)).check(matches(isEnabled()))
//        onView(withId(R.id.editPage)).check(matches(withText(current.toString())))
//        onView(withId(R.id.btnPrevPage)).check(matches(isDisabled()))
//        onView(withId(R.id.btnNextPage)).check(matches(isEnabled()))
//    }
//
//    @Test
//    fun clickingSortRadioButtonsCorrectlyChangesSelectedSort(){
//        val current = 1
//        val last = 5
//        val board = Board("Politics", "politics")
//        val sort = TopicListSorts.UPDATED
//        val sortList = TopicListSorts.BoardSorts
//        val dataPage = BoardsDataPage(
//            emptyList(),
//            current,
//            last,
//            emptyList(),
//            "",
//            0,
//            0,
//            false,
//            emptyList(),
//            ""
//        )
//        delegate.setBaseTopicListData(board, dataPage)
//        delegate.setPageInformation(current, last)
//
////        binding.boardSort.children.forEachIndexed { index, view ->
////            val btn = view as RadioButton
////            val thisSort = sortList[index]
////            btn.performClick()
////            assertEquals(delegate.selectedSort, thisSort)
////        }
//    }
//
//    @Test
//    fun clickingBtnNextWorksCorrectly() {
//        val current = 1
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//
//        for (i in current until last) {
//            val next = i + 1
//            val isLastPage = next == last
//            onView(withId(R.id.btnNextPage)).perform(click())
//            onView(withId(R.id.editPage)).check(matches(withText(next.toString())))
//            onView(withId(R.id.btnVisitPage)).check(matches(isEnabled()))
//            onView(withId(R.id.btnNextPage)).check(
//                matches(
//                    if (isLastPage) {
//                        isDisabled()
//                    } else {
//                        isEnabled()
//                    }
//                )
//            )
//        }
//    }
//
//    @Test
//    fun clickingBtnPrevPageWorksCorrectly() {
//        val current = 5
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//
//        for (i in current downTo 2) {
//            val prev = i - 1
//            val isFirstPage = prev == 1
//            onView(withId(R.id.btnPrevPage)).perform(click())
//            onView(withId(R.id.editPage)).check(matches(withText(prev.toString())))
//            onView(withId(R.id.btnVisitPage)).check(matches(isEnabled()))
//            onView(withId(R.id.btnPrevPage)).check(
//                matches(
//                    if (isFirstPage) {
//                        isDisabled()
//                    } else {
//                        isEnabled()
//                    }
//                )
//            )
//        }
//    }
//
//    @Test
//    fun longPressBtnNextMoveToLastPage() {
//        val current = 1
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//        onView(withId(R.id.btnNextPage)).perform(longClick())
//        onView(withId(R.id.editPage)).check(matches(withText(last.toString())))
//    }
//
//    @Test
//    fun longPressBtnPrevMoveToFirstPage() {
//        val current = 5
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//        onView(withId(R.id.btnPrevPage)).perform(longClick())
//        onView(withId(R.id.editPage)).check(matches(withText("1")))
//    }
//
//    @Test
//    fun whenBadInputEnteredViewIsUpdatedAppropriately() {
//        val current = 1
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//
//        val badInputs: Array<Int> = arrayOf(-1, 0,  last + 1, last + 2)
//        badInputs.forEach {
//            val text = it.toString()
//            onView(withId(R.id.editPage)).perform(replaceText(text), closeSoftKeyboard())
//            onView(withId(R.id.btnPrevPage)).check(
//                matches(
//                    if (it > 1) isEnabled() else isDisabled()
//                )
//            )
//            onView(withId(R.id.btnNextPage)).check(
//                matches(
//                    if (it < last) isEnabled() else isDisabled()
//                )
//            )
//            onView(withId(R.id.btnVisitPage)).check(matches(isDisabled()))
//        }
//
//        // empty text
//        onView(withId(R.id.editPage)).perform(clearText())
//        onView(withId(R.id.btnPrevPage)).check(matches(isEnabled()))
//        onView(withId(R.id.btnNextPage)).check(matches(isEnabled()))
//        onView(withId(R.id.btnVisitPage)).check(matches(isDisabled()))
//    }
//
//    @Test
//    fun whenEditTextIsEmptyOnAnyBtnNextOrBtnPrevClicked_SetTextToLastCorrectPage() {
//        val current = 1
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//
//        // # 1
//        // editText's text is cleared
//        onView(withId(R.id.editPage)).perform(clearText())
//
//        onView(withId(R.id.btnPrevPage)).check(matches(isEnabled()))
//        onView(withId(R.id.btnNextPage)).check(matches(isEnabled()))
//        onView(withId(R.id.btnVisitPage)).check(matches(isDisabled()))
//
//        // check is current
//        onView(withId(R.id.btnPrevPage)).perform(click())
//        onView(withId(R.id.editPage)).check(matches(withText(current.toString())))
//
//
//        // #2
//        onView(withId(R.id.editPage)).perform(clearText())
//        onView(withId(R.id.btnNextPage)).perform(click()) // use btnNext this time
//        onView(withId(R.id.editPage)).check(matches(withText(current.toString())))
//
//        // #3a
//        val page = "3"
//        onView(withId(R.id.editPage)).perform(replaceText(page), closeSoftKeyboard())
//        onView(withId(R.id.editPage)).perform(clearText())
//        onView(withId(R.id.btnPrevPage)).perform(click())
//        onView(withId(R.id.editPage)).check(matches(withText(page))) // "3" is last correct page
//
//        // #3b
//        onView(withId(R.id.editPage)).perform(replaceText("20"), closeSoftKeyboard())
//        onView(withId(R.id.editPage)).perform(clearText())
//        onView(withId(R.id.btnPrevPage)).perform(click())
//        onView(withId(R.id.editPage)).check(matches(withText(page))) // "3" is last correct page
//    }
//
//    @Test
//    fun whenGoodInputEntered_viewsReactCorrectly(){
//        val current = 1
//        val last = 5
//        delegate.setBaseTopicListData(Featured, TopicListDataPage(emptyList(), current, last))
//        delegate.setPageInformation(current, last)
//
//        val inputs = 1..last
//        inputs.forEach {
//            val isFirstPage = if (it == 1) isDisabled() else isEnabled()
//            val isLastPage = if (it == last) isDisabled() else isEnabled()
//            onView(withId(R.id.editPage)).perform(replaceText(it.toString()), closeSoftKeyboard())
//            onView(withId(R.id.btnNextPage)).check(matches(isLastPage))
//            onView(withId(R.id.btnPrevPage)).check(matches(isFirstPage))
//            onView(withId(R.id.btnVisitPage)).check(matches(isEnabled()))
//        }
//
//    }
//}
