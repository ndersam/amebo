package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class BirthDate(
    val day: Day,
    val month: Month,
    val year: Year
): Parcelable

@Parcelize
class Day(val value: Int): Parcelable

@Parcelize
class Month(val value: Int, val name: String): Parcelable

@Parcelize
class Year(val value: Int): Parcelable
