package com.amebo.core.auth

import android.annotation.SuppressLint
import com.amebo.core.Database
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.ResultWrapper
import javax.inject.Inject

class AuthService @Inject constructor(
    private val authenticator: Authenticator,
    private val db: Database
) {
    @SuppressLint("DefaultLocale")
    suspend fun login(username: String, password: String): ResultWrapper<Unit, ErrorResponse> {
        return authenticator.login(username, password)
            .either(
                {
                    db.cookieQueries.insert(username.toLowerCase(), it.data)
                    db.userAccountQueries.insert(
                        username, username.toLowerCase(),
                        isLoggedIn = true
                    )
                    ResultWrapper.Success(Unit)
                },
                { it }
            )
    }
}