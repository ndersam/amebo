package com.amebo.amebo.screens.postlist.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemPostListAltContentBinding
import com.amebo.core.domain.ErrorResponse

class AltAdapter(private val listener: Listener) : RecyclerView.Adapter<AltAdapter.ViewHolder>() {

    private var cause: ErrorResponse? = null
    private var isLoading: Boolean = false

    fun setLoading() {
        cause = null
        isLoading = true
        notifyDataSetChanged()
    }

    fun setError(cause: ErrorResponse) {
        this.cause = cause
        isLoading = false
        notifyDataSetChanged()
    }

    // TODO: Handle isEmpty
    fun clear(isEmpty: Boolean = false) {
        isLoading = false
        cause = null
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
        when {
            cause is ErrorResponse -> holder.setError(cause!!)
            isLoading -> holder.setLoading()
        }
    }

    override fun getItemCount(): Int = if (isLoading || cause != null) 1 else 0


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
    }

    interface Listener {
        fun onRetryClicked()
    }
}