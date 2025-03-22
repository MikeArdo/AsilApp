package it.bugbuster.asilapp.access;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import it.bugbuster.asilapp.DatabaseHelper;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.qrcode.CustomCaptureActivity;
import it.bugbuster.asilapp.utils.AuthUtils;


public class Login extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button btnRegister, btnLogin, btnLoginQRCode;
    private DatabaseHelper dbHelper;

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            saveToSharedPreferences(this);
            startActivity(new Intent(this, Home.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedIstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedIstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginQRCode = findViewById(R.id.btnLoginQRCode);
        dbHelper = new DatabaseHelper(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginText();
            }
        });


        btnLoginQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    startQRScanner();
                } else {
                    ActivityCompat.requestPermissions(Login.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                }
            }
        });


    }

    private void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, R.string.signin_done, Toast.LENGTH_SHORT).show();
                            dbHelper.syncFirestoreToLocal(getBaseContext());
                            saveToSharedPreferences(Login.this);
                            Intent intent = new Intent(Login.this, Home.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, getString(R.string.signin_failed) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginText() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        login(email, password);
    }

    private void loginQrCode(String qrCode) {
        String[] parti = qrCode.split(";");

        String email = "";
        String password = "";

        for (String parte : parti) {
            if (parte.startsWith("email:")) {
                email = parte.substring(6).trim();
            } else if (parte.startsWith("password:")) {
                password = parte.substring(9).trim();
            }
        }

        login(email, password);
    }

    private void startQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.scan_qr_code));
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CustomCaptureActivity.class);

        qrCodeLauncher.launch(options);
    }

    //handle qr codes result
    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                //handle scanned qr code image
                if (result.getContents() != null) {
                    loginQrCode(result.getContents());
                } else {
                    //handle imported qr code image
                    Intent originalIntent = result.getOriginalIntent();
                    if (originalIntent != null && originalIntent.hasExtra("QR_RESULT")) {
                        String accessData = originalIntent.getStringExtra("QR_RESULT");
                        if (accessData != null) {
                            loginQrCode(accessData);
                        }
                    }
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, getString(R.string.camera_permission),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void saveToSharedPreferences(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String userId = AuthUtils.getCurrentUserId();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();



        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String surname = documentSnapshot.getString("surname");
                            String birthDate = documentSnapshot.getString("birthDate");


                            // Save data in SharedPreferences
                            editor.putString("name", name);
                            editor.putString("surname", surname);
                            if (user != null) {
                                String email = user.getEmail();
                                editor.putString("email", email);
                            }

                            editor.putString("birthDate", birthDate);

                            editor.apply();

                        }
                    });
        }

    }




}