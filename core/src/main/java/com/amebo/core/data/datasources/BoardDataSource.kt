package com.amebo.core.data.datasources

import com.amebo.core.domain.Board
import com.amebo.core.domain.ErrorResponse
import com.github.michaelbull.result.Result

interface BoardDataSource {
    /**
     * Copies board data into the main database from a source such as bundled database or xml resource
     */
    suspend fun initialize()

    /**
     * Loads all boards in database
     */
    suspend fun loadAll(): List<Board>

    suspend fun loadFollowedFromDisk(userSlug: String): List<Board>

    suspend fun fetchFollowedBoards(): Result<List<Board>, ErrorResponse>

    suspend fun updateFollowedBoards(userSlug: String, followedBoards: List<Board>)

    suspend fun loadRecent(): List<Board>

    suspend fun search(query: String): List<Board>

    suspend fun loadNairalandPicks(): List<Board>

    suspend fun selectFromSlug(boardSlug: String): Board
}