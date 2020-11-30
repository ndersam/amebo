package com.amebo.amebo.screens.postlist.components;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.amebo.amebo.R;

public class DefaultActionModeCallback implements ActionMode.Callback {
    private final Listener listener;

    public DefaultActionModeCallback(@NonNull Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_default_selected_text, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.search_in_web) {
            listener.searchInWeb();
            mode.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    public interface Listener {
        void searchInWeb();
    }
}