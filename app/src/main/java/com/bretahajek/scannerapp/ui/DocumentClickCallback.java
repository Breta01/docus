package com.bretahajek.scannerapp.ui;

import android.view.View;

import com.bretahajek.scannerapp.db.Document;

public interface DocumentClickCallback {
    void onClick(Document document);

    void onLongClick(DocumentAdapter.DocumentViewHolder viewHolder, View bindView);

    void onMenuClick(DocumentAdapter.DocumentViewHolder viewHolder, View bindView);
}
