package com.example.smart_soil.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import com.example.smart_soil.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ContactActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        MaterialButton submitButton = findViewById(R.id.submit_query_button);
        submitButton.setOnClickListener(v -> 
            Toast.makeText(this, "Query submitted!", Toast.LENGTH_SHORT).show()
        );

        CardView callSupportCard = findViewById(R.id.call_support_card);
        callSupportCard.setOnClickListener(v -> 
            Toast.makeText(this, "Calling support...", Toast.LENGTH_SHORT).show()
        );
    }
}
