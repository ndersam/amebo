package com.amebo.amebo.common

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.QuoteSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.text.HtmlCompat
import com.amebo.core.CoreUtils
import com.amebo.core.domain.Board
import com.amebo.core.domain.Topic

class HTMLBuilder(text: String, private val textView: TextView? = null) {
    private val imageGetterState = ImageGetterState()
    private val imageGetter =
        if (textView == null) null else ImageGetter(textView, imageGetterState)
    private val spannable = SpannableStringBuilder(
        HtmlCompat.fromHtml(
            text,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            imageGetter,
            null
        )
    )

    private var onYoutubeLinkClicked: ((TextView, videoId: String) -> Unit)? = null
    private var refPostHandler: ReferencedPostHandler? = null
    private var onImageClick: ((TextView, url: String) -> Unit)? = null
    private var onTopicLinkClick: ((TextView, Topic) -> Unit)? = null
    private var blockQuoteStyle: BlockQuoteStyle? = null
    private var onBoardClick: ((Board) -> Unit)? = null
    private var onUnknownUrlClick: ((url: String) -> Unit)? = null


    /**
     * Replace the default [QuoteSpan]
     */
    fun blockQuotes(
        @ColorInt
        backgroundColor: Int,
        @ColorInt
        stripeColor: Int,
        stripeWidth: Int = 4,
        gapWidth: Int = 20
    ): HTMLBuilder {
        this.blockQuoteStyle = BlockQuoteStyle(backgroundColor, stripeColor, stripeWidth, gapWidth)
        return this
    }

    /**
     * Attach click listener to [URLSpan]s whose links are valid Nairaland post urls
     */
    fun referencedPost(
        quotedPosts: List<String>,
        onClick: (TextView, postId: String) -> Unit,
        onReferencedClick: (TextView, postId: String, username: String) -> Unit
    ): HTMLBuilder {
        refPostHandler = ReferencedPostHandler(quotedPosts, onReferencedClick, onClick)
        return this
    }

    fun topic(onTopicClick: (TextView, Topic) -> Unit): HTMLBuilder {
        this.onTopicLinkClick = onTopicClick
        return this
    }

    /**
     * @param onYoutubeLinkClick - Listener that takes in [TextView] and  a [String] - the Youtube video id
     */
    fun youTube(onYoutubeLinkClick: (TextView, String) -> Unit): HTMLBuilder {
        this.onYoutubeLinkClicked = onYoutubeLinkClick
        return this
    }

    /**
     * @param onImageClick - Listener that takes in [TextView] and  a [String] - the image web url
     */
    fun images(onImageClick: (TextView, String) -> Unit): HTMLBuilder {
        this.onImageClick = onImageClick
        return this
    }

    /**
     * Use this when the text passed into the constructor contains board information only.
     */
    fun board(onBoardClick: (Board) -> Unit): HTMLBuilder {
        this.onBoardClick = onBoardClick
        return this
    }

    fun unknownUrl(onUnknownUrlClick: (url: String) -> Unit): HTMLBuilder {
        this.onUnknownUrlClick = onUnknownUrlClick
        return this
    }

    fun build(): Spanned {
        onQuoteSpans()
        onURLSpans()
        onImageSpans()
        textView?.setText(spannable, TextView.BufferType.SPANNABLE)
        return spannable
    }

    private fun onURLSpans() {
        spannable.getSpans(
            0, spannable.length,
            URLSpan::class.java
        ).forEachIndexed { _, span ->
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            val text = spannable.substring(start until end)
            val url: String = span.url

            // Is quoted post
            refPostHandler?.let { handler ->
                val postId = CoreUtils.postID(url)
                if (postId != null) {
                    spannable.removeSpan(span)
                    // FIXME:
                    when {
                        // if URLSpan text is also a postId then we treat the span as a regular
                        // post link (and not a mention or quoted post)
                        CoreUtils.postID(text) is String ||
                                handler.quotedPosts.contains(url).not() -> {
                            val newSpan = object : ClickableSpan() {
                                override fun onClick(widget: View) =
                                    handler.onPostLinkClick(widget as TextView, postId)
                            }
                            spannable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        else -> {
                            val newSpan = CustomClickableSpan(useLinkColor = true) {
                                handler.onReferencedClick(it as TextView, postId, text)
                            }
                            spannable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                    return@forEachIndexed
                }
            }


            // Is youtube url?
            onYoutubeLinkClicked?.let { callback ->
                val youtube = AppUtil.parseYoutubeUrl(url)
                if (youtube != null) {
                    spannable.removeSpan(span)
                    val newSpan = object : ClickableSpan() {
                        override fun onClick(p0: View) = callback(p0 as TextView, youtube)
                    }
                    spannable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    return@forEachIndexed
                }
            }


            // Is nairaland link
            onTopicLinkClick?.let { callback ->
                val topic = CoreUtils.topicUrl(url)
                if (topic != null) {
                    spannable.removeSpan(span)
                    val newSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) = callback(widget as TextView, topic)
                    }
                    spannable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    return@forEachIndexed
                }
            }

            //  board
            onBoardClick?.let { callback ->
                val boardSlug = url.substringAfter("/")
                val boardName = spannable.subSequence(start, end).toString()
                val board = Board(boardName, boardSlug)

                spannable.removeSpan(span)
                val newSpan = object : ClickableSpan() {
                    override fun onClick(p0: View) = callback(board)
                }
                spannable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return@forEachIndexed
            }

            // handle unknown Url
            onUnknownUrlClick?.let { callback ->
                spannable.removeSpan(span)
                val newSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) = callback(url)
                }
                spannable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun onImageSpans() {
        onImageClick?.let { callback ->
            spannable.getSpans(0, spannable.length, ImageSpan::class.java)
                .forEachIndexed { _, imageSpan ->
                    val start = spannable.getSpanStart(imageSpan)
                    val end = spannable.getSpanEnd(imageSpan)
                    spannable.getSpans(start, end, ClickableSpan::class.java).forEach {
                        spannable.removeSpan(it)
                    }
                    val newSpan = object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            val source = imageSpan.source!!
                            when (imageGetterState[source]) {
                                ImageFetchState.Success, null -> {
                                    callback(p0 as TextView, source)
                                }
                                ImageFetchState.Error -> {
                                    imageGetter?.getDrawable(source)
                                }
                                ImageFetchState.Loading -> {
                                }
                            }
                        }
                    }
                    spannable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
        }

    }

    private fun onQuoteSpans() {
        blockQuoteStyle?.apply {
            spannable.getSpans(
                0, spannable.length,
                QuoteSpan::class.java
            ).forEach {
                val start = spannable.getSpanStart(it)
                val end = spannable.getSpanEnd(it)
                val flags = spannable.getSpanFlags(it)
                spannable.removeSpan(it)
                spannable.setSpan(
                    CustomQuoteSpan(
                        backgroundColor,
                        stripeColor,
                        stripeWidth.toFloat(),
                        gapWidth.toFloat()
                    ),
                    start,
                    end,
                    flags
                )
            }
        }
    }

    private class ReferencedPostHandler(
        val quotedPosts: List<String>,
        val onReferencedClick: (TextView, String, String) -> Unit,
        val onPostLinkClick: (TextView, String) -> Unit
    )

    private class BlockQuoteStyle(
        @ColorInt
        val backgroundColor: Int,
        @ColorInt
        val stripeColor: Int,
        val stripeWidth: Int = 4,
        val gapWidth: Int = 20
    )
}