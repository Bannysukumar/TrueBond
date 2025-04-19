package com.lucky.truebond;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AnniversarySettingsActivity extends AppCompatActivity {
    private MaterialButton btnSelectDate;
    private TextInputEditText editTextTime;
    private SwitchMaterial switchReminder;
    private MaterialButton btnSave;
    
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    private DatabaseReference settingsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anniversary_settings);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        settingsRef = FirebaseDatabase.getInstance().getReference()
                .child("anniversary_settings")
                .child(currentUserId);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        btnSelectDate = findViewById(R.id.btnSelectDate);
        editTextTime = findViewById(R.id.editTextTime);
        switchReminder = findViewById(R.id.switchReminder);
        btnSave = findViewById(R.id.btnSave);

        // Initialize date and time formats
        dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        selectedDate = Calendar.getInstance();

        // Set click listeners
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        editTextTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveSettings());

        // Load existing settings
        loadSettings();
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, selectedDay);
            btnSelectDate.setText(dateFormat.format(selectedDate.getTime()));
        }, year, month, day);
        datePicker.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    editTextTime.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePicker.show();
    }

    private void loadSettings() {
        settingsRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                AnniversarySettings settings = dataSnapshot.getValue(AnniversarySettings.class);
                if (settings != null) {
                    selectedDate.setTimeInMillis(settings.getAnniversaryDate());
                    btnSelectDate.setText(dateFormat.format(selectedDate.getTime()));
                    editTextTime.setText(settings.getReminderTime());
                    switchReminder.setChecked(settings.isReminderEnabled());
                }
            }
        });
    }

    private void saveSettings() {
        String time = editTextTime.getText().toString().trim();
        if (time.isEmpty()) {
            editTextTime.setError("Please select a reminder time");
            return;
        }

        AnniversarySettings settings = new AnniversarySettings(
                selectedDate.getTimeInMillis(),
                time,
                switchReminder.isChecked()
        );

        settingsRef.setValue(settings)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save settings: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
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