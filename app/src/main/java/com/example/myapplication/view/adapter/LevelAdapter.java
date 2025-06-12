package com.example.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast; // Added for locked toast

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.LevelViewModel; // Import LevelViewModel
import com.example.myapplication.model.data.Level;

public class LevelAdapter extends ListAdapter<Level, LevelAdapter.LevelViewHolder> {

    private OnLevelClickListener onClick;
    private LevelViewModel levelViewModel; // Added ViewModel
    private LifecycleOwner lifecycleOwner;   // Added LifecycleOwner

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_level_button, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        Level level = getItem(position);
        holder.bind(level, onClick, levelViewModel, lifecycleOwner); // Pass new args
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        private Button btnLevel;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            btnLevel = itemView.findViewById(R.id.btnLevel);
        }

        public void bind(Level level, OnLevelClickListener onClick, LevelViewModel levelViewModel, LifecycleOwner lifecycleOwner) {
            btnLevel.setText(String.format("Level %d", level.getLevelNumber()));

            // Observe the unlocked status for this specific level
            levelViewModel.isLevelUnlocked(level.getChapterId(), level.getLevelNumber()).observe(lifecycleOwner, isUnlocked -> {
                btnLevel.setEnabled(isUnlocked);
                if (isUnlocked) {
                    btnLevel.setAlpha(1.0f);
                    btnLevel.setOnClickListener(v -> onClick.onLevelClick(level));
                } else {
                    btnLevel.setAlpha(0.5f); // Make it semi-transparent
                    btnLevel.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "This level is locked! Complete previous level to unlock.", Toast.LENGTH_SHORT).show());
                }
            });

            // Indicate if level is completed
            if (level.isCompleted()) {
                btnLevel.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.material_dynamic_primary40));
                btnLevel.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
            } else {
                btnLevel.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.material_dynamic_neutral90));
                btnLevel.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            }
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