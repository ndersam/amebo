package com.amebo.amebo.screens.userlist

import android.os.Bundle
import androidx.core.os.bundleOf
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.UserListScreenBinding
import com.amebo.core.domain.User

class UserListScreen : BaseFragment(R.layout.user_list_screen), ItemAdapter.Listener {

    private val binding by viewBinding(UserListScreenBinding::bind)
    private val users get() = requireArguments().getParcelableArray(USERS) as Array<User>
    private val title get() = requireArguments().getString(TITLE)!!

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener { router.back() }
        binding.recyclerView.adapter = ItemAdapter(users.toList(), this)
        binding.recyclerView.setHasFixedSize(true)
    }


    override fun visitUser(user: User) {
        router.toUser(user)
    }

    companion object {
        private const val USERS = "data"
        private const val TITLE = "title"

        fun newBundle(users: List<User>, title: String) =
            bundleOf(USERS to users.toTypedArray(), TITLE to title)
    }
}
