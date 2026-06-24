package com.example.focusflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.focusflow.utils.NotificationHelper;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationHelper.showNotification(
                context,
                "Focus Reminder",
                "Time for your focus session. Stay locked in!"
        );
    }
}