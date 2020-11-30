package com.amebo.amebo.common

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.amebo.amebo.R
import com.amebo.core.Values
import java.util.regex.Pattern


object EmojiGetter {
    private val URL_TO_DRAWABLES = mutableMapOf<String, Int>()
    private val URL_TO_DEVICE_EMOTICONS = mutableMapOf<String, String>()
    private val URL_TO_ASCII = mutableMapOf<String, String>()
    private val PATTERN_IMG_SRC = Pattern.compile("<img.+?(/faces/\\S*)\"\\s*[^>]*>")
    private val EMOJI_URL_RE = Pattern.compile("(/faces/.*$)")
    private val EMOJI_IMG_TAG_RE = Pattern.compile("<img.+?(/faces/\\S*)\"\\s*[^>]*>")
    val EMOTICONS: List<Emoticon>

    private fun valueOf(
        url: String,
        drawableRes: Int,
        unicode: Int,
        ascii: String
    ): Emoticon {
        return Emoticon(url, drawableRes, unicode, ascii)
    }

    init {
        EMOTICONS = listOf(
            valueOf(
                "/faces/smiley.png",
                R.drawable.smiley,
                0x1F642,
                ":)"
            ),
            valueOf(
                "/faces/wink.png",
                R.drawable.wink,
                0x1F609,
                ";)"
            ),
            valueOf(
                "/faces/cheesy.png",
                R.drawable.cheesy,
                0x1F600,
                ":D"
            ),
            valueOf(
                "/faces/grin.png",
                R.drawable.grin,
                0x1F601,
                ";D"
            ),
            valueOf(
                "/faces/angry.png",
                R.drawable.angry,
                0x1F620,
                ">:("
            ),
            valueOf(
                "/faces/sad.png",
                R.drawable.sad,
                0x1F61E,
                ":("
            ),
            valueOf(
                "/faces/shocked.gif",
                R.drawable.shocked,
                0x1F632,
                ":o"
            ),
            valueOf(
                "/faces/cool.png",
                R.drawable.cool,
                0x1F60E,
                "8)"
            ),
            valueOf(
                "/faces/huh.png",
                R.drawable.huh,
                0x1F62F,
                "???"
            ),
            valueOf(
                "/faces/tongue.png",
                R.drawable.tongue,
                0x1F61B,
                ":P"
            ),
            valueOf(
                "/faces/embarassed.png",
                R.drawable.embarassed,
                0x1F633,
                ":-["
            ),
            valueOf(
                "/faces/lipsrsealed.png",
                R.drawable.lipsrsealed,
                0x1F910,
                ":-X"
            ),
            valueOf(
                "/faces/undecided.png",
                R.drawable.undecided,
                0x1F615,
                ":-\\"
            ),
            valueOf(
                "/faces/kiss.gif",
                R.drawable.kiss,
                0x1F618,
                ":-*"
            ),
            valueOf(
                "/faces/cry.gif",
                R.drawable.cry,
                0x1F622,
                ":'("
            )
        )
        EMOTICONS.forEach { emoticon ->
            URL_TO_DRAWABLES[emoticon.url] =
                emoticon.drawableRes
            URL_TO_DEVICE_EMOTICONS[emoticon.url] = emoticon.unicode
            URL_TO_ASCII[emoticon.url] = emoticon.ascii
        }
    }

    @JvmStatic
    fun getDrawable(context: Context, source: String): Drawable? {
        val matcher = EMOJI_URL_RE.matcher(source)
        if (matcher.find()) {
            // Some nairaland images are no longer found
            // e.g. https://www.nairaland.com/faces/rolleyes.png
            val id = URL_TO_DRAWABLES[matcher.group(1)!!] ?: return null
            val drawable = ContextCompat.getDrawable(context, id)!!
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            return drawable
        }
        return null
    }

    fun isImageEmoji(imageTag: String): Boolean {
        return URL_TO_DRAWABLES.contains(imageTag)
    }

    fun emojify(text: String): String {
        var modifiedText = text
        val matcher = PATTERN_IMG_SRC.matcher(modifiedText)
        while (matcher.find()) {
            val url: String = matcher.group(1)!!.trim()
            var replacement = URL_TO_DEVICE_EMOTICONS[url]
            if (replacement == null)
                replacement = Emoticon.getEmoticonByUnicode(0x1F601) // ??
            modifiedText = modifiedText.replace(matcher.group().toRegex(), replacement)
        }
        return modifiedText
    }

    fun prepForWebView(text: String): String {
        var string = text
        EMOTICONS.forEach {
            string = string.replace(it.ascii, "<img src=${Values.URL + it.url}>")
        }
        return string
    }


    class Emoticon(
        val url: String,
        @field:DrawableRes
        val drawableRes: Int,
        unicode: Int,
        val ascii: String
    ) {
        val unicode: String

        companion object {
            fun getEmoticonByUnicode(unicode: Int): String {
                return String(Character.toChars(unicode))
            }
        }

        init {
            this.unicode = getEmoticonByUnicode(unicode)
        }
    }

}