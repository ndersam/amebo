package com.amebo.amebo.screens.accounts

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.fragments.PaddedBottomSheetFragment
import com.amebo.amebo.databinding.LayoutRemoveAccountBinding
import com.amebo.core.domain.RealUserAccount

class AccountDeletionScreen : PaddedBottomSheetFragment() {
    private lateinit var binding: LayoutRemoveAccountBinding
    private val userAccount get() = requireArguments().getParcelable<RealUserAccount>(USER)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userAccount.user
        val title = requireContext().getString(R.string.delete_user, user.name)
        binding = inflate(LayoutRemoveAccountBinding::inflate, title)
        binding.txtRemoveAccount.setOnClickListener { deleteAccount() }
        binding.btnCancel.setOnClickListener { cancelAction() }
    }

    private fun deleteAccount() {
        setFragmentResult(FragKeys.RESULT_DELETED_USER, bundleOf(FragKeys.BUNDLE_DELETED_USER to userAccount))
        dismiss()
    }

    private fun cancelAction() {
        dismiss()
    }

    companion object {
        private const val USER = "user"
        fun newBundle(userAccount: RealUserAccount) = bundleOf(USER to userAccount)
    }
}