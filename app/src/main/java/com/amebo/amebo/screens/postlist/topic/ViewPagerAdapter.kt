package com.amebo.amebo.screens.postlist.topic

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.amebo.amebo.R
import com.amebo.core.domain.Topic

class ViewPagerAdapter(private val topic: Topic, private val fragment: Fragment) :
    FragmentStatePagerAdapter(
        fragment.childFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    private val fragments = arrayOf<Fragment>(
        TopicInfoFragment().apply {
            arguments = bundleOf(TopicInfoFragment.KEY_TOPIC to topic)
        },
        RecentTopicsFragment()
    )


    override fun getCount(): Int = fragments.size

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TopicInfoFragment().apply {
                arguments = bundleOf(TopicInfoFragment.KEY_TOPIC to topic)
            }
            1 -> RecentTopicsFragment()
            else -> throw IllegalArgumentException()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> fragment.getString(R.string.this_topic)
        1 -> fragment.getString(R.string.recent)
        else -> throw  IllegalStateException()
    }


    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        // Workaround for java.lang.IllegalStateException: Fragment no longer exists for key ...
        try {
            super.restoreState(state, loader)
        } catch (e: Exception) {
        }
    }

}