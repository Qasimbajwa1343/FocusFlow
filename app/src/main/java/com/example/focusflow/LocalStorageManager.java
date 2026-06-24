package com.example.focusflow;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorageManager {

    private static final String PREF_NAME = "ScrollSenseData";

    private static final String KEY_TOTAL_SCROLL_MINUTES = "total_scroll_minutes";
    private static final String KEY_ESTIMATED_REELS = "estimated_reels";
    private static final String KEY_PRODUCTIVITY_SCORE = "productivity_score";
    private static final String KEY_DATE = "date";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public LocalStorageManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveDailyData(int totalScrollMinutes, int estimatedReels, int productivityScore, String date) {
        editor.putInt(KEY_TOTAL_SCROLL_MINUTES, totalScrollMinutes);
        editor.putInt(KEY_ESTIMATED_REELS, estimatedReels);
        editor.putInt(KEY_PRODUCTIVITY_SCORE, productivityScore);
        editor.putString(KEY_DATE, date);
        editor.apply();
    }

    public int getTotalScrollMinutes() {
        return sharedPreferences.getInt(KEY_TOTAL_SCROLL_MINUTES, 0);
    }

    public int getEstimatedReels() {
        return sharedPreferences.getInt(KEY_ESTIMATED_REELS, 0);
    }

    public int getProductivityScore() {
        return sharedPreferences.getInt(KEY_PRODUCTIVITY_SCORE, 100);
    }

    public String getSavedDate() {
        return sharedPreferences.getString(KEY_DATE, "");
    }

    public void clearData() {
        editor.clear();
        editor.apply();
    }
}