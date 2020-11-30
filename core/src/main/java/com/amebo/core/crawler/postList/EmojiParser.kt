package com.amebo.core.crawler.postList

import java.util.regex.Pattern

object EmojiParser {
    private val MOBILE_EMOJI_PATTERN =
        Pattern.compile("[\\u2190-\\u21FF]|[\\u2600-\\u26FF]|[\\u2700-\\u27BF]|[\\u3000-\\u303F]|[\\u1F300-\\u1F64F]|[\\u1F680-\\u1F6FF]")
    private val WEB_EMOJI_PATTERN = Pattern.compile("/faces/\\w+\\.\\w+$")
    private val SRC_PATTERN = Pattern.compile("src=\"(.*?)\"")

    private val WEB_TO_MOBILE = mutableMapOf<String, String>()
    private val MOBILE_TO_WEB = mutableMapOf<String, String>()

    fun isImageEmoji(imageTag: String): Boolean {
        val matcher = WEB_EMOJI_PATTERN.matcher(imageTag)
        return matcher.find()
    }

    init {
        WEB_TO_MOBILE["/faces/smiley.png"] = "\\u1F642"
        WEB_TO_MOBILE["/faces/wink.png"] = "\\u1F609"
        WEB_TO_MOBILE["/faces/cheesy.png"] = "\\u1F600"
        WEB_TO_MOBILE["/faces/grin.png"] = "\\u1F601"
        WEB_TO_MOBILE["/faces/angry.png"] = "\\u1F620"
        WEB_TO_MOBILE["/faces/sad.png"] = "\\u1F61E"
        WEB_TO_MOBILE["/faces/shocked.gif"] = "\\u1F632"
        WEB_TO_MOBILE["/faces/cool.png"] = "\\u1F60E"
        WEB_TO_MOBILE["/faces/huh.png"] = "\\u1F62F"
        WEB_TO_MOBILE["/faces/tongue.png"] = "\\u1F61B"
        WEB_TO_MOBILE["/faces/embarassed.png"] = "\\u1F633"
        WEB_TO_MOBILE["/faces/lipsrsealed.png"] = "\\u1F910"
        WEB_TO_MOBILE["/faces/undecided.png"] = "\\u1F615"
        WEB_TO_MOBILE["/faces/kiss.gif"] = "\\u1F618"
        WEB_TO_MOBILE["/faces/cry.gif"] = "\\u1F622"
    }

    init {
        MOBILE_TO_WEB[":)"] = "\\u1F600"
        MOBILE_TO_WEB[";)"] = "\\u1F609"
        MOBILE_TO_WEB[":D"] = "\\u1F601"
        MOBILE_TO_WEB[">:("] = "\\u1F620"
        MOBILE_TO_WEB[":("] = "\\u1F61E"
        MOBILE_TO_WEB[":o"] = "\\u1F632"
        MOBILE_TO_WEB["8)"] = "\\u1F60E"
        MOBILE_TO_WEB["???"] = "\\u1F62F"
        MOBILE_TO_WEB[":P"] = "\\u1F61B"
    }
}