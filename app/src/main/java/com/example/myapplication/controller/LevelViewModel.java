package com.example.myapplication.controller;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.model.repository.GameRepository;
import java.util.List;

public class LevelViewModel extends AndroidViewModel {

    private final GameRepository repository;

    public LevelViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application);
    }

    public LiveData<List<Level>> getLevelsForChapter(int chapterId) {
        return repository.getLevelsForChapter(chapterId);
    }

    public LiveData<Boolean> isLevelUnlocked(int chapterId, int levelNumber) {
        if (levelNumber == 1) {
            MutableLiveData<Boolean> unlocked = new MutableLiveData<>();
            unlocked.setValue(true);
            return unlocked;
        }

        MediatorLiveData<Boolean> unlockedStatus = new MediatorLiveData<>();
        LiveData<List<Level>> levelsForChapter = repository.getLevelsForChapter(chapterId);

        unlockedStatus.addSource(levelsForChapter, levels -> {
            if (levels != null) {
                boolean prevLevelCompleted = false;
                Level prevLevel = null;
                for (Level level : levels) { // Use traditional for-each loop
                    if (level.getLevelNumber() == levelNumber - 1) {
                        prevLevel = level;
                        break;
                    }
                }

                if (prevLevel != null) {
                    prevLevelCompleted = prevLevel.isCompleted();
                }
                unlockedStatus.setValue(prevLevelCompleted);
            } else {
                unlockedStatus.setValue(false);
            }
        });
        return unlockedStatus;
    }

    // Solusi untuk 'map' error: Gunakan Transformations.map
    public LiveData<Integer> getTotalScoreForChapter(int chapterId) {
        return Transformations.map(repository.getLevelsForChapter(chapterId), levels -> {
            int total = 0;
            if (levels != null) {
                for (Level level : levels) { // Use traditional for-each loop
                    total += level.getScore();
                }
            }
            return total;
        });
    }

    public void updateLevel(Level level) {
        repository.updateLevel(level);
    }
}