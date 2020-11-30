package com.amebo.amebo.screens.postlist.components;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

import androidx.annotation.NonNull;

import timber.log.Timber;


public class DefaultActionModeListenerImpl implements DefaultActionModeCallback.Listener {

    private final TextView txtView;

    public DefaultActionModeListenerImpl(@NonNull TextView txtView) {
        this.txtView = txtView;
    }


    @Override
    public void searchInWeb() {
        Context context = txtView.getContext();
        String query = txtView.getText().toString().substring(txtView.getSelectionStart(), txtView.getSelectionEnd());
        try {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, query);
            context.startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
            Uri uri = Uri.parse("https://www.google.com/search?q=" + query);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }
}