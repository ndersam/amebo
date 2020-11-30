package com.amebo.amebo.data

import com.amebo.amebo.common.Resource
import com.amebo.core.domain.*
import io.bloco.faker.Faker

object TestData {
    private val faker = Faker()

    val topics = arrayListOf(
        Topic(
            "Covid-19: Israel Adesanya Donates Medical Equipment To Lagos",
            5798658,
            "covid-19-israel-adesanya-donates-medical",
            mainBoard = Board("Politics", "politics")
        )
    )

    fun newPost(
        modifiable: Boolean = false,
        likableOrShareable: Boolean = false,
        isLiked: Boolean = false,
        isShared: Boolean = false
    ) = SimplePost(
        author = generateUser(),
        id = generatePostId(),
        text = faker.lorem.sentence(),
        timestamp = faker.time.backward(1).time,
        topic = generateTopic(),
        editUrl = if (modifiable) faker.lorem.word() else null,
        likeUrl = if (likableOrShareable) faker.lorem.word() else null,
        isLiked = isLiked,
        isShared = isShared,
        images = emptyList(),
        likes = faker.number.positive(),
        parentQuotes = emptyList(),
        reportUrl = null,
        shares = faker.number.positive(),
        shareUrl = if (likableOrShareable) faker.lorem.word() else null,
        url = faker.internet.url()
    )


    fun postListOn(topic: Topic = topics.first()): Resource<PostListDataPage> =
        Resource.Success(
            TopicPostListDataPage(
                topic = topic,
                data = emptyList(),
                page = 0,
                last = 0,
                isClosed = false,
                views = 0,
                usersViewing = emptyList(),
                isHiddenFromUser = false,
                isFollowingTopic = false
            )
        )

    fun fetchPostListOnTopic(topic: Topic = topics.first(), postCount: Int = 3) =
        TopicPostListDataPage(
            topic = topic,
            data = (0 until postCount).map { newPost() },
            page = 0,
            last = 0,
            isClosed = false,
            views = 0,
            usersViewing = emptyList(),
            isHiddenFromUser = false,
            isFollowingTopic = false
        )


    fun newTopicPostList() = TopicPostListDataPage(
        topic = topics.first(),
        data = emptyList(),
        page = 0,
        last = 0,
        isClosed = false,
        views = 0,
        usersViewing = emptyList(),
        isHiddenFromUser = false,
        isFollowingTopic = false
    )

    fun generateTopic(linkedPage: Int = 0) = Topic(
        title = generateTopicTitle(),
        id = generateTopicId(),
        slug = generateSlug(),
        linkedPage = linkedPage
    )

    fun generateFeaturedTopicList(count: Int): List<Topic> {
        assert(count > 0)
        return (1..count).map { generateTopic() }
    }

    fun generateBoard(): Board {
        return Board(faker.book.title(), faker.lorem.word(), faker.number.positive())
    }

    fun generateUser(): User {
        return User(faker.name.firstName())
    }

    fun generateDetailedTopicList(count: Int): List<Topic> {
        assert(count > 0)
        return (1..count).map {
            Topic(
                title = generateTopicTitle(),
                id = generateTopicId(),
                slug = generateSlug(),
                author = User(faker.name.firstName()),
                mainBoard = Board(faker.food.dish(), faker.food.dish())
            )
        }
    }

    private fun generateTopicTitle(numOfWords: Int = 5): String {
        return faker.lorem.words(numOfWords).joinToString(separator = " ")
    }

    private fun generateSlug(numOfWords: Int = 5): String {
        return faker.lorem.words(numOfWords).joinToString(separator = "-")
    }

    private fun generateTopicId() = faker.number.number(7).toInt()

    private fun generatePostId() = faker.number.number(7)
}