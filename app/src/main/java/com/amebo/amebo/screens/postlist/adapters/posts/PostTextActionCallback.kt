package com.amebo.amebo.screens.postlist.adapters.posts

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode

class PostTextActionCallback : ActionMode.Callback{
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

    }
}