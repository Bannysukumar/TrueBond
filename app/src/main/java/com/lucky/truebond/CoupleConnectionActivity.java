package com.lucky.truebond;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CoupleConnectionActivity extends AppCompatActivity {

    private TextView textViewMyCode;
    private EditText editTextPartnerCode;
    private Button buttonShareCode, buttonConnect;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couple_connection);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize views
        textViewMyCode = findViewById(R.id.textViewMyCode);
        editTextPartnerCode = findViewById(R.id.editTextPartnerCode);
        buttonShareCode = findViewById(R.id.buttonShareCode);
        buttonConnect = findViewById(R.id.buttonConnect);

        // Load user's couple code
        loadUserCoupleCode();

        // Set click listeners
        buttonShareCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCoupleCode();
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWithPartner();
            }
        });
    }

    private void loadUserCoupleCode() {
        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !TextUtils.isEmpty(user.getCoupleCode())) {
                        textViewMyCode.setText(user.getCoupleCode());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CoupleConnectionActivity.this, 
                    "Failed to load couple code: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareCoupleCode() {
        String coupleCode = textViewMyCode.getText().toString();
        if (!TextUtils.isEmpty(coupleCode)) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "Hey! Let's connect on TrueBond. My couple code is: " + coupleCode);
            startActivity(Intent.createChooser(shareIntent, "Share Couple Code"));
        }
    }

    private void connectWithPartner() {
        String partnerCode = editTextPartnerCode.getText().toString().trim();
        if (TextUtils.isEmpty(partnerCode)) {
            editTextPartnerCode.setError("Please enter partner's code");
            return;
        }

        // Search for user with matching couple code
        mDatabase.child("users").orderByChild("coupleCode").equalTo(partnerCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String partnerId = userSnapshot.getKey();
                                if (partnerId != null && !partnerId.equals(currentUserId)) {
                                    // Update both users with partner IDs
                                    updatePartnerConnection(partnerId);
                                    return;
                                }
                            }
                            Toast.makeText(CoupleConnectionActivity.this, 
                                "Invalid couple code", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CoupleConnectionActivity.this, 
                                "No user found with this code", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CoupleConnectionActivity.this, 
                            "Connection failed: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePartnerConnection(String partnerId) {
        // Update current user's partner ID
        mDatabase.child("users").child(currentUserId).child("partnerId").setValue(partnerId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update partner's partner ID
                        mDatabase.child("users").child(partnerId).child("partnerId")
                                .setValue(currentUserId)
                                .addOnCompleteListener(partnerTask -> {
                                    if (partnerTask.isSuccessful()) {
                                        Toast.makeText(CoupleConnectionActivity.this, 
                                            "Successfully connected with partner!", 
                                            Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(CoupleConnectionActivity.this, 
                                            MainActivity.class));
                                        finish();
                                    } else {
                                        // Revert current user's partner ID if partner update fails
                                        mDatabase.child("users").child(currentUserId)
                                                .child("partnerId").removeValue();
                                        Toast.makeText(CoupleConnectionActivity.this, 
                                            "Connection failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(CoupleConnectionActivity.this, 
                            "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 