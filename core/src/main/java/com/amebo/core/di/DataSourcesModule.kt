package com.amebo.core.di

import com.amebo.core.data.datasources.*
import com.amebo.core.data.datasources.impls.*
import dagger.Binds
import dagger.Module

@Suppress("UNUSED")
@Module
abstract class DataSourcesModule {
    @Binds
    abstract fun bindTopicListDataSource(topicListDataSourceImpl: TopicListDataSourceImpl): TopicListDataSource

    @Binds
    abstract fun bindBoardDataSource(boardDataSourceImpl: BoardDataSourceImpl): BoardDataSource

    @Binds
    abstract fun bindUserDataSource(userDataSourceImpl: UserDataSourceImpl): UserDataSource

    @Binds
    abstract fun postListDatSource(postListDataSourceImpl: PostListDataSourceImpl): PostListDataSource

    @Binds
    abstract fun formDataSource(formDataSourceImpl: FormDataSourceImpl): FormDataSource

    @Binds
    abstract fun formSubmissionDataSource(formSubmissionDataSourceImpl: FormSubmissionDataSourceImpl): FormSubmissionDataSource

    @Binds
    abstract fun miscDataSourceImpl(miscDataSourceImpl: MiscDataSourceImpl): MiscDataSource


    @Binds
    abstract fun accountDataSourceImpl(accountDataSourceImpl: AccountDataSourceImpl): AccountDataSource
}

