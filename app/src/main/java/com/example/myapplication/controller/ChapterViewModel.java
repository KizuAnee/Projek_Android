package com.example.myapplication.controller;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.myapplication.model.data.Chapter;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.model.repository.GameRepository;

import java.util.List;

public class ChapterViewModel extends AndroidViewModel {

    private final GameRepository repository;
    private final LiveData<List<Chapter>> allChapters;

    public ChapterViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application);
        allChapters = repository.getAllChapters();
    }

    public LiveData<List<Chapter>> getAllChapters() {
        return allChapters;
    }

    public void updateChapter(Chapter chapter) {
        repository.updateChapter(chapter);
    }

    /**
     * Checks if a specific chapter is unlocked based on the completion of the previous chapter.
     * The first chapter is always unlocked.
     *
     * @param chapterId The ID of the chapter to check.
     * @return LiveData<Boolean> indicating if the chapter is unlocked.
     */
    public LiveData<Boolean> isChapterUnlocked(int chapterId) {
        // First chapter is always unlocked
        if (chapterId == 1) {
            return new MediatorLiveData<Boolean>() {{ setValue(true); }}; // Always true for chapter 1
        }

        MediatorLiveData<Boolean> unlockedStatus = new MediatorLiveData<>();

        // Observe all chapters to find the previous one
        unlockedStatus.addSource(allChapters, chapters -> {
            if (chapters != null) {
                Chapter prevChapter = null;
                for (Chapter chapter : chapters) {
                    if (chapter.getId() == chapterId - 1) {
                        prevChapter = chapter;
                        break;
                    }
                }

                if (prevChapter != null) {
                    // Once previous chapter is found, observe its levels to check completion
                    LiveData<List<Level>> levelsOfPrevChapter = repository.getLevelsForChapter(prevChapter.getId());
                    unlockedStatus.addSource(levelsOfPrevChapter, prevLevels -> {
                        if (prevLevels != null && !prevLevels.isEmpty()) {
                            boolean allPrevLevelsCompleted = true;
                            for (Level level : prevLevels) {
                                if (!level.isCompleted()) {
                                    allPrevLevelsCompleted = false;
                                    break;
                                }
                            }
                            unlockedStatus.setValue(allPrevLevelsCompleted);
                        } else {
                            // If previous chapter has no levels, it's not "completed" in terms of unlocking
                            unlockedStatus.setValue(false);
                        }
                        // Important: Remove source once the check is done or if it's no longer needed
                        // For simplicity, we keep it observing as chapter data might change.
                    });
                } else {
                    unlockedStatus.setValue(false); // Previous chapter not found, so current is locked
                }
            } else {
                unlockedStatus.setValue(false); // No chapters loaded yet
            }
        });

        return unlockedStatus;
    }

    // You might also want a factory for ChapterViewModel if you have custom dependencies,
    // but AndroidViewModel.AndroidViewModelFactory is generally sufficient for AndroidViewModel.
}