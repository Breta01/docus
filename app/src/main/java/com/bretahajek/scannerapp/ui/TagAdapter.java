package com.bretahajek.scannerapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.databinding.TagItemBinding;
import com.bretahajek.scannerapp.db.Tag;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    List<? extends Tag> mTagList;

    public TagAdapter() {
//        mDocumentClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setTagList(final List<? extends Tag> tagList) {
        if (mTagList == null) {
            mTagList = tagList;
            notifyItemRangeChanged(0, tagList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mTagList.size();
                }

                @Override
                public int getNewListSize() {
                    return tagList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mTagList.get(oldItemPosition).getId() ==
                            tagList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Tag newTag = tagList.get(newItemPosition);
                    Tag oldTag = mTagList.get(oldItemPosition);
                    return newTag.getId() == oldTag.getId()
                            && newTag.getDocumentCount() == oldTag.getDocumentCount()
                            && newTag.getName().equals(oldTag.getName())
                            && newTag.isState() == oldTag.isState();
                }
            });
            mTagList = tagList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TagItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.tag_item,
                        parent, false);
//        binding.setCallback(mDocumentClickCallback);
        return new TagViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        holder.binding.setTag(mTagList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return (mTagList == null) ? 0 : mTagList.size();
    }

    @Override
    public long getItemId(int position) {
        return mTagList.get(position).getId();
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {

        public final TagItemBinding binding;

        public TagViewHolder(TagItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
