package com.amebo.amebo.screens.user

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.*
import com.amebo.amebo.common.extensions.*
import com.amebo.amebo.databinding.UserScreenBinding
import com.amebo.core.CoreUtils
import com.amebo.core.domain.Board
import com.amebo.core.domain.Topic
import com.amebo.core.domain.User
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

// TODO: THe logic here is messed up? Should do something about it... especially for image loading
class UserScreenView(
    private val pref: Pref,
    binding: UserScreenBinding,
    private val user: User,
    private val listener: Listener,
    scope: CoroutineScope
) : RequestListener<Drawable> {

    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!

    private val context get() = binding.root.context
    private val photoUrl get() = user.data?.image?.url
    private val progressDrawable = AppUtil.progressDrawable(binding.imageProgressView.context)

    private var job: Job? = null

    init {
        val hide = pref.isLoggedOut || pref.isCurrentAccount(user)
        binding.btnFollow.isGone = hide
        binding.btnSendMail.isGone = hide
        binding.btnEdit.isVisible = pref.isCurrentAccount(user)

        if (!listener.lastDisplayPhotoLoadSuccessful) {
            job = scope.launch {
                try {
                    Glide.with(binding.displayPhoto)
                        .load(AvatarGenerator.getForUser(binding.context, user.name))
                        .into(binding.displayPhoto)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance()
                        .log("Error AvatarGenerator for '${user.name}': $e")
                }
            }
        }

        binding.userName.text = user.name
        binding.collapsingToolbar.title = user.name
        binding.toolbar.setNavigationOnClickListener { listener.goBack() }
        binding.btnFollow.setOnClickListener {
            when (val data = user.data) {
                is User.Data -> {
                    data.isFollowing = !data.isFollowing
                    listener.onFollowClicked(data.isFollowing)
                    updateFollowing()
                }
            }
        }
        binding.displayPhoto.setOnClickListener {
            if (listener.lastDisplayPhotoLoadSuccessful) {
                listener.onDisplayPhotoClicked(photoUrl!!)
            }
        }
        binding.btnSendMail.setOnClickListener { listener.sendMail() }
        binding.txtSectionsActive.movementMethod = LinkMovementMethod.getInstance()
        binding.txtModeratesIn.movementMethod = LinkMovementMethod.getInstance()

        binding.toolbar.setOnClickListener {
            binding.appbarLayout.setExpanded(true, true)
        }

        binding.search.setOnClickListener { listener.showSearch() }
        binding.retry.setOnClickListener { listener.load() }
        binding.btnEdit.setOnClickListener {
            listener.onEditClicked()
        }
        binding.viewPosts.setOnClickListener {
            listener.onViewPostsClicked()
        }
        binding.viewTopics.setOnClickListener {
            listener.onViewTopicsClicked()
        }
    }

    private fun setRetryVisible(isVisible: Boolean) {
        binding.retry.isVisible = isVisible
    }

    private fun setToolbarProgress(isVisible: Boolean) {
        binding.toolbarProgress.isVisible = isVisible
    }

    fun onDataLoadProgress(loading: Resource.Loading<User.Data>) {
        val data = loading.content
        if (data == null) {
            setRetryVisible(false)
            setToolbarProgress(false)
            binding.stateLayout.loading()
        } else {
            user.data = data
            if (onDataAvailable()) {
                setRetryVisible(false)
                setToolbarProgress(true)
                binding.stateLayout.content()
            } else {
                setRetryVisible(false)
                setToolbarProgress(false)
                binding.stateLayout.loading()
            }
        }
    }

    fun onDataLoadSuccess(success: Resource.Success<User.Data>) {
        setRetryVisible(true)
        setToolbarProgress(false)
        user.data = success.content
        binding.btnFollow.isEnabled = true
        binding.btnSendMail.isEnabled = true
        binding.stateLayout.content()
        onDataAvailable()
    }

    fun onDataLoadError(error: Resource.Error<User.Data>) {
        setRetryVisible(true)
        setToolbarProgress(false)
        val data = error.content
        if (data == null) {
            binding.ouchView.setState(error.cause)
            binding.stateLayout.failure()
        } else {
            Snackbar.make(
                binding.coordinatorLayout,
                error.cause.getMessage(context),
                Snackbar.LENGTH_SHORT
            ).show()
            user.data = data
            if (onDataAvailable())
                binding.stateLayout.content()
            else
                binding.stateLayout.failure()
        }
    }

    fun onFollowUserProgress(loading: Resource.Loading<User.Data>) {
        // TODO
    }

    fun onFollowUserError(error: Resource.Error<User.Data>) {
        // TODO
    }

    fun onFollowUserSuccess(success: Resource.Success<User.Data>) {
        user.data = success.content
        onDataAvailable()
        snack(context.getString(R.string.now_following, user.name))
    }

    private fun snack(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    /**
     * @return true if body of screen has some content set (that is, any one of the child views in
     * R.id.content has an effective visibility of View.VISIBLE
     */
    private fun onDataAvailable(): Boolean {
        updateFollowing()
        updateGender()
        updateImage()
        updateLastSeen()
        return updateOthers()
    }


    private fun updateFollowing() {
        binding.btnFollow.setText(
            if (user.data?.isFollowing == true) {
                R.string.following
            } else {
                R.string.follow
            }
        )
    }

    private fun updateGender() {
        binding.userName.setDrawableEnd(
            user.genderDrawable,
            useLineHeight = true
        )
    }

    private fun updateImage() {
        when (val url = photoUrl) {
            is String -> {
                Glide.with(binding.displayPhoto)
                    .load(url)
                    .placeholder(progressDrawable)
                    .listener(this)
                    .into(binding.displayPhoto)
            }
        }
    }

    private fun updateLastSeen() {
        when (val timestamp = user.data?.lastSeen) {
            is Long -> {
                if (timestamp != -1L) {
                    binding.lastSeen.text =
                        context.getString(R.string.last_seen_x_ago, CoreUtils.howLongAgo(timestamp))
                }
            }
            else -> {
                binding.lastSeen.text = ""
            }
        }
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        job?.cancel()
        listener.lastDisplayPhotoLoadSuccessful = false
        progressDrawable.stop()

        binding.displayPhoto.post {
            Glide.with(binding.displayPhoto).clear(binding.displayPhoto)
        }
        binding.btnRetry.isVisible = true
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        job?.cancel()
        listener.lastDisplayPhotoLoadSuccessful = true
        progressDrawable.stop()
        return false
    }


    /**
     * @return true if some content was displayed
     */
    private fun updateOthers(): Boolean {
        val data = user.data
        val personalText = data?.personalText ?: ""
        val signature = data?.signature ?: ""
        val moderatesIn = data?.boardsModeratesIn ?: emptyList()
        val following = data?.following ?: emptyList()
        val activeIn = data?.boardsMostActiveIn ?: emptyList()
        val latestTopics = data?.latestTopics ?: emptyList()
        val topics = (data?.topicCount ?: 0) > 0
        val posts = (data?.postCount ?: 0) > 0
        val twitter = data?.twitter ?: ""
        val timeRegistered = data?.timeRegistered ?: -1
        val timeSpentOnline = data?.timeSpentOnline ?: ""

        val quotes = personalText.isNotBlank() ||
                signature.isNotBlank()
        val aboutMore = moderatesIn.isNotEmpty() ||
                following.isNotEmpty() ||
                activeIn.isNotEmpty() ||
                topics || posts
        val moreTimeStats = timeRegistered != -1L || timeSpentOnline.isNotBlank()


        binding.personalText.htmlFromText(personalText)
        binding.signature.htmlFromText(signature)

        binding.quotes.isVisible = quotes
        binding.signature.isVisible = signature.isNotBlank()
        binding.personalText.isVisible = personalText.isNotBlank()

        binding.aboutMore.isVisible = pref.isLoggedIn && aboutMore
        binding.txtModeratesIn.isVisible = moderatesIn.isNotEmpty()
        binding.txtModeratesIn.text = boardListToSpannable(
            context,
            moderatesIn,
            R.string.moderates_in,
            listener::onBoardClicked
        )
        binding.txtViewFollowers.isVisible = following.isNotEmpty()
        binding.txtSectionsActive.isVisible = activeIn.isNotEmpty()
        binding.txtSectionsActive.text = boardListToSpannable(
            context, activeIn, R.string.boards_most_active_in,
            listener::onBoardClicked
        )

        binding.txtViewFollowers.text = getFollowing(context, following)
        binding.txtViewFollowers.setOnClickListener {
            listener.visitFollowersListener(following)
        }
        binding.txtSectionsActive.setOnClickListener {

        }
        binding.viewlatestTopics.isVisible = latestTopics.isNotEmpty()
        binding.viewlatestTopics.setOnClickListener {
            listener.onLatestTopicsClicked(latestTopics)
        }
        binding.btnTwitter.isVisible = twitter.isNotBlank()
        binding.btnTwitter.setOnClickListener {
            listener.onTwitterClicked(twitter)
        }

        val ctx = binding.root.context
        binding.viewTopics.isVisible = topics
        binding.viewTopics.text = viewTopicsSpannable(ctx, data?.topicCount ?: 0)

        binding.viewPosts.isVisible = posts
        binding.viewPosts.text = viewPostsSpannable(ctx, data?.postCount ?: 0)

        binding.timeRegistered.isVisible = true
        binding.timeRegistered.text =
            ctx.getString(R.string.joined_x_date, CoreUtils.timeRegistered(timeRegistered))
        binding.timeSpentOnline.isVisible = timeSpentOnline.isNotBlank()
        binding.timeSpentOnline.text = ctx.getString(R.string.x_time_spent_online, timeSpentOnline)
        return quotes or aboutMore or moreTimeStats
    }

    interface Listener {
        fun load()
        fun onDisplayPhotoClicked(url: String)
        fun visitFollowersListener(followers: List<User>)
        fun onTwitterClicked(text: String)
        fun onFollowClicked(follow: Boolean)
        fun onLatestTopicsClicked(topics: List<Topic>)
        fun goBack()
        fun sendMail()
        fun onBoardClicked(board: Board)
        fun onEditClicked()
        fun onViewPostsClicked()
        fun onViewTopicsClicked()
        fun showSearch()
        var lastDisplayPhotoLoadSuccessful: Boolean
    }

    companion object {

        private fun getFollowing(context: Context, users: List<User>): SpannableStringBuilder {
            return getSpannableParagraph(context, R.string.following, formatUserList(users))
        }

        private fun viewTopicsSpannable(context: Context, topicCount: Int) =
            getViewCountSpan(context, R.string.view_topics, topicCount)

        private fun viewPostsSpannable(context: Context, topicCount: Int) =
            getViewCountSpan(context, R.string.view_posts, topicCount)

        private fun getViewCountSpan(
            context: Context,
            @StringRes titleRes: Int,
            count: Int
        ): Spannable {
            val title = context.getString(titleRes)
            val ssb = SpannableStringBuilder(title)
            ssb.append(" ").append("(${count})")
            ssb.setSpan(
                ForegroundColorSpan(context.asTheme().colorPrimary),
                title.length + 1, // account for spacing
                ssb.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return ssb
        }

        private fun boardListToSpannable(
            context: Context,
            boards: List<Board>,
            @StringRes titleRes: Int,
            onClick: (Board) -> Unit
        ): SpannableStringBuilder {
            val builder = SpannableStringBuilder(context.getString(titleRes)).append(
                HtmlCompat.fromHtml(
                    "<br>",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )
            boards.firstOrNull()?.let { board ->
                builder.append(getSpannable(context, board.name) { onClick(board) })
            }
            for (i in 1 until boards.size) {
                val board = boards[i]
                builder.append(", ")
                    .append(getSpannable(context, board.name) { onClick(board) })
            }
            return builder
        }

        private fun getSpannable(context: Context, text: String): SpannableStringBuilder {
            val theme = context.asTheme()
            val ssb = SpannableStringBuilder(fromHtml(text))
            ssb.setSpan(
                ForegroundColorSpan(theme.colorPrimary),
                0,
                ssb.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return ssb
        }

        private fun fromHtml(text: String) = HtmlCompat.fromHtml(
            text,
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )

        private fun getSpannable(
            context: Context,
            text: String,
            onClick: () -> Unit
        ): SpannableStringBuilder {
            val ssb = getSpannable(context, text)
            ssb.setSpan(
                CustomClickableSpan { onClick() },
                0,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return ssb
        }

        private fun getSpannableParagraph(
            context: Context,
            @StringRes titleRes: Int,
            body: String
        ): SpannableStringBuilder {
            val title = context.getString(titleRes)
            return SpannableStringBuilder(title)
                .append(fromHtml("<br>"))
                .append(getSpannable(context, body))
        }

        private fun formatUserList(users: List<User>) = when (users.size) {
            0 -> ""
            1 -> users.first().name
            2 -> users.first().name + " and " + users[1].name
            else -> users.first().name + " and " + (users.size - 1) + " others"
        }

    }
}