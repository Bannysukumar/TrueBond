package com.lucky.truebond;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private MaterialButton buttonSend;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String partnerId;
    private String chatId;
    private DatabaseReference mStreakRef;
    private Streak currentStreak;
    private TextView streakCount;
    private TextView streakIndicator;
    private StreakManager currentStreakManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize streak reference
        mStreakRef = mDatabase.child("streaks");

        // Initialize streak-related views
        streakCount = findViewById(R.id.streakCount);
        streakIndicator = findViewById(R.id.streakIndicator);
        currentStreakManager = new StreakManager(this);

        // Get partner ID from intent or database
        partnerId = getIntent().getStringExtra("partnerId");
        if (partnerId == null) {
            loadPartnerId();
        } else {
            setupChat();
            loadStreakData();
        }

        // Initialize views
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Setup RecyclerView
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, currentUserId, new MessageAdapter.OnMessageLongClickListener() {
            @Override
            public void onMessageLongClick(Message message) {
                // Handle message deletion
                deleteMessage(message);
            }
        });
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // Set click listener for send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Add menu item for love report
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_love_report) {
            showLoveReport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPartnerId() {
        mDatabase.child("users").child(currentUserId).child("partnerId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            partnerId = snapshot.getValue(String.class);
                            setupChat();
                            loadStreakData();
                        } else {
                            Toast.makeText(ChatActivity.this, "No partner connected", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Failed to load partner", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setupChat() {
        if (currentUserId == null || partnerId == null) {
            Toast.makeText(this, "Error: Missing user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate chat ID by sorting user IDs
        chatId = currentUserId.compareTo(partnerId) < 0 
                ? currentUserId + "_" + partnerId 
                : partnerId + "_" + currentUserId;

        // Load messages
        mDatabase.child("chats").child(chatId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        recyclerViewMessages.scrollToPosition(messages.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadStreakData() {
        String streakId = currentUserId.compareTo(partnerId) < 0 
            ? currentUserId + "_" + partnerId 
            : partnerId + "_" + currentUserId;

        mStreakRef.child(streakId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentStreak = snapshot.getValue(Streak.class);
                } else {
                    currentStreak = new Streak(currentUserId, partnerId);
                    mStreakRef.child(streakId).setValue(currentStreak);
                }
                updateStreakUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load streak data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStreakUI() {
        if (currentStreakManager.getCurrentStreak() > 0) {
            streakIndicator.setVisibility(View.VISIBLE);
            streakCount.setText(String.valueOf(currentStreakManager.getCurrentStreak()));
            streakIndicator.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_fire, 0, 0, 0
            );
        } else {
            streakIndicator.setVisibility(View.GONE);
        }
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty() || chatId == null) return;

        // Generate message ID
        String messageId = mDatabase.child("chats").child(chatId).child("messages").push().getKey();
        if (messageId == null) return;

        // Create message with correct constructor
        Message message = new Message(messageText, currentUserId, partnerId);
        message.setId(messageId);

        // Save message to correct path
        mDatabase.child("chats").child(chatId).child("messages").child(messageId).setValue(message)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    editTextMessage.setText("");
                    
                    // Update streak
                    if (currentStreak != null) {
                        currentStreak.updateStreak();
                        String streakId = currentUserId.compareTo(partnerId) < 0 
                            ? currentUserId + "_" + partnerId 
                            : partnerId + "_" + currentUserId;
                        mStreakRef.child(streakId).setValue(currentStreak)
                            .addOnCompleteListener(streakTask -> {
                                if (streakTask.isSuccessful()) {
                                    updateStreakUI();
                                }
                            });
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void deleteMessage(Message message) {
        if (chatId == null || message == null) {
            return;
        }
        mDatabase.child("chats").child(chatId).child("messages").child(message.getId()).removeValue();
    }

    private void showLoveReport() {
        // Implement love report functionality
    }

    private void saveReport(LoveReport report) {
        if (chatId == null) {
            return;
        }
        mDatabase.child("reports").child(chatId).push().setValue(report);
    }

    private void cleanupOldData() {
        if (chatId == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30); // 30 days ago
        long cutoffTime = calendar.getTimeInMillis();

        mDatabase.child("chats").child(chatId).child("messages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);
                            if (message != null && message.getTimestamp() < cutoffTime) {
                                messageSnapshot.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chatId != null) {
            cleanupOldData();
        }
    }
} 