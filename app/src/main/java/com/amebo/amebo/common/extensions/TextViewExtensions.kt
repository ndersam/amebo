package com.amebo.amebo.common.extensions

import android.app.Activity
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.widget.TextViewCompat
import com.amebo.amebo.common.HTMLBuilder
import com.amebo.amebo.common.SpanHandler
import com.amebo.amebo.common.asTheme
import com.amebo.core.domain.Board
import com.amebo.core.domain.SimplePost


fun TextView.setDrawableColor(@ColorInt color: Int) {
    var drawables = TextViewCompat.getCompoundDrawablesRelative(this)
    drawables += compoundDrawables
    for (drawable in drawables) {
        drawable?.setColor(color)
    }
}

fun TextView.getDrawableStart(): Drawable? {
    return TextViewCompat.getCompoundDrawablesRelative(this)[0]
}

fun TextView.getDrawableTop(): Drawable? {
    return TextViewCompat.getCompoundDrawablesRelative(this)[1]
}

fun TextView.getDrawableEnd(): Drawable? {
    return TextViewCompat.getCompoundDrawablesRelative(this)[2]
}

fun TextView.getDrawableBottom(): Drawable? {
    return TextViewCompat.getCompoundDrawablesRelative(this)[3]
}

fun TextView.setDrawableStart(@DrawableRes drawableRes: Int?) {
    setCompoundDrawablesWithIntrinsicBounds(
        if (drawableRes == null) null else context.getDrawable(drawableRes),
        getDrawableTop(),
        getDrawableEnd(),
        getDrawableBottom()
    )
}

fun TextView.setDrawableEnd(
    @DrawableRes drawableRes: Int?,
    useLineHeight: Boolean = false,
    @ColorInt color: Int? = null
) {
    val drawable = if (drawableRes == null)
        null
    else context.getDrawable(drawableRes)?.apply {
        if (useLineHeight) {
            setBounds(0, 0, lineHeight, lineHeight)
        } else {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
        if (color != null) setColor(color)
    }
    setCompoundDrawables(
        getDrawableStart(),
        getDrawableTop(),
        drawable,
        getDrawableEnd()
    )
}


fun TextView.htmlFromText(text: String) {
    val theme = context.asTheme()
    HTMLBuilder(text, this)
        .blockQuotes(backgroundColor = theme.colorBackground, stripeColor = theme.colorAccent)
        .build()
}

fun TextView.htmlFromBoardInfo(text: String, onBoardClick: (Board) -> Unit) {
    val theme = context.asTheme()
    HTMLBuilder(text, this)
        .blockQuotes(backgroundColor = theme.colorBackground, stripeColor = theme.colorAccent)
        .board(onBoardClick)
        .build()
}


fun TextView.htmlFromPostForRichPost(
    text: String,
    quotedPosts: List<String>,
    handler: SpanHandler,
) {
    val theme = context.asTheme()
    HTMLBuilder(text, this)
        .blockQuotes(backgroundColor = theme.colorBackground, stripeColor = theme.colorAccent)
        .topic(handler::onTopicLinkClick)
        .referencedPost(quotedPosts, handler::onPostLinkClick, handler::onReferencedPostClick)
        .images(handler::onImageClick)
        .unknownUrl(handler::onUnknownLinkClick)
        .build()
}

fun Spanned.quotedPostRanges(post: SimplePost): List<Pair<Int, Int>> {
    val items = mutableListOf<Pair<Int, Int>>()
    getSpans(
        0, length,
        URLSpan::class.java
    ).forEachIndexed { index, span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)

        // Is quoted post
        post.let {
            if (post.parentQuotes.contains(index)) {
                items.add(start to end)
            }
        }
    }
    return items
}

/**
 * [@see https://stackoverflow.com/questions/11905486/how-get-coordinate-of-a-clickablespan-inside-a-textview]
 */
fun TextView.getSpanCoordinates(span: Any, activity: Activity): Rect {
    var rect = Rect()
    val spannable = text as SpannableString

    val start = spannable.getSpanStart(span)
    val end = spannable.getSpanEnd(span)
    var xStart = layout.getPrimaryHorizontal(start)
    var xEnd = layout.getPrimaryHorizontal(end)

    // get the rectangle of the clicked text
    val lineStart = layout.getLineForOffset(start)
    val lineEnd = layout.getLineForOffset(end)
    val isInMultiline = lineStart != lineEnd
    getLineBounds(lineStart, rect)

    // Update the rectangle position to his real position on screen
    val textViewLocation = intArrayOf(0, 0)
    getLocationOnScreen(textViewLocation)
    // textView top and bottom offset
    val topAndBottomOffset = textViewLocation[1] - scrollY + compoundPaddingTop
    rect.top += topAndBottomOffset
    rect.bottom += topAndBottomOffset

    // In the case of multiLine text, we have to choose what rectangle to take
    if (isInMultiline) {
        val screenHeight = activity.windowManager.defaultDisplay.height
        val dyTop = rect.top
        val dyBottom = screenHeight - rect.bottom
        val onTop = dyTop > dyBottom

        if (onTop) {
            xEnd = layout.getLineRight(lineStart)
        } else {
            rect = Rect()
            layout.getLineBounds(lineEnd, rect)
            rect.top += topAndBottomOffset
            rect.bottom += topAndBottomOffset
            xStart = layout.getLineLeft(lineEnd)
        }
    }

    rect.left = (textViewLocation[0] + xStart + compoundPaddingLeft - scrollX).toInt()
    rect.right = (rect.left + xEnd - xStart).toInt()
    return rect
}