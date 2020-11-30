package com.amebo.amebo.screens.editactions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.dividerDrawable
import com.amebo.amebo.common.extensions.resizeWindowOnResume
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.FragmentEditActionsPickerScreenBinding
import com.amebo.amebo.di.Injectable
import com.amebo.amebo.screens.newpost.editor.EditActionSetting
import javax.inject.Inject


class EditActionsPickerScreen : DialogFragment(R.layout.fragment_edit_actions_picker_screen),
    Injectable {

    @Inject
    lateinit var pref: Pref

    private val binding by viewBinding(FragmentEditActionsPickerScreenBinding::bind)
    private lateinit var adapter: ItemAdapter
    private lateinit var items: MutableList<EditActionSetting>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        items = pref.editActions.toMutableList()
        adapter = ItemAdapter(items)
        binding.recyclerView.dividerDrawable(R.drawable.divider)
        binding.recyclerView.adapter = adapter
        val helper = ItemTouchHelper(DragController())
        helper.attachToRecyclerView(binding.recyclerView)
    }

    override fun onResume() {
        super.onResume()
        resizeWindowOnResume()
    }

    override fun onDestroyView() {
        pref.editActions = items
        super.onDestroyView()
    }

}
