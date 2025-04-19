package com.lucky.truebond;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisconnectRequestActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String currentUserId;
    private String partnerId;
    private TextView tvRequestStatus;
    private MaterialButton btnAccept;
    private MaterialButton btnCancel;
    private boolean isRequestSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnect_request);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        tvRequestStatus = findViewById(R.id.tvRequestStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Disconnect Request");

        // Get partner ID
        loadPartnerId();
    }

    private void loadPartnerId() {
        mDatabase.child("users").child(currentUserId).child("partnerId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            partnerId = snapshot.getValue(String.class);
                            checkDisconnectRequest();
                        } else {
                            Toast.makeText(DisconnectRequestActivity.this, 
                                "No partner connected", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DisconnectRequestActivity.this, 
                            "Failed to load partner", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void checkDisconnectRequest() {
        String requestId = currentUserId.compareTo(partnerId) < 0 
                ? currentUserId + "_" + partnerId 
                : partnerId + "_" + currentUserId;

        mDatabase.child("disconnect_requests").child(requestId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            DisconnectRequest request = snapshot.getValue(DisconnectRequest.class);
                            if (request != null) {
                                updateUI(request);
                            }
                        } else {
                            // No request exists, show initial state
                            isRequestSent = false;
                            updateInitialUI();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DisconnectRequestActivity.this, 
                            "Failed to load request status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateInitialUI() {
        tvRequestStatus.setText("Do you want to disconnect from your partner?");
        btnAccept.setText("Send Disconnect Request");
        btnAccept.setOnClickListener(v -> sendDisconnectRequest());
        btnCancel.setText("Cancel");
        btnCancel.setOnClickListener(v -> finish());
    }

    private void updateUI(DisconnectRequest request) {
        if (request.getRequesterId().equals(currentUserId)) {
            // Current user sent the request
            isRequestSent = true;
            tvRequestStatus.setText("Waiting for partner's response...");
            btnAccept.setEnabled(false);
            btnCancel.setText("Cancel Request");
            btnCancel.setOnClickListener(v -> cancelDisconnectRequest());
        } else {
            // Partner sent the request
            isRequestSent = false;
            tvRequestStatus.setText("Your partner has requested to disconnect. Please respond.");
            btnAccept.setText("Accept Disconnect");
            btnAccept.setOnClickListener(v -> acceptDisconnectRequest());
            btnCancel.setText("Reject Request");
            btnCancel.setOnClickListener(v -> rejectDisconnectRequest());
        }
    }

    private void sendDisconnectRequest() {
        String requestId = currentUserId.compareTo(partnerId) < 0 
                ? currentUserId + "_" + partnerId 
                : partnerId + "_" + currentUserId;

        DisconnectRequest request = new DisconnectRequest(currentUserId, partnerId, false);
        mDatabase.child("disconnect_requests").child(requestId).setValue(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Disconnect request sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelDisconnectRequest() {
        String requestId = currentUserId.compareTo(partnerId) < 0 
                ? currentUserId + "_" + partnerId 
                : partnerId + "_" + currentUserId;

        mDatabase.child("disconnect_requests").child(requestId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptDisconnectRequest() {
        String requestId = currentUserId.compareTo(partnerId) < 0 
                ? currentUserId + "_" + partnerId 
                : partnerId + "_" + currentUserId;

        // Update request status
        mDatabase.child("disconnect_requests").child(requestId).child("accepted").setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove partner connection
                        mDatabase.child("users").child(currentUserId).child("partnerId").removeValue();
                        mDatabase.child("users").child(partnerId).child("partnerId").removeValue();
                        
                        // Remove request
                        mDatabase.child("disconnect_requests").child(requestId).removeValue();
                        
                        Toast.makeText(this, "Successfully disconnected", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to accept request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rejectDisconnectRequest() {
        String requestId = currentUserId.compareTo(partnerId) < 0 
                ? currentUserId + "_" + partnerId 
                : partnerId + "_" + currentUserId;

        mDatabase.child("disconnect_requests").child(requestId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to reject request", Toast.LENGTH_SHORT).show();
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