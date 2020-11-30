package com.amebo.amebo.screens.newpost.editor

import android.content.Context
import com.amebo.amebo.R

sealed class EditAction (
    val start: String,
    val end: String = start,
    val drawableRes: Int = 0,
    val identifier: Int) {

    fun getName(context: Context): String
       = context.getString(
        when (this) {
            is Preview -> R.string.preview
            is AlignLeft -> R.string.align_left
            is AlignRight -> R.string.align_right
            is AlignCenter -> R.string.align_center
            is Quote -> R.string.quote
            is TextColor -> R.string.text_color
            is InsertLink -> R.string.insert_link
            is InsertImage -> R.string.insert_image
            is Bold -> R.string.bold
            is Italic -> R.string.italic
            is StrikeThrough -> R.string.strikethrough
            is SuperScript -> R.string.superscript
            is SubScript -> R.string.subscript
            is Font -> R.string.font
            is FontSize -> R.string.text_size
            is Code -> R.string.code
            is HR -> R.string.horizontal_rule
            is AttachFile -> R.string.attach_file
            is Settings -> R.string.settings
            is Emoticon -> R.string.emoticon
            is Undo -> R.string.undo
            is Redo -> R.string.redo
            is QuotePost -> R.string.quote_post
        }
    )



    object Preview : EditAction("", drawableRes = R.drawable.ic_remove_red_eye_24dp ,identifier = 0)
    object AlignLeft : EditAction("left", drawableRes = R.drawable.ic_format_align_left , identifier = 1)
    object AlignRight : EditAction("right", drawableRes = R.drawable.ic_format_align_right,identifier = 2)
    object AlignCenter : EditAction("center",drawableRes = R.drawable.ic_format_align_center, identifier = 3)
    object Quote : EditAction("quote",drawableRes = R.drawable.ic_format_quote ,identifier = 4)
    object TextColor : EditAction("color",drawableRes = R.drawable.ic_format_color_text ,identifier = 5)
    object InsertLink : EditAction("url",drawableRes = R.drawable.ic_insert_link ,identifier = 6)
    object InsertImage : EditAction("img",drawableRes = R.drawable.ic_insert_photo ,identifier = 7)
    object Bold : EditAction("b", drawableRes = R.drawable.ic_format_bold ,identifier = 8)
    object Italic : EditAction("i",drawableRes = R.drawable.ic_format_italic ,identifier = 9)
    object StrikeThrough : EditAction("s",drawableRes = R.drawable.ic_format_strikethrough ,identifier = 10)
    object SuperScript : EditAction("sup",drawableRes = R.drawable.ic_format_subscript ,identifier = 11)
    object SubScript : EditAction("sub", drawableRes = R.drawable.ic_format_superscript ,identifier = 12)
    object Font : EditAction("font=Lucida Sans Unicode", "font", drawableRes = R.drawable.ic_font_download ,identifier = 13)
    object FontSize : EditAction("size=8pt", "size",drawableRes = R.drawable.ic_format_size ,identifier = 14)
    object Code : EditAction("code", drawableRes = R.drawable.ic_code ,identifier = 15)
    object HR : EditAction("hr", drawableRes = R.drawable.ic_settings_ethernet_24dp,identifier = 16)
    object AttachFile : EditAction("", drawableRes = R.drawable.ic_attach_file,identifier = 17)
    object Settings : EditAction("", drawableRes = R.drawable.ic_settings_24dp,identifier = 18)
    object Emoticon : EditAction("", identifier = 19)
    object Undo: EditAction("", identifier = 20)
    object Redo : EditAction("", identifier = 21)
    object QuotePost :
        EditAction("", drawableRes = R.drawable.ic_baseline_add_comment_24, identifier = 22)


    companion object {

        fun getAction(identifier: Int)= when(identifier) {
            0 -> Preview
            1 -> AlignLeft
            2 -> AlignRight
            3 -> AlignCenter
            4 -> Quote
            5 -> TextColor
            6 -> InsertLink
            7 -> InsertImage
            8 -> Bold
            9 -> Italic
            10 -> StrikeThrough
            11 -> SuperScript
            12 -> SubScript
            13 -> Font
            14 -> FontSize
            15 -> Code
            16 -> HR
            17 -> AttachFile
            18 -> Settings
            19 -> Emoticon
            20 -> Undo
            21 -> Redo
            22 -> QuotePost
            else -> throw IllegalArgumentException("Unknown identifier '${identifier}'")
        }

        val defaultList  = listOf(
            AttachFile,
            InsertLink,
            InsertImage,
            Quote,
            Bold,
            Italic,
            StrikeThrough,
            TextColor,
            FontSize,
            Font,
            HR,
            Code,
            SubScript,
            SuperScript,
            AlignLeft,
            AlignCenter,
            AlignRight
        )

        val precedingList = listOf(Preview, QuotePost)
        val endingList = listOf(Settings)
    }
}