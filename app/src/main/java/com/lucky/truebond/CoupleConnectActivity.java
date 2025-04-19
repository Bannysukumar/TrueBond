package com.lucky.truebond;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.Random;

public class CoupleConnectActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String partnerCode;
    private TextView tvCode;
    private TextInputEditText partnerCodeInput;
    private MaterialButton btnScanQR;
    private ImageView qrCodeView;
    private boolean isAlreadyConnected = false;
    private MaterialButton btnDisconnect;
    private String partnerId;
    private MaterialButton btnConnect;
    private MaterialButton btnShareCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couple_connect);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        tvCode = findViewById(R.id.tvCode);
        partnerCodeInput = findViewById(R.id.partnerCodeInput);
        btnScanQR = findViewById(R.id.btnScanQR);
        qrCodeView = findViewById(R.id.qrCodeView);
        btnConnect = findViewById(R.id.btnConnect);
        btnShareCode = findViewById(R.id.btnShareCode);
        btnDisconnect = findViewById(R.id.btnDisconnect);

        // Check if already connected
        checkExistingConnection();

        // Set click listeners
        btnScanQR.setOnClickListener(v -> {
            if (!isAlreadyConnected) {
                startQRScanner();
            } else {
                Toast.makeText(this, "You are already connected with a partner", Toast.LENGTH_SHORT).show();
            }
        });

        btnConnect.setOnClickListener(v -> {
            if (!isAlreadyConnected) {
                String code = partnerCodeInput.getText().toString().trim();
                if (!code.isEmpty()) {
                    connectWithPartner(code);
                } else {
                    Toast.makeText(this, "Please enter a partner code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "You are already connected with a partner", Toast.LENGTH_SHORT).show();
            }
        });

        btnShareCode.setOnClickListener(v -> sharePartnerCode());

        btnDisconnect.setOnClickListener(v -> {
            if (isAlreadyConnected && partnerId != null) {
                Intent intent = new Intent(CoupleConnectActivity.this, DisconnectRequestActivity.class);
                intent.putExtra("partnerId", partnerId);
                startActivity(intent);
            }
        });

        // Load or generate partner code
        loadOrGeneratePartnerCode();
    }

    private void checkExistingConnection() {
        mDatabase.child("users").child(currentUserId).child("partnerId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            partnerId = snapshot.getValue(String.class);
                            isAlreadyConnected = true;
                            updateUIForConnectedState();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CoupleConnectActivity.this, 
                            "Failed to check connection status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUIForConnectedState() {
        if (isAlreadyConnected) {
            btnScanQR.setEnabled(false);
            btnConnect.setEnabled(false);
            partnerCodeInput.setEnabled(false);
            btnDisconnect.setVisibility(View.VISIBLE);
            tvCode.setVisibility(View.GONE);
            findViewById(R.id.tvYourCode).setVisibility(View.GONE);
            btnShareCode.setVisibility(View.GONE);
            qrCodeView.setVisibility(View.GONE);
        }
    }

    private void loadOrGeneratePartnerCode() {
        mDatabase.child("users").child(currentUserId).child("coupleCode")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Use existing code
                            partnerCode = snapshot.getValue(String.class);
                            displayPartnerCode();
                        } else {
                            // Generate new code
                            generateAndSavePartnerCode();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CoupleConnectActivity.this, 
                            "Failed to load partner code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateAndSavePartnerCode() {
        // Generate a random 6-digit code
        Random random = new Random();
        partnerCode = String.format("%06d", random.nextInt(1000000));

        // Save to database
        mDatabase.child("users").child(currentUserId).child("coupleCode")
                .setValue(partnerCode)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        displayPartnerCode();
                    } else {
                        Toast.makeText(CoupleConnectActivity.this, 
                            "Failed to save partner code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayPartnerCode() {
        tvCode.setText(partnerCode);

        // Generate QR code
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(partnerCode, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrBitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Create a new bitmap with the same size as QR code
            Bitmap watermarkedBitmap = Bitmap.createBitmap(qrBitmap.getWidth(), qrBitmap.getHeight(), qrBitmap.getConfig());
            Canvas canvas = new Canvas(watermarkedBitmap);

            // Draw the QR code
            canvas.drawBitmap(qrBitmap, 0, 0, null);

            // Add text watermark
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(24);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);

            // Draw app name at the bottom
            String appName = "TrueBond";
            float x = qrBitmap.getWidth() / 2f;
            float y = qrBitmap.getHeight() - 10;
            canvas.drawText(appName, x, y, paint);

            // Set the watermarked QR code to the ImageView
            qrCodeView.setImageBitmap(watermarkedBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan your partner's QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                connectWithPartner(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void connectWithPartner(String partnerCode) {
        // First get current user's gender
        mDatabase.child("users").child(currentUserId).child("gender")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot currentUserSnapshot) {
                        String currentUserGender = currentUserSnapshot.getValue(String.class);
                        
                        // Find user with this partner code
                        mDatabase.child("users").orderByChild("coupleCode").equalTo(partnerCode)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                String partnerId = userSnapshot.getKey();
                                                if (partnerId != null && !partnerId.equals(currentUserId)) {
                                                    // Get partner's gender
                                                    String partnerGender = userSnapshot.child("gender").getValue(String.class);
                                                    
                                                    // Check if genders are different
                                                    if (currentUserGender != null && partnerGender != null && 
                                                        !currentUserGender.equals(partnerGender)) {
                                                        // Update both users' partner IDs
                                                        mDatabase.child("users").child(currentUserId).child("partnerId")
                                                                .setValue(partnerId);
                                                        mDatabase.child("users").child(partnerId).child("partnerId")
                                                                .setValue(currentUserId);
                                                        
                                                        Toast.makeText(CoupleConnectActivity.this, 
                                                            "Successfully connected with partner!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                        return;
                                                    } else {
                                                        Toast.makeText(CoupleConnectActivity.this, 
                                                            "Cannot connect with same gender partner", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                }
                                            }
                                            // If we get here, it means the code belongs to the current user
                                            Toast.makeText(CoupleConnectActivity.this, 
                                                "Cannot connect with yourself", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(CoupleConnectActivity.this, 
                                                "Invalid partner code", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(CoupleConnectActivity.this, 
                                            "Failed to connect: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CoupleConnectActivity.this, 
                            "Failed to get user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sharePartnerCode() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            "Connect with me on TrueBond! My partner code is: " + partnerCode);
        startActivity(Intent.createChooser(shareIntent, "Share Partner Code"));
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