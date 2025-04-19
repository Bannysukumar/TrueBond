package com.lucky.truebond;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText editTextName;
    private TextInputEditText editTextCurrentPassword;
    private TextInputEditText editTextNewEmail;
    private TextInputEditText editTextNewPassword;
    private TextInputEditText editTextCoupleCode;
    private TextInputLayout coupleCodeLayout;
    private MaterialButton buttonSave;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewEmail = findViewById(R.id.editTextNewEmail);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextCoupleCode = findViewById(R.id.editTextCoupleCode);
        coupleCodeLayout = findViewById(R.id.coupleCodeLayout);
        buttonSave = findViewById(R.id.buttonSave);

        // Setup couple code copy functionality
        coupleCodeLayout.setEndIconOnClickListener(v -> copyCoupleCode());

        // Load current user data
        loadUserData();

        // Setup save button click listener
        buttonSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    editTextName.setText(user.getName());
                                    editTextCoupleCode.setText(user.getCoupleCode());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void copyCoupleCode() {
        String coupleCode = editTextCoupleCode.getText().toString();
        if (!TextUtils.isEmpty(coupleCode)) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Couple Code", coupleCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Couple code copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String name = editTextName.getText().toString().trim();
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newEmail = editTextNewEmail.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required");
            return;
        }

        // Check if any changes are being made to email or password
        boolean isChangingEmail = !TextUtils.isEmpty(newEmail);
        boolean isChangingPassword = !TextUtils.isEmpty(newPassword);

        if (isChangingEmail || isChangingPassword) {
            if (TextUtils.isEmpty(currentPassword)) {
                editTextCurrentPassword.setError("Current password is required");
                return;
            }

            // Verify current password
            AuthCredential credential = EmailAuthProvider.getCredential(
                    currentUser.getEmail(), currentPassword);

            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update email if changed
                            if (isChangingEmail) {
                                updateEmail(newEmail);
                            }
                            // Update password if changed
                            if (isChangingPassword) {
                                updatePassword(newPassword);
                            }
                            // Update name in database
                            updateName(name);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Only update name if no email/password changes
            updateName(name);
        }
    }

    private void updateEmail(String newEmail) {
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateName(String name) {
        mDatabase.child("users").child(currentUser.getUid()).child("name")
                .setValue(name)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
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