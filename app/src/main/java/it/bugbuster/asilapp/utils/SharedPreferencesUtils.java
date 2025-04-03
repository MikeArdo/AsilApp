package it.bugbuster.asilapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SharedPreferencesUtils {
    public static void saveToSharedPreferences(Context context) {
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
                            String refugeeShelter = documentSnapshot.getString("refugeeShelter");

                            // Save data in SharedPreferences
                            editor.putString("name", name);
                            editor.putString("surname", surname);
                            if (user != null) {
                                String email = user.getEmail();
                                editor.putString("email", email);
                            }

                            editor.putString("birthDate", birthDate);
                            editor.putString("refugeeShelter", refugeeShelter);
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
