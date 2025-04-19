package com.lucky.truebond;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private SwitchMaterial switchAnniversaryReminders;
    private SwitchMaterial switchDailyMessages;
    private SwitchMaterial switchConnectionUpdates;
    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        switchAnniversaryReminders = findViewById(R.id.switchAnniversaryReminders);
        switchDailyMessages = findViewById(R.id.switchDailyMessages);
        switchConnectionUpdates = findViewById(R.id.switchConnectionUpdates);
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);

        // Setup RecyclerView
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(new ArrayList<>());
        recyclerViewNotifications.setAdapter(notificationAdapter);

        // Load notification settings
        loadNotificationSettings();

        // Load notifications
        loadNotifications();

        // Set switch listeners
        setupSwitchListeners();
    }

    private void loadNotificationSettings() {
        mDatabase.child("users").child(currentUserId).child("notificationSettings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            switchAnniversaryReminders.setChecked(
                                    Boolean.TRUE.equals(snapshot.child("anniversaryReminders").getValue(Boolean.class)));
                            switchDailyMessages.setChecked(
                                    Boolean.TRUE.equals(snapshot.child("dailyMessages").getValue(Boolean.class)));
                            switchConnectionUpdates.setChecked(
                                    Boolean.TRUE.equals(snapshot.child("connectionUpdates").getValue(Boolean.class)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(NotificationsActivity.this, 
                            "Failed to load notification settings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSwitchListeners() {
        switchAnniversaryReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationSetting("anniversaryReminders", isChecked);
        });

        switchDailyMessages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationSetting("dailyMessages", isChecked);
        });

        switchConnectionUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationSetting("connectionUpdates", isChecked);
        });
    }

    private void updateNotificationSetting(String setting, boolean value) {
        mDatabase.child("users").child(currentUserId).child("notificationSettings")
                .child(setting).setValue(value)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(NotificationsActivity.this, 
                            "Failed to update notification settings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadNotifications() {
        mDatabase.child("users").child(currentUserId).child("notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                            Notification notification = notificationSnapshot.getValue(Notification.class);
                            if (notification != null) {
                                notifications.add(notification);
                            }
                        }
                        notificationAdapter.updateNotifications(notifications);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(NotificationsActivity.this, 
                            "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 