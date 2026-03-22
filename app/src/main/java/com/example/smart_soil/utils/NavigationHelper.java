package com.example.smart_soil.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.example.smart_soil.R;
import com.example.smart_soil.activities.*;

public class NavigationHelper {

    public static void setupCustomNav(Activity activity, int activeId) {
        View historyBtn = activity.findViewById(R.id.btn_nav_history);
        View tasksBtn = activity.findViewById(R.id.btn_nav_tasks);
        View bookmarkBtn = activity.findViewById(R.id.btn_nav_bookmark);
        View settingsBtn = activity.findViewById(R.id.btn_nav_settings);
        View centerBtn = activity.findViewById(R.id.fab_nav_center);

        // Reset all
        resetIcon(activity, R.id.iv_nav_history, R.id.dot_nav_history);
        resetIcon(activity, R.id.iv_nav_tasks, R.id.dot_nav_tasks);
        resetIcon(activity, R.id.iv_nav_bookmark, R.id.dot_nav_bookmark);
        resetIcon(activity, R.id.iv_nav_settings, R.id.dot_nav_settings);

        // Set active
        if (activeId == R.id.btn_nav_history) setActive(activity, R.id.iv_nav_history, R.id.dot_nav_history);
        else if (activeId == R.id.btn_nav_tasks) setActive(activity, R.id.iv_nav_tasks, R.id.dot_nav_tasks);
        else if (activeId == R.id.btn_nav_bookmark) setActive(activity, R.id.iv_nav_bookmark, R.id.dot_nav_bookmark);
        else if (activeId == R.id.btn_nav_settings) setActive(activity, R.id.iv_nav_settings, R.id.dot_nav_settings);

        // Click listeners
        historyBtn.setOnClickListener(v -> {
            if (activeId != R.id.btn_nav_history) {
                activity.startActivity(new Intent(activity, HistoryActivity.class));
                activity.overridePendingTransition(0, 0);
            }
        });

        tasksBtn.setOnClickListener(v -> {
            if (activeId != R.id.btn_nav_tasks) {
                activity.startActivity(new Intent(activity, AboutActivity.class));
                activity.overridePendingTransition(0, 0);
            }
        });

        centerBtn.setOnClickListener(v -> {
            if (!(activity instanceof DashboardActivity)) {
                activity.startActivity(new Intent(activity, DashboardActivity.class));
                activity.overridePendingTransition(0, 0);
            }
        });

        bookmarkBtn.setOnClickListener(v -> {
            // Future feature
        });

        settingsBtn.setOnClickListener(v -> {
            if (activeId != R.id.btn_nav_settings) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
                activity.overridePendingTransition(0, 0);
            }
        });
    }

    private static void setActive(Activity activity, int iconId, int dotId) {
        ImageView iv = activity.findViewById(iconId);
        View dot = activity.findViewById(dotId);
        iv.setColorFilter(ContextCompat.getColor(activity, R.color.brand_green));
        dot.setVisibility(View.VISIBLE);
    }

    private static void resetIcon(Activity activity, int iconId, int dotId) {
        ImageView iv = activity.findViewById(iconId);
        View dot = activity.findViewById(dotId);
        iv.setColorFilter(ContextCompat.getColor(activity, R.color.text_hint));
        dot.setVisibility(View.INVISIBLE);
    }
}
