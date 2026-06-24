package com.example.focusflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tvAnalyticsTotalTime, tvAnalyticsTotalReels, tvAnalyticsScore, tvMostUsedApp;
    private TextView tvInstagramAnalytics, tvFacebookAnalytics, tvTikTokAnalytics, tvYouTubeAnalytics;
    private TextView tvInstagramPercent, tvFacebookPercent, tvTikTokPercent, tvYouTubePercent;
    private TextView tvDailyReport;

    private TextView navHome, navAnalytics, navFocus, navLearn;

    private ProgressBar pbInstagram, pbFacebook, pbTikTok, pbYouTube;

    private static final String PREFS_NAME = "FocusFlowPrefs";

    private static final String KEY_SCROLL_TIME = "lastScrollTime";
    private static final String KEY_REEL_COUNT = "lastReelCount";
    private static final String KEY_SCORE = "lastProductivityScore";

    private static final String KEY_INSTAGRAM_SECONDS = "instagramSeconds";
    private static final String KEY_FACEBOOK_SECONDS = "facebookSeconds";
    private static final String KEY_TIKTOK_SECONDS = "tiktokSeconds";
    private static final String KEY_YOUTUBE_SECONDS = "youtubeSeconds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        connectViews();
        setupBottomNavigation();
        loadAnalyticsData();
    }

    private void connectViews() {
        tvAnalyticsTotalTime = findViewById(R.id.tvAnalyticsTotalTime);
        tvAnalyticsTotalReels = findViewById(R.id.tvAnalyticsTotalReels);
        tvAnalyticsScore = findViewById(R.id.tvAnalyticsScore);
        tvMostUsedApp = findViewById(R.id.tvMostUsedApp);
        tvDailyReport = findViewById(R.id.tvDailyReport);

        tvInstagramAnalytics = findViewById(R.id.tvInstagramAnalytics);
        tvFacebookAnalytics = findViewById(R.id.tvFacebookAnalytics);
        tvTikTokAnalytics = findViewById(R.id.tvTikTokAnalytics);
        tvYouTubeAnalytics = findViewById(R.id.tvYouTubeAnalytics);

        tvInstagramPercent = findViewById(R.id.tvInstagramPercent);
        tvFacebookPercent = findViewById(R.id.tvFacebookPercent);
        tvTikTokPercent = findViewById(R.id.tvTikTokPercent);
        tvYouTubePercent = findViewById(R.id.tvYouTubePercent);

        pbInstagram = findViewById(R.id.pbInstagram);
        pbFacebook = findViewById(R.id.pbFacebook);
        pbTikTok = findViewById(R.id.pbTikTok);
        pbYouTube = findViewById(R.id.pbYouTube);

        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navFocus = findViewById(R.id.navFocus);
        navLearn = findViewById(R.id.navLearn);
    }

    private void setupBottomNavigation() {
        navAnalytics.setTextColor(getResources().getColor(android.R.color.white));

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(AnalyticsActivity.this, DashboardActivity.class));
            finish();
        });

        navFocus.setOnClickListener(v -> {
            startActivity(new Intent(AnalyticsActivity.this, FocusModeActivity.class));
            finish();
        });

        navLearn.setOnClickListener(v -> {
            startActivity(new Intent(AnalyticsActivity.this, LearnActivity.class));
            finish();
        });
    }

    private void loadAnalyticsData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        long totalSeconds = prefs.getLong(KEY_SCROLL_TIME, 0);
        int totalReels = prefs.getInt(KEY_REEL_COUNT, 0);
        int score = prefs.getInt(KEY_SCORE, 100);

        long instagramSeconds = prefs.getLong(KEY_INSTAGRAM_SECONDS, 0);
        long facebookSeconds = prefs.getLong(KEY_FACEBOOK_SECONDS, 0);
        long tiktokSeconds = prefs.getLong(KEY_TIKTOK_SECONDS, 0);
        long youtubeSeconds = prefs.getLong(KEY_YOUTUBE_SECONDS, 0);

        int totalMinutes = (int) (totalSeconds / 60);

        tvAnalyticsTotalTime.setText(formatMinutes(totalMinutes));
        tvAnalyticsTotalReels.setText(String.valueOf(totalReels));
        tvAnalyticsScore.setText(score + "%\n" + getProductivityStatus(score));

        int instagramMinutes = (int) (instagramSeconds / 60);
        int facebookMinutes = (int) (facebookSeconds / 60);
        int tiktokMinutes = (int) (tiktokSeconds / 60);
        int youtubeMinutes = (int) (youtubeSeconds / 60);

        int instagramPercent = calculatePercentage(instagramSeconds, totalSeconds);
        int facebookPercent = calculatePercentage(facebookSeconds, totalSeconds);
        int tiktokPercent = calculatePercentage(tiktokSeconds, totalSeconds);
        int youtubePercent = calculatePercentage(youtubeSeconds, totalSeconds);

        tvInstagramAnalytics.setText("Instagram • " + formatMinutes(instagramMinutes));
        tvFacebookAnalytics.setText("Facebook • " + formatMinutes(facebookMinutes));
        tvTikTokAnalytics.setText("TikTok • " + formatMinutes(tiktokMinutes));
        tvYouTubeAnalytics.setText("YouTube • " + formatMinutes(youtubeMinutes));

        tvInstagramPercent.setText(instagramPercent + "%");
        tvFacebookPercent.setText(facebookPercent + "%");
        tvTikTokPercent.setText(tiktokPercent + "%");
        tvYouTubePercent.setText(youtubePercent + "%");

        pbInstagram.setProgress(instagramPercent);
        pbFacebook.setProgress(facebookPercent);
        pbTikTok.setProgress(tiktokPercent);
        pbYouTube.setProgress(youtubePercent);

        String mostUsedAppText = findMostUsedApp(
                instagramSeconds,
                facebookSeconds,
                tiktokSeconds,
                youtubeSeconds
        );

        tvMostUsedApp.setText(mostUsedAppText);

        tvDailyReport.setText(generateDailyReport(
                totalMinutes,
                totalReels,
                score,
                mostUsedAppText
        ));
    }

    private int calculatePercentage(long appSeconds, long totalSeconds) {
        if (totalSeconds <= 0) {
            return 0;
        }

        return (int) ((appSeconds * 100) / totalSeconds);
    }

    private String findMostUsedApp(
            long instagramSeconds,
            long facebookSeconds,
            long tiktokSeconds,
            long youtubeSeconds
    ) {
        String appName = "No usage yet";
        long maxSeconds = 0;

        if (instagramSeconds > maxSeconds) {
            maxSeconds = instagramSeconds;
            appName = "Instagram";
        }

        if (facebookSeconds > maxSeconds) {
            maxSeconds = facebookSeconds;
            appName = "Facebook";
        }

        if (tiktokSeconds > maxSeconds) {
            maxSeconds = tiktokSeconds;
            appName = "TikTok";
        }

        if (youtubeSeconds > maxSeconds) {
            maxSeconds = youtubeSeconds;
            appName = "YouTube";
        }

        if (maxSeconds == 0) {
            return "Most Used App: No usage yet";
        }

        int minutes = (int) (maxSeconds / 60);
        return "Most Used App: " + appName + " • " + formatMinutes(minutes);
    }

    private String generateDailyReport(
            int totalMinutes,
            int totalReels,
            int score,
            String mostUsedAppText
    ) {
        if (totalMinutes == 0) {
            return "No tracked scrolling activity found yet today.\n"
                    + "Open the dashboard after using tracked apps to generate a report.";
        }

        String status = getProductivityStatus(score);
        String suggestion;

        if (score >= 80) {
            suggestion = "Great control today. Keep protecting your focus.";
        } else if (score >= 60) {
            suggestion = "Your usage is still balanced. A short focus session can keep your momentum strong.";
        } else if (score >= 30) {
            suggestion = "Your scrolling is starting to affect focus. Try a 30-minute focus session.";
        } else {
            suggestion = "Heavy distraction detected. Replace your next scrolling session with study or focus mode.";
        }

        return "You spent " + formatMinutes(totalMinutes) + " on tracked distracting apps today.\n"
                + "Estimated reels/shorts watched: " + totalReels + ".\n"
                + mostUsedAppText + ".\n"
                + "Productivity score: " + score + "%.\n"
                + "Status: " + status + ".\n\n"
                + "Suggestion: " + suggestion;
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
}