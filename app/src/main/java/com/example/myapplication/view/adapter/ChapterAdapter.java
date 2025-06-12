package com.example.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Added for locked toast

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.controller.ChapterViewModel; // Import ChapterViewModel
import com.example.myapplication.model.data.Chapter;

public class ChapterAdapter extends ListAdapter<Chapter, ChapterAdapter.ChapterViewHolder> {

    private OnChapterClickListener onClick;
    private ChapterViewModel chapterViewModel; // Added ViewModel
    private LifecycleOwner lifecycleOwner;     // Added LifecycleOwner

    public interface OnChapterClickListener {
        void onChapterClick(Chapter chapter);
    }

    public ChapterAdapter(OnChapterClickListener onClick, ChapterViewModel chapterViewModel, LifecycleOwner lifecycleOwner) {
        super(new ChapterDiffCallback());
        this.onClick = onClick;
        this.chapterViewModel = chapterViewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter_card, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = getItem(position);
        holder.bind(chapter, onClick, chapterViewModel, lifecycleOwner); // Pass new args
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvChapterTitle;
        private ImageView ivChapterImage;
        // private View overlayLocked; // If you add an overlay in XML, declare it here

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
            ivChapterImage = itemView.findViewById(R.id.ivChapterImage);
            // overlayLocked = itemView.findViewById(R.id.overlayLocked);
        }

        public void bind(Chapter chapter, OnChapterClickListener onClick, ChapterViewModel chapterViewModel, LifecycleOwner lifecycleOwner) {
            tvChapterTitle.setText(chapter.getTitle());
            Glide.with(itemView.getContext())
                    .load(chapter.getImageUrl() != null && !chapter.getImageUrl().isEmpty() ? chapter.getImageUrl() : R.drawable.ic_placeholder_image)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(ivChapterImage);

            // Observe the unlocked status for this specific chapter
            chapterViewModel.isChapterUnlocked(chapter.getId()).observe(lifecycleOwner, isUnlocked -> {
                chapter.setUnlocked(isUnlocked); // Update the chapter object directly
                itemView.setClickable(isUnlocked);
                if (isUnlocked) {
                    itemView.setAlpha(1.0f);
                    // if (overlayLocked != null) overlayLocked.setVisibility(View.GONE);
                    itemView.setOnClickListener(v -> onClick.onChapterClick(chapter));
                } else {
                    itemView.setAlpha(0.5f); // Make it semi-transparent
                    // if (overlayLocked != null) overlayLocked.setVisibility(View.VISIBLE);
                    itemView.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "This chapter is locked! Complete previous chapter to unlock.", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private static class ChapterDiffCallback extends DiffUtil.ItemCallback<Chapter> {
        @Override
        public boolean areItemsTheSame(@NonNull Chapter oldItem, @NonNull Chapter newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Chapter oldItem, @NonNull Chapter newItem) {
            return oldItem.equals(newItem);
        }
    }
}