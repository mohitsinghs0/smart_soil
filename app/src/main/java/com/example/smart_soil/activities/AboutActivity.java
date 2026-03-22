package com.example.smart_soil.activities;

import android.os.Bundle;
import com.example.smart_soil.R;
import com.example.smart_soil.utils.NavigationHelper;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, R.id.btn_nav_about);
    }
}
