package com.amebo.amebo.screens.topiclist.main

import android.graphics.Color
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.context
import com.amebo.amebo.common.extensions.dividerDrawable
import com.amebo.amebo.common.extensions.getTitle
import com.amebo.amebo.common.extensions.htmlFromBoardInfo
import com.amebo.amebo.databinding.FragmentTopicListDrawerBinding
import com.amebo.core.domain.*
import java.text.DecimalFormat

class TopicListDrawerView(
    private val topicList: TopicList,
    pref: Pref,
    view: View,
    private val listener: Listener
) {

    private val binding = FragmentTopicListDrawerBinding.bind(view)

    private var adapter: FollowedBoardsBoardAdapter? = null

    private val context get() = binding.context

    init {
        binding.topicListName.text = topicList.getTitle(context)
        binding.mailMods.isVisible = pref.isLoggedIn && topicList is Board
        binding.viewMods.isVisible = pref.isLoggedIn && topicList is Board
        binding.mailSuperMods.isVisible = pref.isLoggedIn && topicList is Board
        binding.boardInfo.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        if (topicList is FollowedBoards) {
            adapter = FollowedBoardsBoardAdapter(listener)
            binding.recyclerView.isVisible = true
            binding.recyclerView.adapter = adapter
            binding.recyclerView.dividerDrawable(R.drawable.divider, Color.TRANSPARENT)
            binding.boardInfo.isVisible = false
            binding.topicListName.isGone = true
            binding.boardStat.isGone = true
            binding.followedBoard.isVisible = true
        }
        binding.boardInfoCard.isVisible = topicList is FollowedBoards
    }


    fun update(dataPage: BaseTopicListDataPage) {
        when (topicList) {
            is Board -> {
                if (dataPage is BoardsDataPage) {
                    binding.boardStat.text = boardStat(dataPage)
                    binding.boardInfo.htmlFromBoardInfo(
                        "<p>${dataPage.boardInfo}</p></br>${dataPage.relatedBoards}",
                        listener::onBoardClicked
                    )
                    binding.boardInfoCard.isVisible =
                        dataPage.boardInfo.isNotBlank() || dataPage.relatedBoards.isNotBlank()
                    binding.viewMods.setOnClickListener {
                        listener.toUserList(
                            dataPage.moderators,
                            context.getString(R.string.x_board_moderators, topicList.name)
                        )
                    }
                    binding.mailMods.setOnClickListener {
                        listener.mailBoardMods()
                    }
                    binding.mailSuperMods.setOnClickListener {
                        listener.mailSuperMods()
                    }
                }
            }
            is FollowedBoards -> {
                if (dataPage is FollowedBoardsDataPage) {
                    adapter!!.setItem(dataPage.boards.map { it.first })
                }
            }
            else -> throw IllegalStateException("TopicList type ${topicList::class.java.name} unsupported")
        }
    }


    private fun boardStat(dataPage: BoardsDataPage): String {
        val formatter = DecimalFormat("#,###")
        val users = context.resources.getQuantityString(
            R.plurals.users,
            dataPage.numUsersViewing
        )
        val guests = context.resources.getQuantityString(
            R.plurals.guests,
            dataPage.numGuestsViewing
        )
        val online = context.getString(R.string.online)
        return "${formatter.format(dataPage.numUsersViewing)} $users - ${formatter.format(dataPage.numGuestsViewing)} $guests $online"
    }

    interface Listener : FollowedBoardsBoardAdapter.Listener {
        fun toUserList(users: List<User>, title: String)
        fun mailSuperMods()
        fun mailBoardMods()
    }

}