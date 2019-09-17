package com.bretahajek.scannerapp.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.databinding.TagItemBinding;
import com.bretahajek.scannerapp.db.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    List<Tag> mTagList;

    public TagAdapter() {
        setHasStableIds(true);
    }

    public List<Tag> getTagList() {
        return mTagList;
    }

    public void clearTagStates() {
        if (mTagList != null) {
            for (Tag t : mTagList) {
                t.setState(false);
            }
        }
        for (Tag t : mTagList) {

        }
        Log.i("Clear", "cl");
        notifyItemRangeChanged(0, mTagList.size());
    }

    public void setDocumentTagList(final List<Tag> tagList) {
        final List<Tag> newTagList = new ArrayList<>();
        newTagList.addAll(mTagList);

        for (Tag t : tagList) {
            for (int i = 0; i < mTagList.size(); i++) {
                if (t.getId() == mTagList.get(i).getId()
                        && t.getName().equals(mTagList.get(i).getName())) {
                    t.setState(true);
                    mTagList.set(i, t);
                    notifyItemChanged(i, t);
                }
            }
        }
    }

    public void setTagList(final List<Tag> tagList) {
        if (mTagList == null) {
            mTagList = tagList;
            notifyItemRangeChanged(0, tagList.size());
        } else {
            DiffUtil.DiffResult result = getDiffResult(tagList);
            for (int i = 0; i < mTagList.size(); i++) {
                int pos = result.convertOldPositionToNew(i);
                if (pos != DiffUtil.DiffResult.NO_POSITION) {
                    tagList.set(pos, mTagList.get(i));
                }
            }
            mTagList = tagList;
            result.dispatchUpdatesTo(this);
        }
    }

    private DiffUtil.DiffResult getDiffResult(final List<Tag> tagList) {
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
                        && newTag.getName().equals(oldTag.getName());
            }
        });
        return result;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TagItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.tag_item,
                        parent, false);
        binding.setCallback(this);
        return new TagViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        holder.binding.setTag(mTagList.get(position));
        holder.binding.checkBox.setChecked(mTagList.get(position).isState());
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

    public void onTagClickCallback(Tag tag) {
        tag.switchState();
    }
}
