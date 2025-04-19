package com.lucky.truebond;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputEditText editTextDob;
    private MaterialCheckBox checkPrivacyPolicy;
    private MaterialButton btnRegister;
    private MaterialButton btnLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        editTextDob = findViewById(R.id.editTextDob);
        checkPrivacyPolicy = findViewById(R.id.checkPrivacyPolicy);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup date picker
        editTextDob.setOnClickListener(v -> showDatePicker());

        // Setup register button
        btnRegister.setOnClickListener(v -> registerUser());

        // Setup login button
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    editTextDob.setText(String.format("%d/%d/%d", dayOfMonth, month + 1, year));
                    
                    // Check age and show/hide privacy policy checkbox
                    int age = calculateAge(selectedDate);
                    checkPrivacyPolicy.setVisibility(age < 18 ? View.VISIBLE : View.GONE);
                    if (age < 18) {
                        checkPrivacyPolicy.setChecked(false);
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        String gender = "unknown";

        // Validate inputs
        if (!validateInputs(name, email, password, confirmPassword, dob)) {
            return;
        }

        // Check age and privacy policy
        int age = calculateAge(selectedDate);
        if (age < 18 && !checkPrivacyPolicy.isChecked()) {
            Toast.makeText(this, "You must agree to the Privacy Policy to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        createUserAccount(name, email, password, gender, dob);
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword, String dob) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Please confirm password");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            isValid = false;
        }

        if (TextUtils.isEmpty(dob)) {
            editTextDob.setError("Date of birth is required");
            isValid = false;
        }

        return isValid;
    }

    private int calculateAge(Calendar dob) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    private void createUserAccount(String name, String email, String password, String gender, String dob) {
        // Show progress
        btnRegister.setEnabled(false);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Generate partner code
                        String partnerCode = generatePartnerCode();
                        
                        // Create user data
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("gender", gender);
                        userData.put("dob", dob);
                        userData.put("age", calculateAge(selectedDate));
                        userData.put("partnerCode", partnerCode);
                        userData.put("isConnected", false);
                        userData.put("connectedTo", "");
                        userData.put("registrationDate", System.currentTimeMillis());

                        // Save user data to database
                        String userId = mAuth.getCurrentUser().getUid();
                        mDatabase.child("users").child(userId).setValue(userData)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this,
                                                "Registration successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this,
                                                "Failed to save user data: " + dbTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    btnRegister.setEnabled(true);
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                    }
                });
    }

    private String generatePartnerCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}