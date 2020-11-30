package com.amebo.amebo.screens.mail.mods

import com.amebo.amebo.R
import com.amebo.amebo.screens.mail.MailView
import com.amebo.amebo.screens.mail.SimpleMailView
import dagger.Module
import dagger.Provides

@Module
class MailBoardModsScreenModule {

    @Provides
    fun provideMailView(screen: MailBoardModsScreen): MailView {
        return SimpleMailView(
            screen.binding,
            screen,
            screen.getString(R.string.message_x_mods, screen.board.name)
        )
    }
}