package com.example.myapplication.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.controller.ChapterViewModel;
import com.example.myapplication.model.data.Chapter;

public class ChapterAdapter extends ListAdapter<Chapter, ChapterAdapter.ChapterViewHolder> {

    private OnChapterClickListener onClick;
    private ChapterViewModel chapterViewModel;
    private LifecycleOwner lifecycleOwner;

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
        holder.bind(chapter, onClick, chapterViewModel, lifecycleOwner);
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvChapterTitle;
        private ImageView ivChapterImage;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
            ivChapterImage = itemView.findViewById(R.id.ivChapterImage);
        }

        public void bind(Chapter chapter, OnChapterClickListener onClick, ChapterViewModel chapterViewModel, LifecycleOwner lifecycleOwner) {
            tvChapterTitle.setText(chapter.getTitle());

            // Log data untuk debugging
            Log.d("CHAPTER_BIND", "Chapter Title: " + chapter.getTitle());
            Log.d("CHAPTER_BIND", "Image URL (name): " + chapter.getImageUrl());
            Log.d("CHAPTER_BIND", "Image URL (from object): " + chapter.getImageUrl());


            // Ambil ID resource drawable dari nama imageUrl
            int imageResId = itemView.getContext().getResources()
                    .getIdentifier(chapter.getImageUrl(), "drawable", itemView.getContext().getPackageName());

            Log.d("CHAPTER_BIND", "Resolved imageResId: " + imageResId);

            // Tampilkan gambar jika ID ditemukan, jika tidak tampilkan placeholder
            if (imageResId != 0) {
                Log.d("CHAPTER_BIND", "Loading image with Glide: " + chapter.getImageUrl());
                Glide.with(itemView.getContext())
                        .load(imageResId)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(ivChapterImage);
            } else {
                Log.e("CHAPTER_BIND", "Image not found! Using placeholder.");
                ivChapterImage.setImageResource(R.drawable.ic_placeholder_image);
            }

            // Logika kunci/unlock seperti biasa
            chapterViewModel.isChapterUnlocked(chapter.getId()).observe(lifecycleOwner, isUnlocked -> {
                chapter.setUnlocked(isUnlocked);
                itemView.setClickable(isUnlocked);
                if (isUnlocked) {
                    itemView.setAlpha(1.0f);
                    itemView.setOnClickListener(v -> onClick.onChapterClick(chapter));
                } else {
                    itemView.setAlpha(0.5f);
                    itemView.setOnClickListener(v ->
                            Toast.makeText(itemView.getContext(), "This chapter is locked! Complete previous chapter to unlock.", Toast.LENGTH_SHORT).show());
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
