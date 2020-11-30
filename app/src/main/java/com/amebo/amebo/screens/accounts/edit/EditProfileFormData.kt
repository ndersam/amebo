package com.amebo.amebo.screens.accounts.edit

import android.net.Uri
import com.amebo.core.domain.BirthDate
import com.amebo.core.domain.Gender
import com.amebo.core.domain.Year

class EditProfileFormData(
    var birthDate: BirthDate?,
    var personalText: String,
    var signature: String,
    var location: String,
    var yim: String,
    var twitter: String,
    var gender: Gender?,
    var selectedPhoto: Uri?,
    var removeThisImage: Boolean,
    val earliestYear: Year
)