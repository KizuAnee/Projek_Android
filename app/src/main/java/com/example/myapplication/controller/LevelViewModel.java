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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelViewModel extends AndroidViewModel {

    private final GameRepository repository;
    private final Map<Integer, LiveData<List<Level>>> levelsCache = new HashMap<>();

    public LevelViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application);
    }

    // Cache agar tidak query ulang ke repository
    private LiveData<List<Level>> getCachedLevelsForChapter(int chapterId) {
        if (!levelsCache.containsKey(chapterId)) {
            levelsCache.put(chapterId, repository.getLevelsForChapter(chapterId));
        }
        return levelsCache.get(chapterId);
    }

    public LiveData<List<Level>> getLevelsForChapter(int chapterId) {
        return getCachedLevelsForChapter(chapterId);
    }

    public LiveData<Boolean> isLevelUnlocked(int chapterId, int levelNumber) {
        if (levelNumber == 1) {
            MutableLiveData<Boolean> unlocked = new MutableLiveData<>();
            unlocked.setValue(true);
            return unlocked;
        }

        MediatorLiveData<Boolean> unlockedStatus = new MediatorLiveData<>();
        LiveData<List<Level>> levelsForChapter = getCachedLevelsForChapter(chapterId);

        unlockedStatus.addSource(levelsForChapter, levels -> {
            if (levels != null) {
                Level prevLevel = null;
                for (Level level : levels) {
                    if (level.getLevelNumber() == levelNumber - 1) {
                        prevLevel = level;
                        break;
                    }
                }

                if (prevLevel != null) {
                    unlockedStatus.setValue(prevLevel.isCompleted());
                } else {
                    unlockedStatus.setValue(false);
                }
            } else {
                unlockedStatus.setValue(false);
            }
        });

        return unlockedStatus;
    }

    public LiveData<Integer> getTotalScoreForChapter(int chapterId) {
        return Transformations.map(getCachedLevelsForChapter(chapterId), levels -> {
            int total = 0;
            if (levels != null) {
                for (Level level : levels) {
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
