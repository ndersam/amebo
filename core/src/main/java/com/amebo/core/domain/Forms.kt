package com.amebo.core.domain

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

sealed class Form(
    var body: String,
    var title: String,
    var titleEditable: Boolean,
    var bundle: ImageBundle
)

class NewPostForm(
    body: String,
    val topic: String,
    val maxPost: Int,
    title: String,
    val session: String,
    var follow: Boolean = false,
    val quotablePosts: List<QuotablePost> = emptyList()
) : Form(body, title, false, ImageBundle(emptyList()))

class ModifyForm(
    body: String,
    title: String,
    titleEditable: Boolean,
    val session: String,
    val redirect: String,
    val post: Long,
    val attachments: MutableList<Attachment>
) : Form(body, title, titleEditable, ImageBundle(emptyList()))

@Parcelize
data class Attachment(
    val name: String,
    val id: Long,
    val post: Long,
    val session: String,
    val redirect: String,
    val referer: String = ""
) : Parcelable

class NewTopicForm(
    body: String,
    val board: Int,
    title: String,
    val session: String
) : Form(body, title, true, ImageBundle(emptyList()))


class ReportPostForm(
    val postId: String,
    val session: String,
    val redirect: String,
    var reason: String = "",
    var referer: String
)

class ImageBundle(val data: List<Pair<String, Bitmap>>)

open class MailForm(var body: String, var subject: String, val canSendMail: Boolean)
class MailUserForm(
    val session: String,
    val recipientName: String,
    body: String,
    subject: String,
    canSendMail: Boolean
) : MailForm(body, subject, canSendMail)

class MailSuperModsForm(val session: String, body: String, subject: String) :
    MailForm(body = body, subject = subject, canSendMail = true)

class MailBoardModsForm(val session: String, body: String, subject: String, val boardNo: Int) :
    MailForm(body = body, subject = subject, canSendMail = true)

class EditProfileForm(
    val email: String,
    var birthDate: BirthDate?,
    var personalText: String,
    var signature: String,
    var location: String,
    var yim: String,
    var twitter: String,
    var gender: Gender?,
    val session: String,
    var photo: Pair<String, Bitmap>?,
    var removeThisImage: Boolean,
    val earliestYear: Year
)

@Parcelize
class AreYouMuslimDeclarationForm(
    val session: String,
    val accept: String,
    val decline: String,
    val redirect: String,
    var accepted: Boolean = false
) : Parcelable

class DismissMailNotificationForm(
    val session: String,
    val redirect: String,
    val senders: List<User>
)

@Parcelize
class QuotablePost(
    val id: String,
    val number: Int,
    val author: User,
    val text: String,
    val url: String,
    val session: String
) : Parcelable {

    @IgnoredOnParcel
    val post = SimplePost(
        author,
        Topic("", -1, ""),
        id,
        url,
        likes = 0,
        shares = -1,
        text = text,
        timestamp = -1
    )
}