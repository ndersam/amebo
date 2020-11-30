package com.amebo.amebo.di

import com.amebo.amebo.screens.accounts.AccountListScreen
import com.amebo.amebo.screens.accounts.edit.EditAccountScreen
import com.amebo.amebo.screens.editactions.EditActionsPickerScreen
import com.amebo.amebo.screens.explore.ExploreScreen
import com.amebo.amebo.screens.explore.SearchOptionsDialog
import com.amebo.amebo.screens.explore.SelectBoardDialog
import com.amebo.amebo.screens.imagepicker.ImagePickerScreen
import com.amebo.amebo.screens.mail.inbox.InboxScreen
import com.amebo.amebo.screens.mail.mods.MailBoardModsScreen
import com.amebo.amebo.screens.mail.mods.MailBoardModsScreenModule
import com.amebo.amebo.screens.mail.supermods.MailMailSuperModsScreenModule
import com.amebo.amebo.screens.mail.supermods.MailSuperModsScreen
import com.amebo.amebo.screens.mail.user.MailUserScreen
import com.amebo.amebo.screens.mail.user.MailUserScreenModule
import com.amebo.amebo.screens.newpost.modifypost.ModifyPostScreen
import com.amebo.amebo.screens.newpost.modifypost.ModifyPostScreenBindingModule
import com.amebo.amebo.screens.newpost.muslim.MuslimDeclarationScreen
import com.amebo.amebo.screens.newpost.newpost.NewPostScreen
import com.amebo.amebo.screens.newpost.newpost.NewPostScreenBindingModule
import com.amebo.amebo.screens.newpost.newtopic.NewTopicScreen
import com.amebo.amebo.screens.newpost.newtopic.NewTopicScreenBindingModule
import com.amebo.amebo.screens.newpost.quotepost.PostPickerScreen
import com.amebo.amebo.screens.photoviewer.ImageFragment
import com.amebo.amebo.screens.photoviewer.posts.PhotoViewerScreen
import com.amebo.amebo.screens.photoviewer.posts.PhotoViewerScreenModule
import com.amebo.amebo.screens.photoviewer.user.UserPhotoViewerScreen
import com.amebo.amebo.screens.photoviewer.user.UserPhotoViewerScreenModule
import com.amebo.amebo.screens.postlist.PostListPageNavigationDialog
import com.amebo.amebo.screens.postlist.following.PostsFromFollowingScreenModule
import com.amebo.amebo.screens.postlist.following.PostsFromFollowingsScreen
import com.amebo.amebo.screens.postlist.likesandshares.LikesAndSharesScreen
import com.amebo.amebo.screens.postlist.likesandshares.LikesAndSharesScreenModule
import com.amebo.amebo.screens.postlist.mentions.MentionsScreen
import com.amebo.amebo.screens.postlist.mentions.MentionsScreenModule
import com.amebo.amebo.screens.postlist.mylikes.MyLikesScreen
import com.amebo.amebo.screens.postlist.mylikes.MyLikesScreenModule
import com.amebo.amebo.screens.postlist.myshares.MySharedPostsScreen
import com.amebo.amebo.screens.postlist.myshares.MySharedPostsScreenModule
import com.amebo.amebo.screens.postlist.recentposts.RecentPostsScreen
import com.amebo.amebo.screens.postlist.recentposts.RecentPostsScreenModule
import com.amebo.amebo.screens.postlist.sharedwithme.SharedWithMeScreen
import com.amebo.amebo.screens.postlist.sharedwithme.SharedWithMeScreenModule
import com.amebo.amebo.screens.postlist.topic.RecentTopicsFragment
import com.amebo.amebo.screens.postlist.topic.TopicInfoFragment
import com.amebo.amebo.screens.postlist.topic.TopicScreen
import com.amebo.amebo.screens.postlist.topic.TopicScreenModule
import com.amebo.amebo.screens.postlist.topicloader.TopicLoaderScreen
import com.amebo.amebo.screens.postlist.userposts.UserPostsScreen
import com.amebo.amebo.screens.postlist.userposts.UsersPostsScreenModule
import com.amebo.amebo.screens.reportpost.ReportPostScreen
import com.amebo.amebo.screens.rules.RulesScreen
import com.amebo.amebo.screens.search.SearchResultScreen
import com.amebo.amebo.screens.search.SearchResultsScreenModule
import com.amebo.amebo.screens.search.SearchScreen
import com.amebo.amebo.screens.settings.SettingsScreen
import com.amebo.amebo.screens.signin.SignInScreen
import com.amebo.amebo.screens.topiclist.TopicListPageSelectionDialog
import com.amebo.amebo.screens.topiclist.history.ViewedTopicListScreen
import com.amebo.amebo.screens.topiclist.home.HomeScreen
import com.amebo.amebo.screens.topiclist.home.HomeScreenModule
import com.amebo.amebo.screens.topiclist.main.TopicListScreen
import com.amebo.amebo.screens.topiclist.main.TopicListScreenModule
import com.amebo.amebo.screens.topiclist.simple.SimpleTopicListScreen
import com.amebo.amebo.screens.topiclist.user.UserTopicsScreen
import com.amebo.amebo.screens.topiclist.user.UserTopicsScreenModule
import com.amebo.amebo.screens.user.ProfileScreen
import com.amebo.amebo.screens.user.UserScreen
import com.amebo.amebo.screens.user.preview.UserPreviewScreen
import com.amebo.amebo.screens.user.search.GoToUserScreen
import com.amebo.amebo.screens.userlist.UserListScreen
import com.amebo.amebo.screens.userlist.followers.MyFollowersScreen
import com.amebo.amebo.screens.youtube.YoutubeScreen
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("UNUSED")
@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun postPickerScreen(): PostPickerScreen

    @ContributesAndroidInjector
    abstract fun viewedTopicListScreen(): ViewedTopicListScreen

    @ContributesAndroidInjector
    abstract fun topicLoaderScreen(): TopicLoaderScreen

    @ContributesAndroidInjector
    abstract fun rulesScreen(): RulesScreen

    @ContributesAndroidInjector
    abstract fun userPreviewScreen(): UserPreviewScreen

    @ContributesAndroidInjector(modules = [MailMailSuperModsScreenModule::class])
    abstract fun mailSuperMods(): MailSuperModsScreen

    @ContributesAndroidInjector(modules = [MailBoardModsScreenModule::class])
    abstract fun mailBoardModsScreen(): MailBoardModsScreen

    @ContributesAndroidInjector(modules = [HomeScreenModule::class])
    abstract fun homeScreen(): HomeScreen

    @ContributesAndroidInjector
    abstract fun inboxScreen(): InboxScreen

    @ContributesAndroidInjector
    abstract fun goToUserScreen(): GoToUserScreen

    @ContributesAndroidInjector
    abstract fun myFollowersScreen(): MyFollowersScreen

    @ContributesAndroidInjector
    abstract fun muslimDeclarationScreen(): MuslimDeclarationScreen

    @ContributesAndroidInjector
    abstract fun exploreScreen(): ExploreScreen

    @ContributesAndroidInjector
    abstract fun profileScreen(): ProfileScreen

    @ContributesAndroidInjector
    abstract fun signInScreen(): SignInScreen

    @ContributesAndroidInjector(modules = [TopicListScreenModule::class])
    abstract fun topicListScreen(): TopicListScreen

    @ContributesAndroidInjector
    abstract fun simpleTopicListScreen(): SimpleTopicListScreen

    @ContributesAndroidInjector
    abstract fun topicListBottomSheet(): TopicListPageSelectionDialog

    @ContributesAndroidInjector(modules = [TopicScreenModule::class])
    abstract fun topicScreen(): TopicScreen

    @ContributesAndroidInjector
    abstract fun searchOptionsDialog(): SearchOptionsDialog

    @ContributesAndroidInjector
    abstract fun selectBoardDialog(): SelectBoardDialog

    @ContributesAndroidInjector
    abstract fun settingsScreen(): SettingsScreen

    @ContributesAndroidInjector(modules = [RecentPostsScreenModule::class])
    abstract fun recentPostsScreen(): RecentPostsScreen

    @ContributesAndroidInjector(modules = [PhotoViewerScreenModule::class])
    abstract fun photoViewerScreen(): PhotoViewerScreen

    @ContributesAndroidInjector(modules = [UserPhotoViewerScreenModule::class])
    abstract fun userPhotoViewerScreen(): UserPhotoViewerScreen

    @ContributesAndroidInjector
    abstract fun youtubeScreen(): YoutubeScreen

    @ContributesAndroidInjector
    abstract fun imageFragment(): ImageFragment

    @ContributesAndroidInjector(modules = [SearchResultsScreenModule::class])
    abstract fun searchResultsFragment(): SearchResultScreen

    @ContributesAndroidInjector(modules = [MentionsScreenModule::class])
    abstract fun mentionsScreen(): MentionsScreen

    @ContributesAndroidInjector
    abstract fun postListPageNavigationDialog(): PostListPageNavigationDialog

    @ContributesAndroidInjector
    abstract fun userScreen(): UserScreen

    @ContributesAndroidInjector(modules = [PostsFromFollowingScreenModule::class])
    abstract fun postsFromFollowingsScreen(): PostsFromFollowingsScreen

    @ContributesAndroidInjector(modules = [SharedWithMeScreenModule::class])
    abstract fun sharedWithMeScreen(): SharedWithMeScreen

    @ContributesAndroidInjector(modules = [LikesAndSharesScreenModule::class])
    abstract fun likesAndSharesScreen(): LikesAndSharesScreen

    @ContributesAndroidInjector(modules = [MyLikesScreenModule::class])
    abstract fun myLikesScreen(): MyLikesScreen

    @ContributesAndroidInjector(modules = [MySharedPostsScreenModule::class])
    abstract fun mySharedPostsScreen(): MySharedPostsScreen

    @ContributesAndroidInjector(modules = [NewPostScreenBindingModule::class])
    abstract fun newPostScreen(): NewPostScreen

    @ContributesAndroidInjector(modules = [ModifyPostScreenBindingModule::class])
    abstract fun modifyPostScreen(): ModifyPostScreen

    @ContributesAndroidInjector(modules = [NewTopicScreenBindingModule::class])
    abstract fun newTopicScreen(): NewTopicScreen

    @ContributesAndroidInjector
    abstract fun imagePickerScreen(): ImagePickerScreen

    @ContributesAndroidInjector
    abstract fun reportPostScreen(): ReportPostScreen

    @ContributesAndroidInjector(modules = [UsersPostsScreenModule::class])
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

    @ContributesAndroidInjector(modules = [MailUserScreenModule::class])
    abstract fun mailUserScreen(): MailUserScreen

    @ContributesAndroidInjector
    abstract fun editAccountScreen(): EditAccountScreen

    @ContributesAndroidInjector(modules = [UserTopicsScreenModule::class])
    abstract fun userTopicsScreen(): UserTopicsScreen
}