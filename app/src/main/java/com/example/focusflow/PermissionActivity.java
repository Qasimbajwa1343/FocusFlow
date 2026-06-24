package com.example.focusflow;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusflow.utils.PermissionUtils;

public class PermissionActivity extends AppCompatActivity {

    private Button btnGrantPermission;
    private TextView tvPermissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);

        updatePermissionStatus();

        btnGrantPermission.setOnClickListener(v -> openUsageAccessSettings());
    }

    private void openUsageAccessSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);

            Toast.makeText(
                    this,
                    "Find FocusFlow and allow Usage Access",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Could not open Usage Access settings. Open it manually from phone settings.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PermissionUtils.hasUsageStatsPermission(this)) {
            startActivity(new Intent(PermissionActivity.this, DashboardActivity.class));
            finish();
        } else {
            updatePermissionStatus();
        }
    }

    private void updatePermissionStatus() {
        if (tvPermissionStatus == null) {
            return;
        }

        if (PermissionUtils.hasUsageStatsPermission(this)) {
            tvPermissionStatus.setText("Usage Access granted. Opening dashboard...");
        } else {
            tvPermissionStatus.setText("Usage Access not granted yet. Tap the button and enable FocusFlow.");
        }
    }
}