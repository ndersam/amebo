package com.amebo.amebo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amebo.amebo.application.MainActivityViewModel
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.screens.accounts.edit.EditAccountScreenViewModel
import com.amebo.amebo.screens.explore.ExploreScreenViewModel
import com.amebo.amebo.screens.explore.ExploreSharedViewModel
import com.amebo.amebo.screens.explore.SelectBoardViewModel
import com.amebo.amebo.screens.imagepicker.ImagePickerSharedViewModel
import com.amebo.amebo.screens.mail.inbox.InboxScreenViewModel
import com.amebo.amebo.screens.mail.mods.MailBoardModsViewModel
import com.amebo.amebo.screens.mail.supermods.MailSuperModsViewModel
import com.amebo.amebo.screens.mail.user.MailUserScreenViewModel
import com.amebo.amebo.screens.newpost.modifypost.ModifyPostScreenViewModel
import com.amebo.amebo.screens.newpost.muslim.MuslimDeclarationScreenViewModel
import com.amebo.amebo.screens.newpost.newpost.NewPostScreenViewModel
import com.amebo.amebo.screens.newpost.newtopic.NewTopicScreenViewModel
import com.amebo.amebo.screens.photoviewer.PhotoSharedViewModel
import com.amebo.amebo.screens.postlist.SelectedPageSharedViewModel
import com.amebo.amebo.screens.postlist.following.FollowingViewModel
import com.amebo.amebo.screens.postlist.likesandshares.LikesAndSharesViewModel
import com.amebo.amebo.screens.postlist.mentions.MentionsViewModel
import com.amebo.amebo.screens.postlist.mylikes.MyLikesScreenViewModel
import com.amebo.amebo.screens.postlist.myshares.MySharedPostsViewModel
import com.amebo.amebo.screens.postlist.recentposts.RecentPostsViewModel
import com.amebo.amebo.screens.postlist.sharedwithme.SharedWithMeViewModel
import com.amebo.amebo.screens.postlist.topic.RecentTopicsViewModel
import com.amebo.amebo.screens.postlist.topic.TopicViewModel
import com.amebo.amebo.screens.postlist.topicloader.TopicLoaderScreenViewModel
import com.amebo.amebo.screens.postlist.userposts.UserPostsViewModel
import com.amebo.amebo.screens.reportpost.ReportPostSharedScreenViewModel
import com.amebo.amebo.screens.search.SearchResultsViewModel
import com.amebo.amebo.screens.search.SearchViewModel
import com.amebo.amebo.screens.settings.SettingsViewModel
import com.amebo.amebo.screens.splash.SplashViewModel
import com.amebo.amebo.screens.topiclist.TopicListScreenViewModel
import com.amebo.amebo.screens.topiclist.history.ViewedTopicsViewModel
import com.amebo.amebo.screens.topiclist.simple.SimpleTopicListScreenViewModel
import com.amebo.amebo.screens.user.UserScreenViewModel
import com.amebo.amebo.screens.userlist.followers.MyFollowersScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(ViewedTopicsViewModel::class)
    abstract fun viewedTopicsViewModel(vm: ViewedTopicsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MailSuperModsViewModel::class)
    abstract fun mailSuperModsViewModel(vm: MailSuperModsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MailBoardModsViewModel::class)
    abstract fun mailBoardModsViewModel(vm: MailBoardModsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TopicLoaderScreenViewModel::class)
    abstract fun topicLoaderScreenViewModel(vm: TopicLoaderScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExploreScreenViewModel::class)
    abstract fun exploreScreenViewModel(vm: ExploreScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserManagementViewModel::class)
    abstract fun guestProfileViewModel(vm: UserManagementViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun splashViewModel(vm: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TopicListScreenViewModel::class)
    abstract fun boardScreenViewModel(vm: TopicListScreenViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(TopicViewModel::class)
    abstract fun topicViewModel(vm: TopicViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun mainActivityViewModel(vm: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExploreSharedViewModel::class)
    abstract fun exploreSharedViewModel(vm: ExploreSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectBoardViewModel::class)
    abstract fun selectBoardViewModel(vm: SelectBoardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PhotoSharedViewModel::class)
    abstract fun photoSharedViewModel(vm: PhotoSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecentPostsViewModel::class)
    abstract fun recentPostsViewModel(vm: RecentPostsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchResultsViewModel::class)
    abstract fun searchResultsViewModel(vm: SearchResultsViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MentionsViewModel::class)
    abstract fun mentionsViewModel(vm: MentionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyLikesScreenViewModel::class)
    abstract fun myLikesScreenViewModel(vm: MyLikesScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MySharedPostsViewModel::class)
    abstract fun mySharedPostsViewModel(vm: MySharedPostsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectedPageSharedViewModel::class)
    abstract fun selectedPageSharedViewModel(vm: SelectedPageSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserScreenViewModel::class)
    abstract fun userScreenViewModel(vm: UserScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowingViewModel::class)
    abstract fun followingViewModel(vm: FollowingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SharedWithMeViewModel::class)
    abstract fun sharedWithMeViewModel(vm: SharedWithMeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LikesAndSharesViewModel::class)
    abstract fun likesAndSharesViewModel(vm: LikesAndSharesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewPostScreenViewModel::class)
    abstract fun newPostScreenViewModel(vm: NewPostScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ImagePickerSharedViewModel::class)
    abstract fun imagePickerSharedViewModel(vm: ImagePickerSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewTopicScreenViewModel::class)
    abstract fun newTopicFormViewModel(vm: NewTopicScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ModifyPostScreenViewModel::class)
    abstract fun modifyPostScreenViewModel(vm: ModifyPostScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportPostSharedScreenViewModel::class)
    abstract fun reportPostScreenViewModel(vm: ReportPostSharedScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserPostsViewModel::class)
    abstract fun userPostsViewModel(vm: UserPostsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun searchViewModel(vm: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecentTopicsViewModel::class)
    abstract fun recentTopicsViewModel(vm: RecentTopicsViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun settingsViewModel(vm: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SimpleTopicListScreenViewModel::class)
    abstract fun simpleTopicListScreenViewModel(vm: SimpleTopicListScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MailUserScreenViewModel::class)
    abstract fun mailScreenViewModel(vm: MailUserScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditAccountScreenViewModel::class)
    abstract fun editAccountScreenViewModel(vm: EditAccountScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MuslimDeclarationScreenViewModel::class)
    abstract fun muslimDeclarationScreenViewModel(vm: MuslimDeclarationScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyFollowersScreenViewModel::class)
    abstract fun myFollowersScreenViewModel(vm: MyFollowersScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InboxScreenViewModel::class)
    abstract fun inboxScreenViewModel(vm: InboxScreenViewModel): ViewModel
}
