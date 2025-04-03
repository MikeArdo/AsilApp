package it.bugbuster.asilapp.access;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.qrcode.CustomCaptureActivity;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.SharedPreferencesUtils;


public class Login extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button btnRegister, btnLogin, btnLoginQRCode, demoAsylumSeeker, demoDoctor;
    private LinearLayout loadingOverlay;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();

        String currentUser = AuthUtils.getCurrentUserId();
        if (currentUser != null) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
            String typeUser = sharedPreferences.getString("typeUser", null);
            if (typeUser != null) {
                //saveToSharedPreferences(this);
                SharedPreferencesUtils.saveToSharedPreferences(this);
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
        demoAsylumSeeker = findViewById(R.id.demoAsylumSeeker);
        demoDoctor = findViewById(R.id.demoDoctor);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        demoAsylumSeeker.setOnClickListener(view -> {
            login("michele.pio2000@gmail.com", "Password123");
        });

        demoDoctor.setOnClickListener(view -> {
            login("prova@gmail.com", "Prova123");
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                //overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
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

        loadingOverlay.setVisibility(View.VISIBLE);
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
                                                //saveToSharedPreferences(this);
                                                SharedPreferencesUtils.saveToSharedPreferences(Login.this);
                                                Toast.makeText(Login.this, R.string.signin_done, Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Login.this, HomeAsylumSeeker.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                db.collection("doctors").document(userId).get()
                                                        .addOnSuccessListener(documentDoctor -> {
                                                            if (documentDoctor.exists()) {
                                                                //saveToSharedPreferences(this);
                                                                SharedPreferencesUtils.saveToSharedPreferences(Login.this);
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
                        loadingOverlay.setVisibility(View.GONE);
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
}