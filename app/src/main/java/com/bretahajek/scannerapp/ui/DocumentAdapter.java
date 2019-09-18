package com.bretahajek.scannerapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.databinding.DocumentItemBinding;
import com.bretahajek.scannerapp.db.Document;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    List<? extends Document> mDocumentList;

    @Nullable
    private final DocumentClickCallback mDocumentClickCallback;

    public DocumentAdapter(@Nullable DocumentClickCallback clickCallback) {
        mDocumentClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setDocumentList(final List<? extends Document> documentList) {
        if (mDocumentList == null) {
            mDocumentList = documentList;
            notifyItemRangeInserted(0, documentList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mDocumentList.size();
                }

                @Override
                public int getNewListSize() {
                    return documentList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mDocumentList.get(oldItemPosition).getId() ==
                            documentList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Document newDocument = documentList.get(newItemPosition);
                    Document oldDocument = mDocumentList.get(oldItemPosition);
                    return newDocument.getId() == oldDocument.getId()
                            && newDocument.getPageCount() == oldDocument.getPageCount()
                            && newDocument.getName().equals(oldDocument.getName())
                            && newDocument.getFolder().equals(oldDocument.getFolder())
                            && newDocument.getCreationDate().equals(oldDocument.getCreationDate());
                }
            });
            mDocumentList = documentList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DocumentItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.document_item,
                        parent, false);
        binding.setCallback(mDocumentClickCallback);
        return new DocumentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final DocumentViewHolder holder, final int position) {
        holder.binding.setDocument(mDocumentList.get(position));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mDocumentClickCallback.onLongClick(holder, view);
                return false;
            }
        });

        holder.binding.dropdownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDocumentClickCallback.onMenuClick(holder, holder.binding.dropdownMenu);
            }
        });

        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mDocumentList == null ? 0 : mDocumentList.size();
    }

    @Override
    public long getItemId(int position) {
        return mDocumentList.get(position).getId();
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {

        public final DocumentItemBinding binding;

        public DocumentViewHolder(DocumentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
