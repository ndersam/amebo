package com.amebo.amebo.screens.topiclist.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemFollowedBoardBinding
import com.amebo.core.domain.Board

/**
 * Used to list of followed boards in [TopicListScreen]
 */
class FollowedBoardsBoardAdapter(
    private val listener: Listener,
) :
    ListAdapter<Board, FollowedBoardsBoardAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var _items = mutableListOf<Board>()

    fun setItem(topics: List<Board>) {
        _items = ArrayList(topics.toMutableList())
        submitList(_items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemFollowedBoardBinding.inflate(inflater, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun removeItemAt(position: Int) {
        _items.removeAt(position)
        submitList(ArrayList(_items))
    }

    class ViewHolder(private val binding: ItemFollowedBoardBinding, listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var board: Board

        init {
            binding.btnRemove.setOnClickListener {
                listener.unFollowBoard(board, bindingAdapterPosition)
            }
            binding.root.setOnClickListener {
                listener.onBoardClicked(board)
            }
        }

        fun bind(board: Board) {
            this.board = board
            binding.textView.text = board.name
        }
    }

    interface Listener {
        fun onBoardClicked(board: Board)
        fun unFollowBoard(board: Board, position: Int)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Board>() {
            override fun areItemsTheSame(oldItem: Board, newItem: Board): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Board, newItem: Board): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }
}