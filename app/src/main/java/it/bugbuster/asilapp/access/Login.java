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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import it.bugbuster.asilapp.database.ExpensesDatabase;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.qrcode.CustomCaptureActivity;
import it.bugbuster.asilapp.utils.AuthUtils;


public class Login extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button btnRegister, btnLogin, btnLoginQRCode;
    private ExpensesDatabase expensesDatabase;

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
            String typeUser = sharedPreferences.getString("typeUser", null);
            if (typeUser != null) {
                saveToSharedPreferences(this);
                Intent intent = null;

                if (typeUser.equals("asylum_seeker")) {
                    intent = new Intent(this, HomeAsylumSeeker.class);

                } else if (typeUser.equals("doctor")) {
                    intent = new Intent(this, HomeDoctor.class);
                }
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();
            }

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
        expensesDatabase = new ExpensesDatabase(this);

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
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        Login.this, Manifest.permission.CAMERA)) {
                    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(Login.this)
                            .setTitle(getString(R.string.camera_permission_request))
                            .setMessage(getString(R.string.camera_permission_description))
                            .setNegativeButton(getString(R.string.no_thanks), (dialog, which) -> {
                                Toast.makeText(Login.this, getString(R.string.camera_permission),
                                        Toast.LENGTH_LONG).show();
                            })
                            .setPositiveButton(getString(R.string.continue_text), (dialog, which) -> {
                                ActivityCompat.requestPermissions(Login.this,
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                            });
                    dialogBuilder.create();
                    dialogBuilder.show();
                } else {
                    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(Login.this)
                            .setTitle(getString(R.string.camera_permission_request))
                            .setMessage(getString(R.string.camera_permission_description))
                            .setPositiveButton(getString(R.string.continue_text), (dialog, which) -> {
                                ActivityCompat.requestPermissions(Login.this,
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                            });
                    dialogBuilder.create();
                    dialogBuilder.show();
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
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                db.collection("users").document(userId).get()
                                        .addOnSuccessListener(document -> {
                                            if (document.exists()) {
                                                saveToSharedPreferences(Login.this);
                                                Toast.makeText(Login.this, R.string.signin_done, Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Login.this, HomeAsylumSeeker.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                db.collection("doctors").document(userId).get()
                                                        .addOnSuccessListener(documentDoctor -> {
                                                            if (documentDoctor.exists()) {
                                                                saveToSharedPreferences(Login.this);
                                                                Toast.makeText(Login.this, R.string.signin_done, Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(Login.this, HomeDoctor.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(e2 -> {
                                                            Toast.makeText(Login.this, R.string.signin_failed, Toast.LENGTH_SHORT).show();
                                                        });
                                            }

                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Login.this, R.string.signin_failed, Toast.LENGTH_SHORT).show();
                                        });
                            }
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
                            editor.putString("typeUser", "asylum_seeker");

                            editor.apply();

                        } else {
                            db.collection("doctors").document(userId)
                                    .get()
                                    .addOnSuccessListener(documentDoctor -> {
                                        if (documentDoctor.exists()) {
                                            String name = documentDoctor.getString("name");
                                            String surname = documentDoctor.getString("surname");
                                            String birthDate = documentDoctor.getString("birthDate");
                                            String licenseNumber = documentDoctor.getString("licenseNumber");


                                            // Save data in SharedPreferences
                                            editor.putString("name", name);
                                            editor.putString("surname", surname);
                                            if (user != null) {
                                                String email = user.getEmail();
                                                editor.putString("email", email);
                                            }

                                            editor.putString("birthDate", birthDate);
                                            editor.putString("licenseNumber", licenseNumber);
                                            editor.putString("typeUser", "doctor");

                                            editor.apply();

                                        }
                                    });
                        }
                    });
        }
    }
}