package com.bretahajek.docus.ui;

import android.view.View;

import com.bretahajek.docus.db.Document;

public interface DocumentClickCallback {
    void onClick(Document document);

    void onLongClick(DocumentAdapter.DocumentViewHolder viewHolder, View bindView);

    void onMenuClick(DocumentAdapter.DocumentViewHolder viewHolder, View bindView);
}
