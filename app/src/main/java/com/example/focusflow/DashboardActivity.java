package com.example.focusflow;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.focusflow.utils.NotificationHelper;
import com.example.focusflow.utils.PermissionUtils;
import com.example.focusflow.utils.UsageHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvScrollTime, tvReels, tvScore, tvScoreStatus, tvQuote, tvFocusStreak;
    private TextView navHome, navAnalytics, navFocus, navLearn;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 500;

    private static final String PREFS_NAME = "FocusFlowPrefs";

    private static final String KEY_SCROLL_TIME = "lastScrollTime";
    private static final String KEY_REEL_COUNT = "lastReelCount";
    private static final String KEY_SCORE = "lastProductivityScore";
    private static final String KEY_DATE = "lastSavedDate";
    private static final String KEY_NOTIFICATION_DATE = "lastNotificationDate";

    private static final String KEY_INSTAGRAM_SECONDS = "instagramSeconds";
    private static final String KEY_FACEBOOK_SECONDS = "facebookSeconds";
    private static final String KEY_TIKTOK_SECONDS = "tiktokSeconds";
    private static final String KEY_YOUTUBE_SECONDS = "youtubeSeconds";

    private static final String KEY_FOCUS_STREAK = "focusStreak";

    private static final int SOFT_WARNING_MIN = 30;
    private static final int SERIOUS_WARNING_MIN = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        connectViews();
        setupBottomNavigation();

        loadStats();

        if (!PermissionUtils.hasUsageStatsPermission(this)) {
            startActivity(new Intent(DashboardActivity.this, PermissionActivity.class));
            finish();
            return;
        }

        if (hasNotificationPermission()) {
            updateStats();
        } else {
            requestNotificationPermission();
        }
    }

    private void connectViews() {
        tvScrollTime = findViewById(R.id.tvScrollTime);
        tvReels = findViewById(R.id.tvReels);
        tvScore = findViewById(R.id.tvScore);
        tvScoreStatus = findViewById(R.id.tvScoreStatus);
        tvQuote = findViewById(R.id.tvQuote);
        tvFocusStreak = findViewById(R.id.tvFocusStreak);

        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navFocus = findViewById(R.id.navFocus);
        navLearn = findViewById(R.id.navLearn);
    }

    private void setupBottomNavigation() {
        navHome.setTextColor(getResources().getColor(android.R.color.white));

        navAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AnalyticsActivity.class));
        });

        navFocus.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, FocusModeActivity.class));
        });

        navLearn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, LearnActivity.class));
        });
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        } else {
            updateStats();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            updateStats();
        }
    }

    private void updateStats() {
        UsageHelper usageHelper = new UsageHelper(this);

        Map<String, Long> appUsageMap = usageHelper.getTrackedAppsUsage();

        long instagramSeconds = getSafeSeconds(appUsageMap, "Instagram");
        long facebookSeconds = getSafeSeconds(appUsageMap, "Facebook");
        long tiktokSeconds = getSafeSeconds(appUsageMap, "TikTok");
        long youtubeSeconds = getSafeSeconds(appUsageMap, "YouTube");

        long totalSeconds = instagramSeconds + facebookSeconds + tiktokSeconds + youtubeSeconds;

        int totalMinutes = (int) (totalSeconds / 60);
        int totalReels = usageHelper.estimateReels(totalSeconds);
        int score = calculateProductivityScore(totalMinutes);
        String todayDate = getTodayDate();
        String quote = getRandomQuote();

        updateDashboardUI(totalMinutes, totalReels, score, quote);

        saveStats(
                totalSeconds,
                totalReels,
                score,
                todayDate,
                instagramSeconds,
                facebookSeconds,
                tiktokSeconds,
                youtubeSeconds
        );

        checkAndShowScrollAlert(totalMinutes, totalReels, todayDate);
    }

    private long getSafeSeconds(Map<String, Long> appUsageMap, String appName) {
        Long seconds = appUsageMap.get(appName);
        return seconds == null ? 0 : seconds;
    }

    private void updateDashboardUI(int totalMinutes, int reels, int score, String quote) {
        tvScrollTime.setText(formatMinutes(totalMinutes));
        tvReels.setText(String.valueOf(reels));
        tvScore.setText(score + "%");
        tvScoreStatus.setText(getProductivityStatus(score));
        tvQuote.setText(quote);
    }

    private void saveStats(
            long totalSeconds,
            int reels,
            int score,
            String date,
            long instagramSeconds,
            long facebookSeconds,
            long tiktokSeconds,
            long youtubeSeconds
    ) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(KEY_SCROLL_TIME, totalSeconds);
        editor.putInt(KEY_REEL_COUNT, reels);
        editor.putInt(KEY_SCORE, score);
        editor.putString(KEY_DATE, date);

        editor.putLong(KEY_INSTAGRAM_SECONDS, instagramSeconds);
        editor.putLong(KEY_FACEBOOK_SECONDS, facebookSeconds);
        editor.putLong(KEY_TIKTOK_SECONDS, tiktokSeconds);
        editor.putLong(KEY_YOUTUBE_SECONDS, youtubeSeconds);

        editor.apply();
    }

    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        long lastScrollTime = prefs.getLong(KEY_SCROLL_TIME, 0);
        int lastReelCount = prefs.getInt(KEY_REEL_COUNT, 0);
        int lastScore = prefs.getInt(KEY_SCORE, 100);
        String lastDate = prefs.getString(KEY_DATE, "Today");
        int focusStreak = prefs.getInt(KEY_FOCUS_STREAK, 0);

        int lastMinutes = (int) (lastScrollTime / 60);

        tvScrollTime.setText(formatMinutes(lastMinutes));
        tvReels.setText(String.valueOf(lastReelCount));
        tvScore.setText(lastScore + "%");
        tvScoreStatus.setText(getProductivityStatus(lastScore));
        tvQuote.setText("Saved stats for: " + lastDate);
        tvFocusStreak.setText("Current Streak: " + focusStreak + " days");
    }

    private String formatMinutes(int totalMinutes) {
        if (totalMinutes < 60) {
            return totalMinutes + " min";
        }

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (minutes == 0) {
            return hours + " hr";
        }

        return hours + " hr " + minutes + " min";
    }

    private int calculateProductivityScore(int totalMinutes) {
        int score;

        if (totalMinutes <= 30) {
            score = 100 - totalMinutes;
        } else if (totalMinutes <= 90) {
            score = 70 - ((totalMinutes - 30) / 2);
        } else if (totalMinutes <= 180) {
            score = 40 - ((totalMinutes - 90) / 4);
        } else {
            score = 20 - ((totalMinutes - 180) / 30);
        }

        if (score < 0) {
            score = 0;
        }

        if (score > 100) {
            score = 100;
        }

        return score;
    }

    private String getProductivityStatus(int score) {
        if (score >= 80) {
            return "Excellent Focus";
        } else if (score >= 60) {
            return "Good Balance";
        } else if (score >= 30) {
            return "Needs Control";
        } else {
            return "High Distraction";
        }
    }

    private void checkAndShowScrollAlert(int totalMinutes, int reels, String todayDate) {
        if (totalMinutes <= SOFT_WARNING_MIN) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastNotificationDate = prefs.getString(KEY_NOTIFICATION_DATE, "");

        if (!lastNotificationDate.equals(todayDate)) {

            String title;
            String message;

            if (totalMinutes >= SERIOUS_WARNING_MIN) {
                title = "High Distraction Alert";
                message = "You crossed " + formatMinutes(totalMinutes)
                        + " of scrolling today. Start a focus session now.";
            } else {
                title = "Take a Break";
                message = "You watched approx. " + reels + " reels. Time to study!";
            }

            NotificationHelper.showNotification(
                    this,
                    title,
                    message
            );

            prefs.edit()
                    .putString(KEY_NOTIFICATION_DATE, todayDate)
                    .apply();
        }
    }

    private String getRandomQuote() {
        String[] quotes = {
                "Discipline beats motivation.",
                "Focus is your superpower.",
                "Stop scrolling, start growing.",
                "Your future self is watching you.",
                "Small focus today, big results tomorrow."
        };

        int randomIndex = (int) (Math.random() * quotes.length);
        return quotes[randomIndex];
    }

    private String getTodayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}