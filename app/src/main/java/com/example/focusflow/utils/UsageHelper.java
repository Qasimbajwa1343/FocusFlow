package com.example.focusflow.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UsageHelper {

    private final UsageStatsManager usageStatsManager;

    public UsageHelper(Context context) {
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    // Get usage time for one specific app in seconds
    public long getAppUsageSeconds(String packageName) {
        long startTime = getTodayStartTime();
        long endTime = System.currentTimeMillis();

        long totalTimeMillis = 0;

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

        if (usageStatsList == null) {
            return 0;
        }

        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getPackageName().equals(packageName)) {
                totalTimeMillis += usageStats.getTotalTimeInForeground();
            }
        }

        return totalTimeMillis / 1000;
    }

    // Get total usage time for multiple apps in seconds
    public long getTotalTimeInForeground(String[] packageNames) {
        long totalSeconds = 0;

        for (String packageName : packageNames) {
            totalSeconds += getAppUsageSeconds(packageName);
        }

        return totalSeconds;
    }

    // Get app-wise usage data
    public Map<String, Long> getTrackedAppsUsage() {
        Map<String, Long> appUsageMap = new LinkedHashMap<>();

        appUsageMap.put("Instagram", getAppUsageSeconds("com.instagram.android"));
        appUsageMap.put("Facebook", getAppUsageSeconds("com.facebook.katana"));
        appUsageMap.put("TikTok", getAppUsageSeconds("com.zhiliaoapp.musically"));
        appUsageMap.put("YouTube", getAppUsageSeconds("com.google.android.youtube"));

        return appUsageMap;
    }

    // Estimate reels/shorts based on seconds
    public int estimateReels(long totalSeconds) {
        return (int) totalSeconds / 20;
    }

    // Start time = today at 12:00 AM
    private long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}