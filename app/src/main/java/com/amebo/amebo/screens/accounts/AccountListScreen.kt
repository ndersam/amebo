package com.amebo.amebo.screens.accounts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.extensions.restart
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.InjectablePaddedBottomSheetFragment
import com.amebo.amebo.databinding.AccountListScreenBinding
import com.amebo.core.domain.AnonymousAccount
import com.amebo.core.domain.RealUserAccount
import com.amebo.core.domain.UserAccount

class AccountListScreen : InjectablePaddedBottomSheetFragment(), AccountAdapter.Listener {
    override val contentResId = R.layout.account_list_screen
    private val binding by viewBinding(AccountListScreenBinding::bind)
    private val viewModel get() = createViewModel<UserManagementViewModel>(requireActivity())
    private var adapter: AccountAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_DELETED_USER) { _, bundle ->
            val user = bundle.getParcelable<RealUserAccount>(FragKeys.BUNDLE_DELETED_USER)!!
            removeUser(user)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.accountListEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onAccountsLoaded)
        )

        val activity = requireActivity()
        viewModel.removeUserAccountEvent.observe(activity, EventObserver {
            val (_, isCurrentUser) = it
            if (isCurrentUser) {
                activity.restart()
            }
        })

        viewModel.loadAccounts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    private fun onAccountsLoaded(accounts: List<UserAccount>) {
        adapter = AccountAdapter(accounts, this)
        binding.recyclerView.adapter = adapter
    }


    override fun onRemoveClicked(userAccount: RealUserAccount, position: Int) =
        router.toAccountDeletion(userAccount)


    override fun onUserClicked(userAccount: UserAccount) {
        when (userAccount) {
            is AnonymousAccount -> {
                if (!pref.isLoggedIn) return
                viewModel.setUser(userAccount)
                requireActivity().restart()
            }
            is RealUserAccount -> {
                if (pref.isCurrentAccount(userAccount)) return
                if (userAccount.isLoggedIn) {
                    viewModel.setUser(userAccount)
                    requireActivity().restart()
                } else {
                    // user needs to login again
                    router.toSignIn(userAccount.user.name)
                }
            }
        }
        dismiss()
    }

    override fun onAddClicked() {
        router.toSignIn()
    }

    override fun isCurrentUser(userAccount: UserAccount): Boolean {
        return pref.isCurrentAccount(userAccount)
    }

    private fun removeUser(userAccount: RealUserAccount) {
        val adapter = this.adapter ?: return
        val isCurrentUser = pref.isCurrentAccount(userAccount)
        if (isCurrentUser) {
            pref.logOut()
        }
        adapter.remove(userAccount)
        viewModel.removeUser(userAccount, isCurrentUser)
    }
}

