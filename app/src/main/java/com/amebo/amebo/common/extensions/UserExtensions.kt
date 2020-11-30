package com.amebo.amebo.common.extensions

import com.amebo.amebo.R
import com.amebo.core.domain.Gender
import com.amebo.core.domain.User

val User.genderDrawable: Int?
    get() = when (gender) {
        null, Gender.Unknown -> null
        Gender.Female -> R.drawable.ic_gender_female
        Gender.Male -> R.drawable.ic_gender_male
    }