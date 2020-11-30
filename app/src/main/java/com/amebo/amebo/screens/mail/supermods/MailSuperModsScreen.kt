package com.amebo.amebo.screens.mail.supermods

import android.os.Bundle
import com.amebo.amebo.screens.mail.BaseMailScreen

class MailSuperModsScreen : BaseMailScreen<Unit>() {
    override val viewModel by viewModels<MailSuperModsViewModel>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        viewModel.initializeFormLoading()
    }
}