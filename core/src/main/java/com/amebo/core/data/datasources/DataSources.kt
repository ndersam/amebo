package com.amebo.core.data.datasources

import android.content.Context
import javax.inject.Inject

class DataSources @Inject constructor(
    val topicLists: TopicListDataSource,
    val postLists: PostListDataSource,
    val users: UserDataSource,
    val forms: FormDataSource,
    val submissions: FormSubmissionDataSource,
    val boards: BoardDataSource,
    val accounts: AccountDataSource,
    val misc: MiscDataSource
) {
    suspend fun initialize() {
        boards.initialize()
        topicLists.initialize()
    }

    suspend fun migrate(context: Context, currentUserName: String?) {
        accounts.migrate(context, currentUserName)
    }
}


