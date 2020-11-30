package com.amebo.amebo.screens.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.setDrawableEnd
import com.amebo.amebo.databinding.ItemAccountBinding
import com.amebo.amebo.databinding.ItemAddAccountBinding
import com.amebo.core.domain.AnonymousAccount
import com.amebo.core.domain.RealUserAccount
import com.amebo.core.domain.UserAccount

class AccountAdapter(
    users: List<UserAccount>,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val list: MutableList<UserAccount> = users.toMutableList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_BUTTON -> {
                val binding = ItemAddAccountBinding.inflate(inflater, parent, false)
                AddItemViewHolder(binding, listener)
            }
            ANONYMOUS_ACCOUNT -> {
                val binding = ItemAccountBinding.inflate(inflater, parent, false)
                AnonymousViewHolder(binding, listener)
            }
            else -> {
                val binding = ItemAccountBinding.inflate(inflater, parent, false)
                ViewHolder(binding, listener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < list.size) {
            if (list[position] is RealUserAccount) {
                return REAL_ACCOUNT
            }
            return ANONYMOUS_ACCOUNT
        }
        return TYPE_BUTTON
    }

    fun remove(account: RealUserAccount) {
        val index = list.indexOfFirst {
            it is RealUserAccount && it.user == account.user
        }
        if (index == -1) {
            throw IllegalArgumentException("Unable to find user")
        }
        list.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun getItemCount(): Int = list.size + 1 // +1 for button

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(list[position] as RealUserAccount)
            }
            is AddItemViewHolder -> {
                // Nothing
            }
            is AnonymousViewHolder -> {
                holder.bind()
            }
        }
    }

    class ViewHolder(val binding: ItemAccountBinding, private val listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var userAccount: RealUserAccount

        init {
            binding.btnRemove.setOnClickListener {
                listener.onRemoveClicked(
                    userAccount,
                    bindingAdapterPosition
                )
            }
            binding.root.setOnClickListener { listener.onUserClicked(userAccount) }
        }

        fun bind(userAccount: RealUserAccount) {
            this.userAccount = userAccount
            binding.username.text = userAccount.user.name
            markCurrentAccount(
                binding.username,
                listener.isCurrentUser(userAccount)
            )
        }

        companion object {
            fun markCurrentAccount(username: TextView, isCurrentAccount: Boolean) {
                username.setDrawableEnd(
                    if (isCurrentAccount) R.drawable.ic_done_24dp else null
                )
            }
        }
    }

    class AnonymousViewHolder(val binding: ItemAccountBinding, private val listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRemove.isVisible = false
            binding.root.setOnClickListener { listener.onUserClicked(AnonymousAccount) }
            binding.username.setText(R.string.anonymous)
        }

        fun bind() {
            ViewHolder.markCurrentAccount(
                binding.username,
                listener.isCurrentUser(AnonymousAccount)
            )
        }
    }

    class AddItemViewHolder(val binding: ItemAddAccountBinding, listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { listener.onAddClicked() }
        }
    }

    companion object {
        const val REAL_ACCOUNT = 0
        const val ANONYMOUS_ACCOUNT = 1
        const val TYPE_BUTTON = 2
    }

    interface Listener {
        fun onRemoveClicked(userAccount: RealUserAccount, position: Int)
        fun onUserClicked(userAccount: UserAccount)
        fun isCurrentUser(userAccount: UserAccount): Boolean
        fun onAddClicked()
    }
}