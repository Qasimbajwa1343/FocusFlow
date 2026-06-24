package com.example.focusflow;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FocusModeActivity extends AppCompatActivity {

    private TextView tvTimer, tvFocusStatus, tvCompletedSessions;
    private TextView tvStartFocusText, tvResetFocusText, tvReminderTime;

    private CardView btn30Min, btn1Hour, btn2Hour;
    private CardView btnStartFocus, btnResetFocus, cardSetReminder;

    private TextView navHome, navAnalytics, navFocus, navLearn;

    private CountDownTimer countDownTimer;

    private long selectedTimeMillis = 30 * 60 * 1000;
    private long remainingTimeMillis = selectedTimeMillis;

    private boolean isTimerRunning = false;

    private static final String PREFS_NAME = "FocusFlowPrefs";

    private static final String KEY_COMPLETED_FOCUS_SESSIONS = "completedFocusSessions";
    private static final String KEY_FOCUS_STREAK = "focusStreak";
    private static final String KEY_LAST_STREAK_DATE = "lastStreakDate";

    private static final String KEY_REMINDER_HOUR = "reminderHour";
    private static final String KEY_REMINDER_MINUTE = "reminderMinute";

    private static final int REMINDER_REQUEST_CODE = 900;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_mode);

        connectViews();
        setupButtons();
        setupBottomNavigation();

        loadCompletedSessions();
        loadReminderTime();
        updateTimerText();
    }

    private void connectViews() {
        tvTimer = findViewById(R.id.tvTimer);
        tvFocusStatus = findViewById(R.id.tvFocusStatus);
        tvCompletedSessions = findViewById(R.id.tvCompletedSessions);

        tvStartFocusText = findViewById(R.id.tvStartFocusText);
        tvResetFocusText = findViewById(R.id.tvResetFocusText);
        tvReminderTime = findViewById(R.id.tvReminderTime);

        btn30Min = findViewById(R.id.btn30Min);
        btn1Hour = findViewById(R.id.btn1Hour);
        btn2Hour = findViewById(R.id.btn2Hour);

        btnStartFocus = findViewById(R.id.btnStartFocus);
        btnResetFocus = findViewById(R.id.btnResetFocus);
        cardSetReminder = findViewById(R.id.cardSetReminder);

        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navFocus = findViewById(R.id.navFocus);
        navLearn = findViewById(R.id.navLearn);
    }

    private void setupButtons() {
        btn30Min.setOnClickListener(v -> selectFocusTime(30));
        btn1Hour.setOnClickListener(v -> selectFocusTime(60));
        btn2Hour.setOnClickListener(v -> selectFocusTime(120));

        btnStartFocus.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnResetFocus.setOnClickListener(v -> resetTimer());

        cardSetReminder.setOnClickListener(v -> showReminderTimePicker());
    }

    private void setupBottomNavigation() {
        navFocus.setTextColor(getResources().getColor(android.R.color.white));

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(FocusModeActivity.this, DashboardActivity.class));
            finish();
        });

        navAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(FocusModeActivity.this, AnalyticsActivity.class));
            finish();
        });

        navLearn.setOnClickListener(v -> {
            startActivity(new Intent(FocusModeActivity.this, LearnActivity.class));
            finish();
        });
    }

    private void selectFocusTime(int minutes) {
        if (isTimerRunning) {
            Toast.makeText(this, "Pause or reset the timer before changing duration.", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedTimeMillis = minutes * 60L * 1000L;
        remainingTimeMillis = selectedTimeMillis;

        if (minutes < 60) {
            tvFocusStatus.setText(minutes + " minute focus session selected");
        } else {
            int hours = minutes / 60;
            tvFocusStatus.setText(hours + " hour focus session selected");
        }

        updateTimerText();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                remainingTimeMillis = 0;

                tvStartFocusText.setText("Start Focus");
                tvFocusStatus.setText("Focus session completed. Nice work!");

                saveCompletedSession();
                updateFocusStreak();
                loadCompletedSessions();

                Toast.makeText(FocusModeActivity.this, "Focus session completed!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        tvStartFocusText.setText("Pause Focus");
        tvFocusStatus.setText("Focus mode is running. Stay locked in.");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isTimerRunning = false;
        tvStartFocusText.setText("Resume Focus");
        tvFocusStatus.setText("Focus session paused.");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isTimerRunning = false;
        remainingTimeMillis = selectedTimeMillis;

        tvStartFocusText.setText("Start Focus");
        tvFocusStatus.setText("Timer reset. Choose your focus and start again.");

        updateTimerText();
    }

    private void updateTimerText() {
        int totalSeconds = (int) (remainingTimeMillis / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String formattedTime;

        if (hours > 0) {
            formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }

        tvTimer.setText(formattedTime);
    }

    private void saveCompletedSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int completedSessions = prefs.getInt(KEY_COMPLETED_FOCUS_SESSIONS, 0);

        prefs.edit()
                .putInt(KEY_COMPLETED_FOCUS_SESSIONS, completedSessions + 1)
                .apply();
    }

    private void updateFocusStreak() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String todayDate = getTodayDate();
        String lastStreakDate = prefs.getString(KEY_LAST_STREAK_DATE, "");
        int currentStreak = prefs.getInt(KEY_FOCUS_STREAK, 0);

        if (!todayDate.equals(lastStreakDate)) {
            currentStreak++;

            prefs.edit()
                    .putInt(KEY_FOCUS_STREAK, currentStreak)
                    .putString(KEY_LAST_STREAK_DATE, todayDate)
                    .apply();
        }
    }

    private void loadCompletedSessions() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int completedSessions = prefs.getInt(KEY_COMPLETED_FOCUS_SESSIONS, 0);

        tvCompletedSessions.setText("Completed Focus Sessions: " + completedSessions);
    }

    private void showReminderTimePicker() {
        Calendar currentTime = Calendar.getInstance();

        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    saveReminderTime(hourOfDay, minute);
                    scheduleStudyReminder(hourOfDay, minute);
                    updateReminderText(hourOfDay, minute);

                    Toast.makeText(this, "Study reminder set!", Toast.LENGTH_SHORT).show();
                },
                currentHour,
                currentMinute,
                false
        );

        timePickerDialog.show();
    }

    private void saveReminderTime(int hour, int minute) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        prefs.edit()
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .apply();
    }

    private void loadReminderTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int hour = prefs.getInt(KEY_REMINDER_HOUR, -1);
        int minute = prefs.getInt(KEY_REMINDER_MINUTE, -1);

        if (hour == -1 || minute == -1) {
            tvReminderTime.setText("No reminder set");
        } else {
            updateReminderText(hour, minute);
        }
    }

    private void updateReminderText(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour % 12;

        if (displayHour == 0) {
            displayHour = 12;
        }

        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
        tvReminderTime.setText("Reminder set for " + formattedTime);
    }

    private void scheduleStudyReminder(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If selected time already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private String getTodayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}