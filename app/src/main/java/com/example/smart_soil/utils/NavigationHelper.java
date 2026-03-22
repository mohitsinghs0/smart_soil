package com.example.smart_soil.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.example.smart_soil.R;
import com.example.smart_soil.activities.*;

public class NavigationHelper {

    public static void setupCustomNav(Activity activity, int activeId) {
        View historyBtn = activity.findViewById(R.id.btn_nav_history);
        View aboutBtn = activity.findViewById(R.id.btn_nav_about);
        View profileBtn = activity.findViewById(R.id.btn_nav_profile);
        View settingsBtn = activity.findViewById(R.id.btn_nav_settings);
        View centerBtn = activity.findViewById(R.id.fab_nav_center);

        // If core navigation elements are missing, skip setup to avoid crash
        if (historyBtn == null && aboutBtn == null && profileBtn == null && centerBtn == null) {
            return;
        }

        // Reset all icons and dots (with null checks inside helper methods)
        resetIcon(activity, R.id.iv_nav_history, R.id.dot_nav_history);
        resetIcon(activity, R.id.iv_nav_about, R.id.dot_nav_about);
        resetIcon(activity, R.id.iv_nav_profile, R.id.dot_nav_profile);
        resetIcon(activity, R.id.iv_nav_settings, R.id.dot_nav_settings);

        // Set active icon and dot based on the current page
        if (activeId == R.id.btn_nav_history) setActive(activity, R.id.iv_nav_history, R.id.dot_nav_history);
        else if (activeId == R.id.btn_nav_about) setActive(activity, R.id.iv_nav_about, R.id.dot_nav_about);
        else if (activeId == R.id.btn_nav_profile) setActive(activity, R.id.iv_nav_profile, R.id.dot_nav_profile);
        else if (activeId == R.id.btn_nav_settings) setActive(activity, R.id.iv_nav_settings, R.id.dot_nav_settings);

        // Click listeners with safe checks
        if (historyBtn != null) {
            historyBtn.setOnClickListener(v -> {
                if (activeId != R.id.btn_nav_history) {
                    Intent intent = new Intent(activity, HistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (aboutBtn != null) {
            aboutBtn.setOnClickListener(v -> {
                if (activeId != R.id.btn_nav_about) {
                    Intent intent = new Intent(activity, AboutActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (centerBtn != null) {
            centerBtn.setOnClickListener(v -> {
                if (!(activity instanceof DashboardActivity)) {
                    Intent intent = new Intent(activity, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (profileBtn != null) {
            profileBtn.setOnClickListener(v -> {
                if (activeId != R.id.btn_nav_profile) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                Toast.makeText(activity, "Settings coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private static void setActive(Activity activity, int iconId, int dotId) {
        ImageView iv = activity.findViewById(iconId);
        View dot = activity.findViewById(dotId);
        if (iv != null) iv.setColorFilter(ContextCompat.getColor(activity, R.color.brand_green));
        if (dot != null) dot.setVisibility(View.VISIBLE);
    }

    private static void resetIcon(Activity activity, int iconId, int dotId) {
        View view = activity.findViewById(iconId);
        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(ContextCompat.getColor(activity, R.color.text_hint));
        }
        View dot = activity.findViewById(dotId);
        if (dot != null) dot.setVisibility(View.INVISIBLE);
    }
}
