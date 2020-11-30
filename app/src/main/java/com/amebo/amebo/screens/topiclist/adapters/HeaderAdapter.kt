package com.amebo.amebo.screens.topiclist.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.databinding.ItemSortBinding
import com.amebo.core.domain.*

class HeaderAdapter(
    private val topicList: TopicList,
    private var sort: Sort?,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: List<HeaderItem> = when (topicList) {
        is Board, FollowedBoards -> mutableListOf(ItemSort)
        else -> emptyList()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SortVH(ItemSortBinding.inflate(inflater, parent, false), topicList, listener)
    }

    override fun getItemCount(): Int = if (sort == null) 0 else items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as SortVH
        vh.bind(sort!!)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_sort
    }

    fun setItems(sort: Sort?) {
        this.sort = sort
        notifyDataSetChanged()
    }


    private class SortVH(
        private val binding: ItemSortBinding,
        private val topicList: TopicList,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        private var trigger = true


        init {
            binding.boardSorts.isVisible = topicList is Board
            binding.followBoardSorts.isVisible = topicList is FollowedBoards

            binding.boardSorts.addOnSegmentSelectListener { segmentViewHolder, isSelected, _ ->
                if (trigger) {
                    val index = segmentViewHolder.absolutePosition
                    if (isSelected) {
                        listener.onSortSelected(TopicListSorts.BoardSorts[index])
                    }
                }

            }
            binding.followBoardSorts.addOnSegmentSelectListener { segmentViewHolder, isSelected, _ ->
                if (trigger) {
                    val index = segmentViewHolder.absolutePosition
                    if (isSelected) {
                        listener.onSortSelected(TopicListSorts.FollowedBoardsSorts[index])
                    }
                }
            }
        }

        fun bind(selected: Sort) {
            trigger = false
            when (topicList) {
                is Board -> {
                    binding.boardSorts.setSelectedSegment(TopicListSorts.BoardSorts.indexOfFirst { it == selected })
                }
                is FollowedBoards -> {
                    binding.followBoardSorts.setSelectedSegment(TopicListSorts.FollowedBoardsSorts.indexOfFirst { it == selected })

                }
                else -> throw IllegalStateException("You shouldn't be here")
            }
            trigger = true
        }
    }


    interface Listener {
        fun onSortSelected(sort: Sort)
        fun loadNextPage()
        fun loadPrevPage()
        fun loadFirstPage()
        fun loadLastPage()
        fun hasNextPage(): Boolean
        fun hasPrevPage(): Boolean
    }
}

private sealed class HeaderItem
private object ItemSort : HeaderItem()