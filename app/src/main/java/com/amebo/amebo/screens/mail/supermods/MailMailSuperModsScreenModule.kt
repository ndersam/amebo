package com.amebo.amebo.screens.mail.supermods

import com.amebo.amebo.R
import com.amebo.amebo.screens.mail.MailView
import com.amebo.amebo.screens.mail.SimpleMailView
import dagger.Module
import dagger.Provides

@Module
class MailMailSuperModsScreenModule {

    @Provides
    fun provideMailView(screen: MailSuperModsScreen): MailView {
        return SimpleMailView(
            screen.binding,
            screen,
            screen.getString(R.string.message_super_mods)
        )
    }
}