package com.lucky.truebond;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class SinglesActivity extends AppCompatActivity {
    private static final String TAG = "SinglesActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private SwitchMaterial switchSingleStatus;
    private RecyclerView rvFemaleCodes, rvMaleCodes;
    private ConnectionCodeAdapter femaleAdapter, maleAdapter;
    private List<UserCode> femaleCodes, maleCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singles);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        switchSingleStatus = findViewById(R.id.switchSingleStatus);
        rvFemaleCodes = findViewById(R.id.rvFemaleCodes);
        rvMaleCodes = findViewById(R.id.rvMaleCodes);

        // Setup RecyclerViews
        femaleCodes = new ArrayList<>();
        maleCodes = new ArrayList<>();
        
        femaleAdapter = new ConnectionCodeAdapter(femaleCodes);
        maleAdapter = new ConnectionCodeAdapter(maleCodes);

        rvFemaleCodes.setLayoutManager(new LinearLayoutManager(this));
        rvMaleCodes.setLayoutManager(new LinearLayoutManager(this));

        rvFemaleCodes.setAdapter(femaleAdapter);
        rvMaleCodes.setAdapter(maleAdapter);

        // Load current single status
        loadSingleStatus();

        // Set switch listener
        switchSingleStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSingleStatus(isChecked);
        });

        // Load connection codes
        loadConnectionCodes();
    }

    private void loadSingleStatus() {
        Log.d(TAG, "Loading single status for user: " + currentUserId);
        mDatabase.child("users").child(currentUserId).child("isSingle")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            boolean isSingle = snapshot.getValue(Boolean.class);
                            Log.d(TAG, "Single status loaded: " + isSingle);
                            switchSingleStatus.setChecked(isSingle);
                        } else {
                            Log.d(TAG, "No single status found, setting default to false");
                            mDatabase.child("users").child(currentUserId).child("isSingle")
                                    .setValue(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load single status: " + error.getMessage());
                        Toast.makeText(SinglesActivity.this, 
                            "Failed to load single status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSingleStatus(boolean isSingle) {
        Log.d(TAG, "Updating single status to: " + isSingle);
        mDatabase.child("users").child(currentUserId).child("isSingle")
                .setValue(isSingle)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Single status updated successfully");
                        if (isSingle) {
                            Toast.makeText(SinglesActivity.this, 
                                "You are now visible to other singles", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SinglesActivity.this, 
                                "You are no longer visible to other singles", Toast.LENGTH_SHORT).show();
                        }
                        loadConnectionCodes();
                    } else {
                        Log.e(TAG, "Failed to update single status: " + task.getException().getMessage());
                        Toast.makeText(SinglesActivity.this, 
                            "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadConnectionCodes() {
        Log.d(TAG, "Loading connection codes");
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Received users data from Firebase");
                femaleCodes.clear();
                maleCodes.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null && !userId.equals(currentUserId)) {
                        Boolean isSingle = userSnapshot.child("isSingle").getValue(Boolean.class);
                        String gender = userSnapshot.child("gender").getValue(String.class);
                        String name = userSnapshot.child("name").getValue(String.class);
                        String partnerCode = userSnapshot.child("partnerCode").getValue(String.class);
                        Boolean isConnected = userSnapshot.child("isConnected").getValue(Boolean.class);

                        Log.d(TAG, String.format("User data - ID: %s, isSingle: %s, gender: %s, name: %s, partnerCode: %s, isConnected: %s",
                            userId, isSingle, gender, name, partnerCode, isConnected));

                        // Only show users who:
                        // 1. Have enabled single status
                        // 2. Are not already connected
                        // 3. Have all required information
                        if (isSingle != null && isSingle && 
                            isConnected != null && !isConnected &&
                            name != null && partnerCode != null) {
                            
                            UserCode userCode = new UserCode(name, partnerCode);
                            if ("female".equalsIgnoreCase(gender)) {
                                Log.d(TAG, "Adding female code: " + name);
                                femaleCodes.add(userCode);
                            } else if ("male".equalsIgnoreCase(gender)) {
                                Log.d(TAG, "Adding male code: " + name);
                                maleCodes.add(userCode);
                            }
                        }
                    }
                }

                Log.d(TAG, "Found " + femaleCodes.size() + " female codes and " + maleCodes.size() + " male codes");
                femaleAdapter.notifyDataSetChanged();
                maleAdapter.notifyDataSetChanged();

                // Show message if no codes are available
                if (femaleCodes.isEmpty() && maleCodes.isEmpty()) {
                    Log.d(TAG, "No singles available");
                    Toast.makeText(SinglesActivity.this, 
                        "No singles available at the moment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load connection codes: " + error.getMessage());
                Toast.makeText(SinglesActivity.this, 
                    "Failed to load connection codes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class UserCode {
        String name;
        String code;

        UserCode(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    private class ConnectionCodeAdapter extends RecyclerView.Adapter<ConnectionCodeAdapter.ViewHolder> {
        private List<UserCode> codes;

        ConnectionCodeAdapter(List<UserCode> codes) {
            this.codes = codes;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_connection_code, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserCode userCode = codes.get(position);
            holder.tvUserName.setText(userCode.name);
            holder.tvConnectionCode.setText(userCode.code);
        }

        @Override
        public int getItemCount() {
            return codes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView tvUserName, tvConnectionCode;

            ViewHolder(View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvConnectionCode = itemView.findViewById(R.id.tvConnectionCode);
            }
        }
    }
} 