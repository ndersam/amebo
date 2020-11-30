package com.amebo.amebo.screens.leftdrawer

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import com.amebo.amebo.R
import com.amebo.amebo.common.Badge
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.routing.TabItem
import com.amebo.core.domain.Session
import com.mikepenz.fastadapter.expandable.getExpandableExtension
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.updateItem
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*


class DrawerLayoutView(
    username: String,
    isLoggedIn: Boolean,
    binding: MaterialDrawerSliderView
) {

    constructor(pref: Pref, binding: MaterialDrawerSliderView) : this(
        pref.userDisplayName(binding.context),
        pref.isLoggedIn,
        binding
    )

    var listener: Listener? = null
    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!

    private val drawerLayoutRef = WeakReference(binding.parent as DrawerLayout)
    val drawerLayout get() = drawerLayoutRef.get()!!

    private val navigationViewRef = WeakReference(binding)
    private val navigationView get() = navigationViewRef.get()!!
    private val badge = Badge(binding.context)

    private var currentItem: TabItem? = null


    init {
        initializeDrawerLayout(username, isLoggedIn)
    }


    fun restoreSavedState(savedState: Bundle?) {
        binding.setSavedInstance(savedState)
    }

    fun saveInstanceState(outState: Bundle) {
        binding.saveInstanceState(outState)
    }

    fun setSelection(item: TabItem) = binding.setSelection(item.id)


    fun setNotification(session: Session) {
        val hasNotifications =
            session.mentions > 0 || session.likesAndShares > 0 || session.sharedWithMe > 0 || session.following > 0
        if (hasNotifications)
            badge.setCount("  ")
        else
            badge.setCount("")
        setNotification(TabItem.Inbox, session.mailNotificationForm?.senders?.size ?: 0)
        setNotification(TabItem.Mentions, session.mentions)
        setNotification(TabItem.LikesAndShares, session.likesAndShares)
        setNotification(TabItem.SharedWithMe, session.sharedWithMe)
        setNotification(TabItem.Following, session.following)
    }

    fun setDisplayPhoto(displayPhotoLink: String) {
        val slider = bindingRef.get() ?: return
        val item = slider.accountHeader!!.profiles!!.first()
        item.iconUrl = displayPhotoLink
        slider.accountHeader?.updateProfile(item)
    }

    fun setDisplayPhoto(bitmap: Bitmap) {
        val slider = bindingRef.get() ?: return
        val item = slider.accountHeader!!.profiles!!.first()
        item.iconBitmap = bitmap
        slider.accountHeader?.updateProfile(item)
    }


    private fun initializeDrawerLayout(username: String, isLoggedIn: Boolean) {
        binding.initHeader(username, isLoggedIn)

        binding.initBody(
            TabItem.Topics,
            TabItem.RecentPosts
        )
        binding.initGoToProfile()
        if (isLoggedIn) {
            binding.initBody(null)
            binding.initBody(
                TabItem.Profile,
                TabItem.Inbox,
                TabItem.Mentions,
                TabItem.LikesAndShares,
                TabItem.SharedWithMe,
                TabItem.Following
            )
            binding.initBody(null)
            binding.initBody(
                TabItem.MyLikes,
                TabItem.MyShares,
                TabItem.MyFollowers
            )
        }
        binding.initBody(null)
        binding.initFooter()

        binding.onItemSelected(::onItemSelected)
        binding.onImageClicked {
            listener?.onImageClicked()
        }
        binding.onHeaderClicked {
            listener?.onHeaderClicked()
        }
    }


    private fun onItemSelected(destination: Destination) {
        when (destination) {
            is Destination.Settings -> listener?.onSettingsClicked()
            is Destination.GoToProfile -> {
                listener?.onGoToProfileClicked()
            }
            is Destination.Drawer -> {
                if (currentItem != destination) {
                    listener?.onItemSelected(destination.item)
                }
                currentItem = destination.item
                drawerLayout.closeDrawer(navigationView)
            }
        }
    }

    private fun setNotification(tabItem: TabItem, count: Int) {
        val item = badgeableDrawerItem(tabItem) ?: return
        if (count > 0) {
            item.badgeText = count.toString()
        } else {
            item.badge = null
        }
        binding.updateItem(item)
    }


    private fun badgeableDrawerItem(tabItem: TabItem) =
        binding.itemAdapter.itemList.items
            .firstOrNull { it.identifier == tabItem.id } as? AbstractBadgeableDrawerItem<*>

    interface Listener {
        fun onItemSelected(item: TabItem)
        fun onSettingsClicked()
        fun onGoToProfileClicked()
        fun onImageClicked()
        fun onHeaderClicked()
    }

    companion object {
        private const val SETTINGS_IDENTIFIER = -90L
        private const val GO_TO_PROFILE_IDENTIFIER = -100L
        private const val PROFILE_IDENTIFIER = 200L


        /**
         * @param items - null values indicate dividerItems
         */
        private fun MaterialDrawerSliderView.initBody(vararg items: TabItem?) {
            items.forEach { item ->
                when (item) {
                    is TabItem -> {
                        itemAdapter.add(
                            PrimaryDrawerItem().apply {
                                identifier = item.id
                                nameRes = item.titleRes
                                iconRes = item.drawableRes
                                isIconTinted = true
                                badgeStyle = BadgeStyle().apply {
                                    this.badgeBackground =
                                        context.getDrawable(R.drawable.notification_badge)
                                    this.textColorStateList = ColorStateList.valueOf(Color.WHITE)
                                }
                            }
                        )
                    }
                    null -> {
                        itemAdapter.add(DividerDrawerItem())
                    }
                }
            }
        }

        /**
         * @param items - null values indicate dividerItems
         */
        private fun MaterialDrawerSliderView.initBody(vararg items: Pair<TabItem, Array<TabItem>>) {
            items.forEach { pair ->
                val (item, childItems) = pair
                val expandableDrawerItem = CustomExpandableDrawerItem().apply {
                    identifier = item.id
                    nameRes = item.titleRes
                    iconRes = item.drawableRes
                    isIconTinted = true
                    onArrowClickListener = {
                        onExpandableItemArrowClicked(this@initBody, this)
                    }
                }
                itemAdapter.add(expandableDrawerItem)
                childItems.forEach { childItem ->
                    expandableDrawerItem.subItems.add(
                        SecondaryDrawerItem().apply {
                            identifier = childItem.id
                            nameRes = childItem.titleRes
                            iconRes = android.R.color.transparent
                            isIconTinted = true
                            badgeStyle = BadgeStyle().apply {
                                badgeBackground =
                                    context.getDrawable(R.drawable.notification_badge)
                            }
                        }
                    )
                }
            }
        }

        private fun onExpandableItemArrowClicked(
            slider: MaterialDrawerSliderView,
            item: ExpandableDrawerItem
        ) {
            val pos = slider.adapter.getPosition(item)
            slider.adapter.getExpandableExtension().toggleExpandable(pos)
        }

        private fun MaterialDrawerSliderView.initGoToProfile() {
            itemAdapter.add(PrimaryDrawerItem().apply {
                identifier = GO_TO_PROFILE_IDENTIFIER
                nameRes = R.string.go_to_profile
                iconRes = R.drawable.ic_person_24dp
                isSelectable = false
            })
        }

        private fun MaterialDrawerSliderView.initFooter() {
            itemAdapter.add(PrimaryDrawerItem().apply {
                identifier = SETTINGS_IDENTIFIER
                nameRes = R.string.settings
                iconRes = R.drawable.ic_settings_24dp
                isSelectable = false
            })
        }

        private fun MaterialDrawerSliderView.initHeader(username: String, isLoggedIn: Boolean) {
            accountHeader = AccountHeaderView(context).apply {
                attachToSliderView(this@initHeader)
                addProfiles(
                    ProfileDrawerItem().apply {
                        nameText = username
                        identifier = PROFILE_IDENTIFIER
                        descriptionText = if (isLoggedIn) {
                            "/${username.toLowerCase(Locale.ENGLISH)}"
                        } else {
                            "logged out"
                        }
                    }

                )
            }
        }

        private fun MaterialDrawerSliderView.onImageClicked(listener: () -> Unit) {
            accountHeader!!.onAccountHeaderProfileImageListener = { _, _, _ -> listener(); true }
        }

        private fun MaterialDrawerSliderView.onHeaderClicked(listener: () -> Unit) {
            accountHeader!!.onAccountHeaderSelectionViewClickListener = { _, _ -> listener(); true }
        }

        private fun MaterialDrawerSliderView.onItemSelected(listener: (Destination) -> Unit) {
            onDrawerItemClickListener = { _, drawerItem, _ ->
                Timber.d("Destination: ${drawerItem.identifier}")
                listener(drawerItemToDestination(drawerItem.identifier))
                true
            }
        }

        /**
         * @return null if user clicked Settings Item otherwise returns [TabItem] of
         * the [IDrawerItem] selected
         */
        private fun drawerItemToDestination(identifier: Long): Destination {
            // FIXME
            val drawerItem = when (identifier) {
                TabItem.Following.id -> TabItem.Following
                TabItem.Mentions.id -> TabItem.Mentions
                TabItem.LikesAndShares.id -> TabItem.LikesAndShares
                TabItem.Profile.id -> TabItem.Profile
                TabItem.Topics.id -> TabItem.Topics
                TabItem.SharedWithMe.id -> TabItem.SharedWithMe
                TabItem.RecentPosts.id -> TabItem.RecentPosts
                TabItem.MyShares.id -> TabItem.MyShares
                TabItem.MyLikes.id -> TabItem.MyLikes
                TabItem.MyFollowers.id -> TabItem.MyFollowers
                TabItem.Inbox.id -> TabItem.Inbox
                else -> null
            }
            if (drawerItem != null) return Destination.Drawer(drawerItem)
            return when (identifier) {
                SETTINGS_IDENTIFIER -> Destination.Settings
                GO_TO_PROFILE_IDENTIFIER -> Destination.GoToProfile
                else -> throw IllegalArgumentException("Unknown identifier $identifier")
            }
        }
    }

    private sealed class Destination {
        class Drawer(val item: TabItem) : Destination()
        object Settings : Destination()
        object GoToProfile : Destination()
    }
}