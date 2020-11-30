package com.amebo.amebo.screens.mail.user

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.screens.mail.BaseMailScreen
import com.amebo.core.domain.User

class MailUserScreen : BaseMailScreen<User.Data>() {
    override val viewModel: MailUserScreenViewModel by viewModels()

    val user get() = requireArguments().getParcelable<User>(USER)!!

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        viewModel.initialize(user)
    }

    override fun onSubmissionSuccess(content: User.Data) {
        setFragmentResult(
            FragKeys.RESULT_UPDATED_USER,
            bundleOf(FragKeys.BUNDLE_UPDATED_USER to content)
        )
    }

    companion object {
        private const val USER = "USER"
        fun newBundle(user: User) = bundleOf(USER to user)
    }
}