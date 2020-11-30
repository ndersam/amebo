package com.amebo.amebo.screens.postlist.topic

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.amebo.core.domain.Topic

class ViewPagerAdapter(private val topic: Topic, fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    private val fragments = arrayOf<Fragment>(
        TopicInfoFragment().apply {
            arguments = bundleOf(TopicInfoFragment.KEY_TOPIC to topic)
        },
        RecentTopicsFragment()
    )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> TopicInfoFragment().apply {
                arguments = bundleOf(TopicInfoFragment.KEY_TOPIC to topic)
            }
            1 -> RecentTopicsFragment()
            else -> throw IllegalArgumentException()
        }
    }
}