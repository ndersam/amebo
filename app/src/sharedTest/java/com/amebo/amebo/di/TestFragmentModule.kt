package com.amebo.amebo.di

import com.amebo.amebo.di.mocks.Mocks
import com.amebo.amebo.screens.accounts.AccountListScreen
import com.amebo.amebo.screens.editactions.EditActionsPickerScreen
import com.amebo.amebo.screens.explore.ExploreScreen
import com.amebo.amebo.screens.explore.SearchOptionsDialog
import com.amebo.amebo.screens.explore.SelectBoardDialog
import com.amebo.amebo.screens.imagepicker.ImagePickerScreen
import com.amebo.amebo.screens.mail.inbox.InboxScreen
import com.amebo.amebo.screens.newpost.modifypost.ModifyPostScreen
import com.amebo.amebo.screens.newpost.newpost.NewPostScreen
import com.amebo.amebo.screens.newpost.newtopic.NewTopicScreen
import com.amebo.amebo.screens.photoviewer.ImageFragment
import com.amebo.amebo.screens.photoviewer.posts.PhotoViewerScreen
import com.amebo.amebo.screens.postlist.PostListPageNavigationDialog
import com.amebo.amebo.screens.postlist.following.PostsFromFollowingsScreen
import com.amebo.amebo.screens.postlist.likesandshares.LikesAndSharesScreen
import com.amebo.amebo.screens.postlist.mentions.MentionsScreen
import com.amebo.amebo.screens.postlist.recentposts.RecentPostsScreen
import com.amebo.amebo.screens.postlist.sharedwithme.SharedWithMeScreen
import com.amebo.amebo.screens.postlist.topic.RecentTopicsFragment
import com.amebo.amebo.screens.postlist.topic.TopicInfoFragment
import com.amebo.amebo.screens.postlist.topic.TopicScreen
import com.amebo.amebo.screens.postlist.userposts.UserPostsScreen
import com.amebo.amebo.screens.reportpost.ReportPostScreen
import com.amebo.amebo.screens.search.SearchResultScreen
import com.amebo.amebo.screens.search.SearchScreen
import com.amebo.amebo.screens.settings.SettingsScreen
import com.amebo.amebo.screens.signin.SignInScreen
import com.amebo.amebo.screens.topiclist.TopicListPageSelectionDialog
import com.amebo.amebo.screens.topiclist.main.TopicListScreen
import com.amebo.amebo.screens.topiclist.main.TopicListScreenModule
import com.amebo.amebo.screens.user.ProfileScreen
import com.amebo.amebo.screens.user.UserScreen
import com.amebo.amebo.screens.userlist.UserListScreen
import com.amebo.amebo.screens.youtube.YoutubeScreen
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [TestRouterModule::class])
abstract class TestFragmentModule {

    @ContributesAndroidInjector
    abstract fun inboxScreen(): InboxScreen

    @ContributesAndroidInjector
    abstract fun exploreScreen(): ExploreScreen

    @ContributesAndroidInjector
    abstract fun profileScreen(): ProfileScreen


    @ContributesAndroidInjector
    abstract fun signInScreen(): SignInScreen

    @ContributesAndroidInjector(modules = [TopicListScreenModule::class])
    abstract fun boardScreen(): TopicListScreen

    @ContributesAndroidInjector
    abstract fun topicListBottomSheet(): TopicListPageSelectionDialog

    @ContributesAndroidInjector(modules = [Mocks.TopicScreen::class])
    abstract fun topicScreen(): TopicScreen

    @ContributesAndroidInjector
    abstract fun searchOptionsDialog(): SearchOptionsDialog

    @ContributesAndroidInjector
    abstract fun selectBoardDialog(): SelectBoardDialog

    @ContributesAndroidInjector
    abstract fun settingsScreen(): SettingsScreen

    @ContributesAndroidInjector(modules = [PostListViewModule::class])
    abstract fun recentPostsScreen(): RecentPostsScreen

    @ContributesAndroidInjector(modules = [PhotoViewModule::class])
    abstract fun photoViewerScreen(): PhotoViewerScreen

    @ContributesAndroidInjector
    abstract fun youtubeScreen(): YoutubeScreen

    @ContributesAndroidInjector
    abstract fun imageFragment(): ImageFragment

    @ContributesAndroidInjector(modules = [SearchResultsViewModule::class])
    abstract fun searchResultsFragment(): SearchResultScreen

    @ContributesAndroidInjector(modules = [PostListViewModule::class])
    abstract fun mentionsScreen(): MentionsScreen

    @ContributesAndroidInjector
    abstract fun postListPageNavigationDialog(): PostListPageNavigationDialog

    @ContributesAndroidInjector
    abstract fun userScreen(): UserScreen

    @ContributesAndroidInjector(modules = [PostListViewModule::class])
    abstract fun postsFromFollowingsScreen(): PostsFromFollowingsScreen

    @ContributesAndroidInjector(modules = [PostListViewModule::class])
    abstract fun sharedWithMeScreen(): SharedWithMeScreen


    @ContributesAndroidInjector(modules = [PostListViewModule::class])
    abstract fun likesAndSharesScreen(): LikesAndSharesScreen

    @ContributesAndroidInjector(modules = [Mocks.NewPostScreen::class])
    abstract fun newPostScreen(): NewPostScreen

    @ContributesAndroidInjector(modules = [TestModifyPostScreenModule::class])
    abstract fun modifyPostScreen(): ModifyPostScreen

    @ContributesAndroidInjector(modules = [TestNewTopicScreenModule::class])
    abstract fun newTopicScreen(): NewTopicScreen

    @ContributesAndroidInjector
    abstract fun imagePickerScreen(): ImagePickerScreen

    @ContributesAndroidInjector
    abstract fun reportPostScreen(): ReportPostScreen

    @ContributesAndroidInjector(modules = [PostListViewModule::class])
    abstract fun userPostsScreen(): UserPostsScreen

    @ContributesAndroidInjector
    abstract fun accountListScreen(): AccountListScreen

    @ContributesAndroidInjector
    abstract fun userListScreen(): UserListScreen

    @ContributesAndroidInjector
    abstract fun searchScreen(): SearchScreen

    @ContributesAndroidInjector
    abstract fun recentTopicsFragment(): RecentTopicsFragment

    @ContributesAndroidInjector
    abstract fun topicInfoFragment(): TopicInfoFragment

    @ContributesAndroidInjector
    abstract fun settingsScreenPreferenceFragment(): SettingsScreen.PreferenceFragment

    @ContributesAndroidInjector
    abstract fun editActionsPickerScreen(): EditActionsPickerScreen

}
