package com.amebo.amebo.common.extensions

import android.content.Context
import com.amebo.amebo.R
import com.amebo.core.domain.AnonymousAccount
import com.amebo.core.domain.RealUserAccount
import com.amebo.core.domain.UserAccount

fun UserAccount.displayName(context: Context) = when(this){
    is AnonymousAccount -> context.getString(R.string.anonymous)
    is RealUserAccount -> user.name
}