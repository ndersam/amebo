package com.amebo.amebo.screens.newpost.editor

import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.amebo.amebo.R

/**
 * This class is used to provide a text selection menu for edit action such as bold, italic and
 * strikethrough.
 *
 */
class StyleCallback(private val listener: EditActionMenu.Listener ) :
    ActionMode.Callback {
    override fun onCreateActionMode(
        mode: ActionMode,
        menu: Menu
    ): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.edit_menu_select, menu)
        menu.removeItem(android.R.id.selectAll)
        if (Build.VERSION.SDK_INT >= 23) menu.removeItem(android.R.id.shareText)
        return true
    }

    override fun onPrepareActionMode(
        mode: ActionMode,
        menu: Menu
    ): Boolean {
        return false
    }

    override fun onActionItemClicked(
        mode: ActionMode,
        item: MenuItem
    ): Boolean {
        when (item.itemId) {
            R.id.bold -> {
                listener.onEditActionClicked(null, EditAction.Bold)
                return true
            }
            R.id.italic -> {
                listener.onEditActionClicked(null, EditAction.Italic)
                return true
            }
            R.id.strike_through -> {
                listener.onEditActionClicked(null, EditAction.StrikeThrough)
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {}

}