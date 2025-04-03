package it.bugbuster.asilapp.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.access.Login;
import it.bugbuster.asilapp.entity.RefugeeShelter;
import it.bugbuster.asilapp.utils.UserAvatarUtil;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private Button btnLogout, btnResetPassword;
    private ImageView userAvatarImageView;

    private TextView textNameField, textEmailField, textBirthDateField;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profilo");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        btnLogout = view.findViewById(R.id.btnLogout);
        btnResetPassword = view.findViewById(R.id.resetPasswordButton);
        userAvatarImageView = view.findViewById(R.id.user_avatar);
        textNameField = view.findViewById(R.id.text_name);
        textEmailField = view.findViewById(R.id.text_email);
        textBirthDateField = view.findViewById(R.id.text_birthdate);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);

        String name = sharedPreferences.getString("name", "Name");
        String surname = sharedPreferences.getString("surname", "Surname");
        String email = sharedPreferences.getString("email", "Email");
        String birthDate = sharedPreferences.getString("birthDate", "Birth date");
        String typeUser = sharedPreferences.getString("typeUser", null);

        UserAvatarUtil.setUserAvatar(name, surname, userAvatarImageView);
        String nameAndSurname = name + " " + surname;
        textNameField.setText(nameAndSurname);
        textBirthDateField.setText(birthDate);
        textEmailField.setText(email);

        btnResetPassword.setOnClickListener(v -> {
            if (email.isEmpty()) {
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Email di reset inviata!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "Errore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

                Intent intent = new Intent(getActivity(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        return view;
    }
}