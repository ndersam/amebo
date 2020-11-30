package com.amebo.amebo.screens.userlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.common.extensions.genderDrawable
import com.amebo.amebo.common.extensions.setDrawableEnd
import com.amebo.amebo.databinding.ItemUserBinding
import com.amebo.core.domain.User

class ItemAdapter(
     users: List<User>,
    private val listener: Listener
) : ListAdapter<User, ItemAdapter.ViewHolder>(DIFF_CALLBACK) {

    init {
        submitList(users)
    }

    override fun submitList(list: List<User>?) {
        super.submitList(list?.let {
            ArrayList(list)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemUserBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class ViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, listener: Listener) {
            binding.textView.text = user.name
            binding.textView.setDrawableEnd(user.genderDrawable)
            binding.root.setOnClickListener { listener.visitUser(user) }
        }
    }

    interface Listener {
        fun visitUser(user: User)
    }

    companion object {
        private  val DIFF_CALLBACK  = object: DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
               return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.slug == newItem.slug && oldItem.data?.equals(newItem.data) == true
            }

        }
    }
}