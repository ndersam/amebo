package com.amebo.amebo.screens.mail.user

import com.amebo.amebo.R
import com.amebo.amebo.screens.mail.MailView
import com.amebo.amebo.screens.mail.SimpleMailView
import dagger.Module
import dagger.Provides

@Module
class MailUserScreenModule {

    @Provides
    fun provideMailView(screen: MailUserScreen): MailView {
        return SimpleMailView(
            screen.binding,
            screen,
            screen.getString(R.string.message_x, screen.user.name)
        )
    }
}