package com.amebo.amebo.screens.mail.inbox

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.databinding.ItemPmSenderBinding
import com.amebo.core.domain.User

class ItemAdapter(private val listener: Listener): ListAdapter<User, ItemAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPmSenderBinding.inflate(LayoutInflater.from(parent.context)),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: List<User>?) {
        super.submitList(list?.let { ArrayList(list) })
    }

    class ViewHolder(private val binding: ItemPmSenderBinding, private val listener: Listener): RecyclerView.ViewHolder(binding.root) {
        private lateinit var user: User

        init {
            binding.card.setOnClickListener {
                listener.onUserClicked(user)
            }
        }

        fun bind(user: User){
            this.user = user
            binding.txtName.text = user.name
        }
    }

    interface Listener {
        fun onUserClicked(user: User)
    }

    companion object {
        private  val DIFF_CALLBACK = object: DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }

        }
    }


}