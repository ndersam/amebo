package com.amebo.amebo.screens.postlist.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.widgets.OuchView
import com.amebo.amebo.databinding.ItemPostListAltContentBinding
import com.amebo.core.domain.ErrorResponse

@SuppressLint("NotifyDataSetChanged")
class AltAdapter(private val listener: Listener) : RecyclerView.Adapter<AltAdapter.ViewHolder>() {

    private var state: State? = null

    fun setLoading() {
        state = State.Loading
        notifyDataSetChanged()
    }

    fun setError(cause: ErrorResponse) {
        state = State.Error(cause)
        notifyDataSetChanged()
    }

    fun clear(isEmpty: Boolean = false) {
        state = if (isEmpty) {
            State.Empty
        } else {
            null
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemPostListAltContentBinding.inflate(inflater, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val state = state) {
            State.Empty -> holder.setEmpty()
            State.Loading -> holder.setLoading()
            is State.Error -> holder.setError(state.cause)
            null -> {
                /* do nothing */
            }
        }
    }

    override fun getItemCount(): Int = if (state != null) 1 else 0


    class ViewHolder(
        private val binding: ItemPostListAltContentBinding,
        private val listener: Listener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.ouchView.isVisible = false
            binding.ouchView.setButtonClickListener {
                listener.onRetryClicked()
            }
        }

        fun setLoading() {
            binding.progress.isVisible = true
            binding.ouchView.isVisible = false
        }

        fun setError(cause: ErrorResponse) {
            binding.progress.isVisible = false
            binding.ouchView.isVisible = true
            binding.ouchView.setState(cause)
        }

        fun setEmpty() {
            binding.progress.isVisible = false
            binding.ouchView.isVisible = true
            binding.ouchView.setState(OuchView.State.Empty)
        }
    }

    private sealed class State {
        object Empty : State()
        object Loading : State()
        class Error(val cause: ErrorResponse) : State()
    }

    interface Listener {
        fun onRetryClicked()
    }
}