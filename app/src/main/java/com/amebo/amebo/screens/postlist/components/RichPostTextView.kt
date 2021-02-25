package com.amebo.amebo.screens.postlist.components

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.common.EmojiGetter
import com.amebo.amebo.common.SpanHandler
import com.amebo.amebo.common.extensions.htmlFromPostForRichPost
import com.amebo.amebo.common.extensions.quotedPostRanges
import com.amebo.amebo.databinding.ItemRichPostTextViewTextBinding
import com.amebo.amebo.databinding.ItemYoutubeViewBinding
import com.amebo.core.domain.SimplePost
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class RichPostTextView : RecyclerView {


    private lateinit var itemAdapter: ItemAdapter


    fun setData(
        lifecycle: Lifecycle,
        post: SimplePost,
        useDeviceEmojis: Boolean,
        handler: SpanHandler
    ) {
        itemAdapter.lifecycle = lifecycle
        itemAdapter.handler = handler
        val res = getItems(post, useDeviceEmojis)
        itemAdapter.quotedPosts = post.parentQuotes
        itemAdapter.items = res.second
        itemAdapter.notifyDataSetChanged()
        setHasFixedSize(true)
    }

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize()
    }

    private fun initialize() {
        itemAdapter = ItemAdapter()
        this.adapter = itemAdapter
    }

    private class ItemAdapter(
        var items: List<Item> = emptyList(),
        var quotedPosts: List<String> = emptyList(),
        var handler: SpanHandler? = null,
        var lifecycle: Lifecycle? = null,
    ) :
        Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                R.layout.item_rich_post_text_view_text ->
                    TextVH(
                        ItemRichPostTextViewTextBinding.inflate(inflater, parent, false)
                    )
                R.layout.fragment_youtube_screen -> VideoVH(
                    ItemYoutubeViewBinding.inflate(inflater, parent, false),
                    lifecycle!!
                )
                else -> throw IllegalArgumentException("Unknown viewType '$viewType'")
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (val item = items[position]) {
                is Item.Text -> {
                    val vh = holder as TextVH
                    vh.bind(item, handler!!, quotedPosts)
                }
                is Item.Video -> {
                    val vh = holder as VideoVH
                    vh.bind(AppUtil.parseYoutubeUrl(item.url) ?: run {
                        FirebaseCrashlytics.getInstance()
                            .log("AppUtil.parseYoutubeUrl returns null, url=${item.url}")
                        return
                    })
                }
            }
        }

        override fun getItemCount(): Int = items.size


        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is Item.Text -> R.layout.item_rich_post_text_view_text
                is Item.Video -> R.layout.fragment_youtube_screen
            }
        }

        private class TextVH(private val binding: ItemRichPostTextViewTextBinding) :
            ViewHolder(binding.root) {

            init {
                binding.root.movementMethod = LinkMovementMethod.getInstance()
                binding.root.setTextIsSelectable(true)
                binding.root.isFocusable = true
                binding.root.isLongClickable = true
                binding.root.isFocusableInTouchMode = true
                binding.root.customSelectionActionModeCallback =
                    DefaultActionModeCallback(DefaultActionModeListenerImpl(binding.root))
            }

            fun bind(
                item: Item.Text,
                handler: SpanHandler,
                quotedPost: List<String>
            ) {
                binding.root.htmlFromPostForRichPost(item.text, quotedPost, handler)
            }
        }

        private class VideoVH(binding: ItemYoutubeViewBinding, lifecycle: Lifecycle) :
            ViewHolder(binding.root) {

            private var videoID: String? = null
            private var player: YouTubePlayer? = null


            init {
                lifecycle.addObserver(binding.player)
                binding.player.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // binding.player.exitFullScreen()
                        player = youTubePlayer
                        if (videoID != null) {
                            youTubePlayer.cueVideo(videoID!!, 0f)
                        }
                    }
                })
            }

            fun bind(videoID: String) {
                this.videoID = videoID
                player?.cueVideo(this.videoID!!, 0f)
            }
        }
    }

    companion object {

        private fun getItems(
            post: SimplePost,
            useDeviceEmojis: Boolean
        ): Pair<List<Pair<Int, Int>>, List<Item>> {
            val html = if (useDeviceEmojis)
                EmojiGetter.emojify(post.text)
            else post.text
            val spanned = SpannableStringBuilder(
                HtmlCompat.fromHtml(
                    html,
                    HtmlCompat.FROM_HTML_MODE_COMPACT,
                    null, null
                )
            )
            return spanned.quotedPostRanges(post) to spanned.items()
        }

        private fun Spannable.items(): List<Item> {
            val items = mutableListOf<Item>()
            var lastIndex = 0
            getSpans(
                0, length,
                URLSpan::class.java
            ).forEachIndexed { _, span ->
                val start = getSpanStart(span)
                val end = getSpanEnd(span)
                val youtube = AppUtil.parseYoutubeUrl(span.url)
                if (youtube != null) {
                    removeSpan(span)
                    if (start - lastIndex > 0) {
                        items.add(Item.Text(sub(lastIndex, start), lastIndex, start))
                    }
                    items.add(Item.Video(substring(start, end), start, end))
                    lastIndex = end
                }
            }
            if (length - lastIndex > 0) {
                items.add(Item.Text(sub(lastIndex), lastIndex, length))
            }
            return items
        }

        private fun Spannable.sub(start: Int, end: Int = length) = HtmlCompat.toHtml(
            subSequence(start, end) as Spanned,
            HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL
        )

        private sealed class Item(val start: Int, val end: Int) {
            class Text(val text: String, start: Int, end: Int) : Item(start, end)
            class Video(val url: String, start: Int, end: Int) : Item(start, end)
        }
    }
}