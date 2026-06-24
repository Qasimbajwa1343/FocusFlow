package com.example.focusflow.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;

public class PermissionUtils {

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOpsManager =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if (appOpsManager == null) {
            return false;
        }

        int mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.getPackageName()
        );

        return mode == AppOpsManager.MODE_ALLOWED;
    }
}