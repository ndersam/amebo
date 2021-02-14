package com.amebo.amebo.screens.newpost.newtopic

import androidx.fragment.app.Fragment
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.showKeyboard
import com.amebo.amebo.databinding.NewTopicScreenBinding
import com.amebo.amebo.screens.newpost.IFormView
import com.amebo.amebo.screens.newpost.SimpleFormView
import com.amebo.core.domain.Board
import java.lang.ref.WeakReference

class NewTopicView(
    fragment: Fragment,
    pref: Pref,
    binding: NewTopicScreenBinding,
    board: Board?,
    listener: Listener
) :

    SimpleFormView(
        fragment = fragment,
        pref = pref,
        editActionMenu = binding.editActionMenu,
        editMessage = binding.editMessage,
        editTitle = binding.editTitle,
        listener = listener,
        ouchView = binding.ouchView,
        stateLayout = binding.stateLayout,
        toolbar = binding.toolbar
    ) {
    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!

    init {
        binding.background.setOnClickListener {
            binding.editMessage.showKeyboard()
        }
        binding.txtBoard.setOnClickListener {
            listener.selectBoard()
        }

        setSelectedBoard(board)
    }

    fun setSelectedBoard(board: Board?) {
        binding.txtBoard.text = board?.name ?: binding.root.context.getString(R.string.select_board)
        invalidateMenu()
    }

    interface Listener : IFormView.Listener {
        fun selectBoard()
    }
}