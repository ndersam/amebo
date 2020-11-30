package com.amebo.amebo.di

import com.amebo.core.Nairaland
import com.amebo.core.auth.AuthService
import com.amebo.core.data.datasources.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

object TestNairalandProvider {

    fun newNairalandInstance(): Nairaland {
        val nairaland = mock<Nairaland>()
        val dataSource = newDataSources()
        val auth = authProvider()
        whenever(nairaland.sources).thenReturn(dataSource)
        whenever(nairaland.auth).thenReturn(auth)
        return nairaland
    }

    private fun newDataSources(): DataSources {
        val dataSources = mock<DataSources>()

        val forms = mock<FormDataSource>()
        whenever(dataSources.forms).thenReturn(forms)

        val submissions = mock<FormSubmissionDataSource>()
        whenever(dataSources.submissions).thenReturn(submissions)

        val postList = mock<PostListDataSource>()
        whenever(dataSources.postLists).thenReturn(postList)

        val topicList = mock<TopicListDataSource>()
        whenever(dataSources.topicLists).thenReturn(topicList)

        val accounts = mock<AccountDataSource>()
        whenever(dataSources.accounts).thenReturn(accounts)

        val users = mock<UserDataSource>()
        whenever(dataSources.users).thenReturn(users)

        val boards = mock<BoardDataSource>()
        whenever(dataSources.boards).thenReturn(boards)

        val misc = mock<MiscDataSource>()
        whenever(dataSources.misc).thenReturn(misc)

        return dataSources
    }

    private fun authProvider(): AuthService{
        return mock()
    }
}