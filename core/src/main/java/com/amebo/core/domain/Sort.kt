package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class Sort(val name: String, val value: String = name.toLowerCase(Locale.ENGLISH)) : Parcelable