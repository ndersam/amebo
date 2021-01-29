package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class Sort(val name: String, val value: String = name.toLowerCase(Locale.ENGLISH)) : Parcelable