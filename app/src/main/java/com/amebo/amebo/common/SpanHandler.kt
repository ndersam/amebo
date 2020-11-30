package com.amebo.amebo.common

import android.widget.TextView
import com.amebo.core.domain.Topic

interface SpanHandler {
    fun onImageClick(view: TextView, imagePosition: Int)
    fun onImageClick(view: TextView, url: String)
    fun onYoutubeLinkClick(view: TextView, url: String)
    fun onTopicLinkClick(view: TextView, topic: Topic)
    fun onReferencedPostClick(view: TextView, postID: String, author: String)
    fun onPostLinkClick(view: TextView, postID: String)
    fun onUnknownLinkClick(url: String)
}
