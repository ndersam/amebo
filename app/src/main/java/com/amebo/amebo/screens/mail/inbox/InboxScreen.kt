package com.amebo.amebo.screens.mail.inbox

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.drawerLayout.DrawerLayoutToolbarMediator
import com.amebo.amebo.common.extensions.emailAppIntent
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.InboxScreenBinding
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.core.domain.DismissMailNotificationForm
import com.amebo.core.domain.Session
import com.amebo.core.domain.User
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayoutManager


class InboxScreen : BaseFragment(R.layout.inbox_screen), AuthenticationRequired,
    ItemAdapter.Listener {
    private val binding by viewBinding(InboxScreenBinding::bind)
    private val userManagementViewModel by activityViewModels<UserManagementViewModel>()
    private var adapter: ItemAdapter? = null

    private val viewModel by viewModels<InboxScreenViewModel>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        DrawerLayoutToolbarMediator(this, binding.toolbar)
        binding.recyclerView.layoutManager =
            FlexboxLayoutManager(requireContext(), FlexDirection.ROW, FlexWrap.WRAP)
        binding.recyclerView.addItemDecoration(
            FlexboxItemDecoration(context).apply {
                setOrientation(FlexboxItemDecoration.BOTH) // or VERTICAL or BOTH
                setDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_pm_senders_divider
                    )
                )
            }
        )
        adapter = ItemAdapter(this)
        binding.recyclerView.adapter = adapter

        binding.btnCheckMail.setOnClickListener {
            val intent = requireContext().emailAppIntent()
            if (intent != null) {
                startActivity(intent)
            }
        }

        binding.btnDismiss.setOnClickListener {
            viewModel.dismiss()
        }

        viewModel.dismissMailEvent.observe(
            viewLifecycleOwner,
            EventObserver(::handleDismissMailEvent)
        )
        userManagementViewModel.sessionEvent.observe(
            viewLifecycleOwner,
            {
                if (pref.isLoggedIn) {
                    onSessionEvent(it)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun onUserClicked(user: User) = router.toUser(user)

    private fun onSessionEvent(session: Session) {
        when (val form = session.mailNotificationForm) {
            is DismissMailNotificationForm -> {
                viewModel.form = form
                binding.noMail.isVisible = false
                binding.newMail.isVisible = true
                binding.txtMailCount.text = if (form.senders.size > 99)
                    "99+"
                else form.senders.size.toString()
                adapter!!.submitList(form.senders)
            }
            null -> {
                binding.noMail.isVisible = true
                binding.newMail.isVisible = false
            }
        }
    }

    private fun handleDismissMailEvent(resource: Resource<Unit>) {
        when (resource) {
            is Resource.Success -> {

            }
            is Resource.Error -> {

            }
            is Resource.Loading -> {

            }
        }
    }

}