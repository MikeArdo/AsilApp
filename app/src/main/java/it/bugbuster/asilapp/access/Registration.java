package it.bugbuster.asilapp.access;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.bugbuster.asilapp.MailSender;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.entity.User;
import it.bugbuster.asilapp.qrcode.QRCodeGeneration;
import it.bugbuster.asilapp.utils.DatePickerUtils;
//TODO modificare la registrazione specializzando l'utente in Dottore e Richiedente asilo
public class Registration extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText nameField, surnameField, emailField, passwordField, confirmPasswordField, birthDateField;

    private EditText refugeeShelterField, licenseNumberField;
    private TextInputLayout refugeeShelterLayout, licenseNumberLayout;
    private Button btnRegister;

    private RadioGroup radioTypeUser;

    private MaterialDatePicker<Long> datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameField = findViewById(R.id.name);
        surnameField = findViewById(R.id.surname);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        birthDateField = findViewById(R.id.birthdate);
        btnRegister = findViewById(R.id.btnRegister);
        refugeeShelterField = findViewById(R.id.refugeeShelter);
        refugeeShelterLayout = findViewById(R.id.refugeeShelterLayout);
        licenseNumberField = findViewById(R.id.licenseNumber);
        licenseNumberLayout = findViewById(R.id.licenseNumberLayout);
        radioTypeUser = findViewById(R.id.radio_type_user);
        datePicker = DatePickerUtils.setupDatePicker();

        datePicker.addOnPositiveButtonClickListener (selection ->  {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            birthDateField.setText(dateFormat.format(new Date(selection)));
        });

        radioTypeUser.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_doctor) {
                    refugeeShelterLayout.setVisibility(View.GONE);
                    licenseNumberLayout.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.radio_asylum_seeker) {
                    licenseNumberLayout.setVisibility(View.GONE);
                    refugeeShelterLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        birthDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });
    }


    private void registerUser() {
        String name = nameField.getText().toString().trim();
        String surname = surnameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String birthDate = birthDateField.getText().toString().trim();
        String refugeeShelter = refugeeShelterField.getText().toString().trim();
        String licenseNumber = licenseNumberField.getText().toString().trim();




        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || birthDate.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (refugeeShelterField.getVisibility() == View.VISIBLE && refugeeShelter.isEmpty()) {
            Toast.makeText(this, R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (licenseNumberField.getVisibility() == View.VISIBLE && licenseNumber.isEmpty()) {
            Toast.makeText(this, R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwords_not_match, Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser fBuser = mAuth.getCurrentUser();
                            User user = new User(name, surname, email, birthDate);
                            if (fBuser != null) {
                                saveUserToFirestore(fBuser.getUid(), user);
                                String credentials = "email:" + email + ";password:" + password;
                                Bitmap qrCode = generateQRCode(credentials);
                                sendEmail(user, qrCode);
                                finish();
                            }
                        } else {
                            Toast.makeText(Registration.this, getText(R.string.signup_failed) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Bitmap generateQRCode(String credentials) {
        Bitmap qrCodeBitmap = null;
        try {
            qrCodeBitmap = QRCodeGeneration.generateQRCode(credentials);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Registration.this, getText(R.string.error_qrcode), Toast.LENGTH_SHORT).show();
        }
        return qrCodeBitmap;
    }

    private void sendEmail(User user, Bitmap qrCode) {
        //TODO change emailTo
        String emailTo = "michele.pio2000@gmail.com";
        String subject = getString(R.string.welcome_to_asilapp);
        String body = getString(R.string.welcome_message, user.getName(), user.getSurname());

        MailSender emailSender = new MailSender();
        emailSender.sendEmailService(emailTo, subject, body, qrCode);

    }

    private void saveUserToFirestore(String userId, User user) {

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Registration.this, R.string.saved_data, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, Home.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(Registration.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}