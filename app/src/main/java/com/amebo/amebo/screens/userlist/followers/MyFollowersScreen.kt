package com.amebo.amebo.screens.userlist.followers

import android.os.Bundle
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.drawerLayout.DrawerLayoutToolbarMediator
import com.amebo.amebo.common.extensions.snack
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.MyFollowersScreenBinding
import com.amebo.amebo.screens.userlist.ItemAdapter
import com.amebo.core.domain.User

class MyFollowersScreen : BaseFragment(R.layout.my_followers_screen), ItemAdapter.Listener {

    private val viewModel by viewModels<MyFollowersScreenViewModel>()
    private val binding by viewBinding(MyFollowersScreenBinding::bind)
    private lateinit var adapter: ItemAdapter

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { router.back() }
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.load()
        }
        adapter = ItemAdapter(emptyList(), this)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter
        DrawerLayoutToolbarMediator(this, binding.toolbar)

        viewModel.dataEvent.observe(
            viewLifecycleOwner,
            EventObserver(::handleEventContent)
        )
        viewModel.load()
    }


    private fun handleEventContent(resource: Resource<List<User>>) {
        when (resource) {
            is Resource.Loading -> {
                if (resource.content != null) {
                    adapter.submitList(resource.content)
                    binding.stateLayout.content()
                    binding.swipeRefreshLayout.isRefreshing = true
                } else {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.stateLayout.loading()
                }
            }
            is Resource.Success -> {
                binding.swipeRefreshLayout.isRefreshing = false
                adapter.submitList(resource.content)
                if (resource.content.isEmpty())
                    binding.stateLayout.empty()
                else
                    binding.stateLayout.content()
            }
            is Resource.Error -> {
                binding.swipeRefreshLayout.isRefreshing = false
                if (resource.content != null) {
                    adapter.submitList(resource.content)
                    binding.stateLayout.content()
                    binding.snack(resource.cause)
                } else {
                    binding.stateLayout.failure()
                }
            }
        }
    }

    override fun visitUser(user: User) {
        router.toUser(user)
    }

}
