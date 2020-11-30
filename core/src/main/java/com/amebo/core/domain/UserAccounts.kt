package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


sealed class UserAccount : Parcelable

@Parcelize
object AnonymousAccount : UserAccount()

@Parcelize
class RealUserAccount(val user: User, val isLoggedIn: Boolean) : UserAccount()