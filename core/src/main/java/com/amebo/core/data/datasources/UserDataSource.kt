package com.amebo.core.data.datasources

import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.User

interface UserDataSource {
    suspend fun fetchData(user: User): ResultWrapper<User.Data, ErrorResponse>
    suspend fun fetchCached(user: User): User.Data?
    suspend fun fetchFollowers(): ResultWrapper<List<User>, ErrorResponse>
}