package com.amebo.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*


@Parcelize
class User @JvmOverloads constructor(
    val name: String,
    var data: Data? = null,
    private val _gender: Gender? = null
) : Parcelable {
    val slug: String = name.toLowerCase(Locale.ENGLISH)

    val gender: Gender? get() = data?.gender ?: _gender

    override fun equals(other: Any?): Boolean {
        if (other is User) {
            return other.slug.equals(slug, true)
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return slug.hashCode()
    }


    @Parcelize
    class Data(
        var gender: Gender = Gender.Unknown,
        var followers: MutableList<User> = mutableListOf(),
        var following: MutableList<User> = mutableListOf(),
        var isFollowing: Boolean = false,
        var personalText: String = "",
        var timeSpentOnline: String = "",
        var topicCount: Int = 0,
        var postCount: Int = 0,
        val latestTopics: MutableList<Topic> = mutableListOf(),
        var location: String = "",
        var signature: String = "",
        var twitter: String = "",
        var yim: String = "",
        var timeRegistered: Long = -1,
        var lastSeen: Long? = -1,
        var userID: String = "",
        var boardsMostActiveIn: MutableList<Board> = mutableListOf(),
        var boardsModeratesIn: MutableList<Board> = mutableListOf(),
        var image: Image? = null,
        var followUserUrl: String? = null
    ) : Parcelable

    @Parcelize
    class Image(
        var url: String,
        var timestamp: Long,
        var likes: Int,
        var oldLikes: Int,
        var isLiked: Boolean,
        var likeUrl: String?
    ) :
        Parcelable
}

enum class Gender { Male, Female, Unknown }