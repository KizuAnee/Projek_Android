package com.example.myapplication.view.adapter;

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

import com.example.myapplication.R;
import com.example.myapplication.controller.LevelViewModel;
import com.example.myapplication.model.data.Level;

public class LevelAdapter extends ListAdapter<Level, LevelAdapter.LevelViewHolder> {

    private final OnLevelClickListener onClick;
    private final LevelViewModel levelViewModel;
    private final LifecycleOwner lifecycleOwner;

    public interface OnLevelClickListener {
        void onLevelClick(Level level);
    }

    public LevelAdapter(OnLevelClickListener onClick, LevelViewModel levelViewModel, LifecycleOwner lifecycleOwner) {
        super(new LevelDiffCallback());
        this.onClick = onClick;
        this.levelViewModel = levelViewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_button, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        Level level = getItem(position);
        holder.bind(level, onClick, levelViewModel, lifecycleOwner);
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLevelNumber;
        private final ImageView ivLockIcon;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevelNumber = itemView.findViewById(R.id.tvLevelNumber);
            ivLockIcon = itemView.findViewById(R.id.ivLockIcon);
        }

        public void bind(Level level, OnLevelClickListener onClick, LevelViewModel viewModel, LifecycleOwner owner) {
            tvLevelNumber.setText(String.format("LEVEL %d", level.getLevelNumber()));

            viewModel.isLevelUnlocked(level.getChapterId(), level.getLevelNumber()).observe(owner, isUnlocked -> {
                if (isUnlocked != null && isUnlocked) {
                    tvLevelNumber.setAlpha(1.0f);
                    tvLevelNumber.setClickable(true);
                    ivLockIcon.setVisibility(View.GONE);

                    tvLevelNumber.setOnClickListener(v -> onClick.onLevelClick(level));
                } else {
                    tvLevelNumber.setAlpha(0.5f);
                    tvLevelNumber.setClickable(true);
                    ivLockIcon.setVisibility(View.VISIBLE);

                    tvLevelNumber.setOnClickListener(v ->
                            Toast.makeText(itemView.getContext(), "Level ini terkunci. Selesaikan level sebelumnya untuk membuka.", Toast.LENGTH_SHORT).show());
                }

                // Update warna untuk status selesai
                if (level.isCompleted()) {
                    tvLevelNumber.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.material_dynamic_primary40));
                    tvLevelNumber.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
                } else {
                    tvLevelNumber.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.material_dynamic_neutral90));
                    tvLevelNumber.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                }
            });
        }
    }

    private static class LevelDiffCallback extends DiffUtil.ItemCallback<Level> {
        @Override
        public boolean areItemsTheSame(@NonNull Level oldItem, @NonNull Level newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Level oldItem, @NonNull Level newItem) {
            return oldItem.equals(newItem);
        }
    }
}
