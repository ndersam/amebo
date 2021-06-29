package com.amebo.core.data.datasources

import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.User
import com.github.michaelbull.result.Result

interface UserDataSource {
    suspend fun fetchData(user: User): Result<User.Data, ErrorResponse>
    suspend fun fetchCached(user: User): User.Data?
    suspend fun fetchFollowers(): Result<List<User>, ErrorResponse>
}