package com.bretahajek.scannerapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.databinding.DocumentItemBinding;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.viewmodel.DocumentListViewModel;

import java.io.File;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    List<? extends Document> mDocumentList;

    private Context mContext;
    private DocumentListViewModel viewModel;

    @Nullable
    private final DocumentClickCallback mProductClickCallback;

    public DocumentAdapter(@Nullable DocumentClickCallback clickCallback, Context context) {
        mProductClickCallback = clickCallback;
        mContext = context;
        viewModel =
                new ViewModelProvider((FragmentActivity) context).get(DocumentListViewModel.class);
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
                    Document newProduct = documentList.get(newItemPosition);
                    Document oldProduct = mDocumentList.get(oldItemPosition);
                    return newProduct.getId() == oldProduct.getId()
                            && newProduct.getPageCount() == oldProduct.getPageCount()
                            && newProduct.getName().equals(oldProduct.getName())
                            && newProduct.getFolder().equals(oldProduct.getFolder())
                            && newProduct.getCreationDate().equals(oldProduct.getCreationDate());
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
        binding.setCallback(mProductClickCallback);
        return new DocumentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final DocumentViewHolder holder, final int position) {
        holder.binding.setDocument(mDocumentList.get(position));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buildPopupMenu(holder, view);
                return false;
            }
        });

        holder.binding.dropdownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildPopupMenu(holder, holder.binding.dropdownMenu);
            }
        });

        holder.binding.executePendingBindings();
    }

    private void buildPopupMenu(DocumentViewHolder holder, View bindView) {
        PopupMenu menu = new PopupMenu(mContext, bindView);
        menu.inflate(R.menu.document_card_menu);

        final Document document = holder.binding.getDocument();
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.dc_menu_export:
                        break;
                    case R.id.dc_menu_tags:
                        break;
                    case R.id.dc_menu_delete:
                        // TODO: "Are you sure" dialog
                        File deleteFolder = new File(
                                mContext.getExternalFilesDir(null), document.getFolder());
                        viewModel.deleteDocument(document, deleteFolder);
                        break;
                }
                return false;
            }
        });
        menu.show();
    }

    @Override
    public int getItemCount() {
        return mDocumentList == null ? 0 : mDocumentList.size();
    }

    @Override
    public long getItemId(int position) {
        return mDocumentList.get(position).getId();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {

        final DocumentItemBinding binding;

        public DocumentViewHolder(DocumentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
