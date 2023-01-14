package com.amebo.core.auth

import android.annotation.SuppressLint
import com.amebo.core.data.local.Database
import com.amebo.core.domain.ErrorResponse
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import javax.inject.Inject

class AuthService @Inject internal constructor(
    private val authenticator: Authenticator,
    private val db: Database
) {
    @SuppressLint("DefaultLocale")
    suspend fun login(username: String, password: String): Result<Unit, ErrorResponse> {
        return authenticator.login(username, password)
            .onSuccess {
                db.cookieQueries.insert(username.toLowerCase(), it)
                db.userAccountQueries.insert(
                    username, username.toLowerCase(),
                    isLoggedIn = true
                )
            }.map {  }
    }
}