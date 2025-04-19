package com.lucky.truebond;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DailyReportActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String partnerId;

    private CircularProgressIndicator progressLoveScore;
    private TextView tvLoveScore;
    private ImageView ivMood;
    private TextView tvMood;
    private TextView tvSuggestions;
    private TextView tvColdZones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        progressLoveScore = findViewById(R.id.progressLoveScore);
        tvLoveScore = findViewById(R.id.tvLoveScore);
        ivMood = findViewById(R.id.ivMood);
        tvMood = findViewById(R.id.tvMood);
        tvSuggestions = findViewById(R.id.tvSuggestions);
        tvColdZones = findViewById(R.id.tvColdZones);

        // Get partner ID and load report
        loadPartnerId();
    }

    private void loadPartnerId() {
        mDatabase.child("connections").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            partnerId = dataSnapshot.getValue(String.class);
                            loadDailyReport();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void loadDailyReport() {
        // Get messages from the last 24 hours
        long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        mDatabase.child("messages").child(currentUserId).child(partnerId)
                .orderByChild("timestamp").startAt(cutoffTime)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Message> messages = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Message message = snapshot.getValue(Message.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        analyzeMessages(messages);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void analyzeMessages(List<Message> messages) {
        // Use LoveAnalyzer to analyze messages
        LoveReport report = LoveAnalyzer.analyzeMessages(messages);

        // Update UI with report data
        updateUI(report);
    }

    private void updateUI(LoveReport report) {
        // Update love score
        progressLoveScore.setProgress(report.getLoveScore());
        tvLoveScore.setText(report.getLoveScore() + "%");

        // Update mood
        String mood = report.getMood();
        tvMood.setText(mood);
        switch (mood) {
            case "POSITIVE":
                ivMood.setImageResource(R.drawable.ic_mood_positive);
                break;
            case "NEGATIVE":
                ivMood.setImageResource(R.drawable.ic_mood_negative);
                break;
            default:
                ivMood.setImageResource(R.drawable.ic_mood_neutral);
        }

        // Update suggestions
        tvSuggestions.setText(report.getSuggestion());

        // Update cold zones
        int coldZones = report.getColdZones();
        if (coldZones > 0) {
            tvColdZones.setText("You had " + coldZones + " cold zone(s) today. " +
                    "These are periods of negative communication that may need attention.");
        } else {
            tvColdZones.setText("No cold zones detected today. Keep up the good communication!");
        }
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