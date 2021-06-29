package com.amebo.core.data.datasources

import android.content.Context
import com.amebo.core.domain.*
import com.github.michaelbull.result.Result

interface AccountDataSource {
    suspend fun loadAccountUsers(): List<UserAccount>
    suspend fun removeUser(account: RealUserAccount)
    suspend fun logout(account: RealUserAccount)
    suspend fun logout(user: User)
    suspend fun login(account: RealUserAccount)
    suspend fun displayPhoto(user: User): Result<DisplayPhoto, ErrorResponse>
    suspend fun migrate(context: Context, currentUserName: String?)
}