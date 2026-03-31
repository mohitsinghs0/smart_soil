package com.example.smart_soil.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.adapters.ChatAdapter;
import com.example.smart_soil.models.AIChatMessage;
import com.example.smart_soil.models.AIChatSession;
import com.example.smart_soil.services.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AIChatActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<AIChatMessage> messageList = new ArrayList<>();
    private EditText messageInput;
    private MaterialButton sendButton;
    private Long sessionId;
    private Long soilTestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        adapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        soilTestId = (long) getIntent().getIntExtra("soil_test_id", -1);
        sessionId = getIntent().getLongExtra("session_id", -1);

        if (sessionId != -1) {
            loadMessages();
        } else if (soilTestId != -1) {
            createSession();
        } else {
            Toast.makeText(this, "No test selected for chat", Toast.LENGTH_SHORT).show();
            finish();
        }

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void createSession() {
        Map<String, Long> payload = new HashMap<>();
        payload.put("soil_test_id", soilTestId);

        RetrofitClient.getApiService().createAIChatSession(getAuthToken(), payload).enqueue(new Callback<AIChatSession>() {
            @Override
            public void onResponse(Call<AIChatSession> call, Response<AIChatSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionId = response.body().id;
                    // Start with a greeting from AI
                    AIChatMessage welcomeMsg = new AIChatMessage();
                    welcomeMsg.role = "assistant";
                    welcomeMsg.content = "Hello! I am your Soil Expert AI. I've analyzed your soil test results. How can I help you today?";
                    messageList.add(welcomeMsg);
                    adapter.notifyItemInserted(0);
                } else {
                    Toast.makeText(AIChatActivity.this, "Failed to start AI session", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIChatSession> call, Throwable t) {
                Toast.makeText(AIChatActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        RetrofitClient.getApiService().getAIChatMessages(getAuthToken(), sessionId).enqueue(new Callback<List<AIChatMessage>>() {
            @Override
            public void onResponse(Call<List<AIChatMessage>> call, Response<List<AIChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.clear();
                    messageList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<List<AIChatMessage>> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty() || sessionId == null) return;

        AIChatMessage userMsg = new AIChatMessage(sessionId, "user", content);
        messageList.add(userMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
        messageInput.setText("");

        RetrofitClient.getApiService().sendAIChatMessage(getAuthToken(), userMsg).enqueue(new Callback<AIChatMessage>() {
            @Override
            public void onResponse(Call<AIChatMessage> call, Response<AIChatMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.add(response.body());
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<AIChatMessage> call, Throwable t) {
                Toast.makeText(AIChatActivity.this, "Failed to get AI response", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
