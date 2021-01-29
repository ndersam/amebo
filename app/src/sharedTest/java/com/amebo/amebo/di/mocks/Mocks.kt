package com.amebo.amebo.di.mocks

import androidx.lifecycle.MutableLiveData
import com.amebo.amebo.common.Event
import com.amebo.amebo.common.Optional
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.imagepicker.ImagePickerSharedViewModel
import com.amebo.amebo.screens.imagepicker.PostImagesUpdate
import com.amebo.amebo.screens.newpost.FormData
import com.amebo.amebo.screens.newpost.modifypost.ModifyPostScreenViewModel
import com.amebo.amebo.screens.newpost.newpost.NewPostScreenViewModel
import com.amebo.amebo.screens.newpost.newpost.NewPostView
import com.amebo.amebo.screens.postlist.PostListMeta
import com.amebo.amebo.screens.postlist.topic.TopicPostListView
import com.amebo.amebo.screens.postlist.topic.TopicViewModel
import com.amebo.amebo.suite.TestPref
import com.amebo.core.domain.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

object Mocks {

    object PrefModule {
        lateinit var pref: TestPref

        fun create(): Pref {
            pref = TestPref()
            return pref
        }
    }

    object UserManagement {
        lateinit var vm: UserManagementViewModel
            private set
        lateinit var userLoggedOutEvent: MutableLiveData<Event<Unit>>
            private set
        lateinit var accountListEvent: MutableLiveData<Event<List<UserAccount>>>
            private set
        lateinit var loginEvent: MutableLiveData<Event<Resource<Unit>>>
            private set
        lateinit var removeUserAccountEvent: MutableLiveData<Event<Pair<RealUserAccount, Boolean>>>
            private set
        lateinit var displayPhotoEvent: MutableLiveData<Event<Resource<DisplayPhoto>>>
            private set
        lateinit var sessionEvent: MutableLiveData<Session>
            private set

        fun new(): UserManagementViewModel {
            vm = mock()
            userLoggedOutEvent = MutableLiveData()
            accountListEvent = MutableLiveData()
            loginEvent = MutableLiveData()
            removeUserAccountEvent = MutableLiveData()
            displayPhotoEvent = MutableLiveData()
            sessionEvent = MutableLiveData()
            whenever(vm.userLoggedOutEvent).thenReturn(userLoggedOutEvent)
            whenever(vm.accountListEvent).thenReturn(accountListEvent)
            whenever(vm.loginEvent).thenReturn(loginEvent)
            whenever(vm.removeUserAccountEvent).thenReturn(removeUserAccountEvent)
            whenever(vm.displayPhotoEvent).thenReturn(displayPhotoEvent)
            whenever(vm.sessionEvent).thenReturn(sessionEvent)
            return vm
        }

    }

    @Module
    object NewPostScreen {
        lateinit var vm: NewPostScreenViewModel
            private set
        lateinit var formLoadingEvent: MutableLiveData<Event<Resource<FormData>>>
            private set
        lateinit var formSubmissionEvent: MutableLiveData<Event<Resource<PostListDataPage>>>
            private set
        lateinit var imageCountEvent: MutableLiveData<Event<Int>>
            private set
        lateinit var existingImageRemovalEvent: MutableLiveData<Event<Resource<ImageItem.Existing>>>
            private set
        lateinit var newPostView: NewPostView
            private set
        lateinit var getPostEvent: MutableLiveData<Event<Resource<String>>>
            private set
        lateinit var muslimDeclarationEvent: MutableLiveData<Event<AreYouMuslimDeclarationForm>>
            private set

        @JvmStatic
        @Provides
        fun createView(): NewPostView {
            newPostView = mock()
            return newPostView
        }

        fun createVM(): NewPostScreenViewModel {
            vm = mock()
            formLoadingEvent = MutableLiveData()
            formSubmissionEvent = MutableLiveData()
            imageCountEvent = MutableLiveData()
            existingImageRemovalEvent = MutableLiveData()
            getPostEvent = MutableLiveData()
            muslimDeclarationEvent = MutableLiveData()

            whenever(vm.formLoadingEvent).thenReturn(formLoadingEvent)
            whenever(vm.formSubmissionEvent).thenReturn(formSubmissionEvent)
            whenever(vm.imageCountEvent).thenReturn(imageCountEvent)
            whenever(vm.existingImageRemovalEvent).thenReturn(existingImageRemovalEvent)
            whenever(vm.getPostEvent).thenReturn(getPostEvent)
            whenever(vm.muslimDeclarationEvent).thenReturn(muslimDeclarationEvent)
            return vm
        }
    }

    object ImagePickerShared {
        lateinit var vm: ImagePickerSharedViewModel
            private set

        lateinit var imagesUpdatedEvent: MutableLiveData<Event<PostImagesUpdate>>
            private set

        fun new(): ImagePickerSharedViewModel {
            vm = mock()
            imagesUpdatedEvent = MutableLiveData()
            whenever(vm.imagesUpdatedEvent).thenReturn(imagesUpdatedEvent)
            return vm
        }
    }

    @Module
    object TopicScreen {
        lateinit var vm: TopicViewModel
            private set
        lateinit var dataEvent: MutableLiveData<Event<Resource<PostListDataPage>>>
            private set
        lateinit var metaEvent: MutableLiveData<Event<PostListMeta>>
            private set
        lateinit var unknownUriResultEvent: MutableLiveData<Event<Pair<String, Optional<IntentParseResult>>>>
            private set
        lateinit var addViewedTopicEvent: MutableLiveData<Event<Unit>>
            private set
        lateinit var view: TopicPostListView
            private set

        fun createVM(mockLiveData: Boolean = true): TopicViewModel {
            vm = mock()
            if (mockLiveData) {
                dataEvent = mock()
                metaEvent = mock()
                unknownUriResultEvent = mock()
                addViewedTopicEvent = mock()
            } else {
                dataEvent = MutableLiveData()
                metaEvent = MutableLiveData()
                unknownUriResultEvent = MutableLiveData()
                addViewedTopicEvent = MutableLiveData()
            }
            whenever(vm.dataEvent).thenReturn(dataEvent)
            whenever(vm.metaEvent).thenReturn(metaEvent)
            whenever(vm.unknownUriResultEvent).thenReturn(unknownUriResultEvent)
            whenever(vm.addViewedTopicEvent).thenReturn(addViewedTopicEvent)

            return vm
        }

        @JvmStatic
        @Provides
        fun createView(): TopicPostListView {
            view = mock()
            return view
        }
    }

    object ModifyPostScreen {
        lateinit var vm: ModifyPostScreenViewModel
            private set
        lateinit var formSubmissionEvent: MutableLiveData<Event<Resource<PostListDataPage>>>
            private set
        lateinit var formLoadingEvent: MutableLiveData<Event<Resource<FormData>>>
            private set
        lateinit var imageCountEvent: MutableLiveData<Event<Int>>
            private set
        lateinit var existingImageRemovalEvent: MutableLiveData<Event<Resource<ImageItem.Existing>>>
            private set

        fun createVM(mockLiveData: Boolean = true): ModifyPostScreenViewModel {
            vm = mock()
            if (mockLiveData) {
                formSubmissionEvent = mock()
                formLoadingEvent = mock()
                imageCountEvent = mock()
                existingImageRemovalEvent = mock()
            } else {
                formSubmissionEvent = MutableLiveData()
                formLoadingEvent = MutableLiveData()
                imageCountEvent = MutableLiveData()
                existingImageRemovalEvent = MutableLiveData()
            }
            whenever(vm.formLoadingEvent).thenReturn(formLoadingEvent)
            whenever(vm.formSubmissionEvent).thenReturn(formSubmissionEvent)
            whenever(vm.imageCountEvent).thenReturn(imageCountEvent)
            whenever(vm.existingImageRemovalEvent).thenReturn(existingImageRemovalEvent)
            return vm
        }
    }
}