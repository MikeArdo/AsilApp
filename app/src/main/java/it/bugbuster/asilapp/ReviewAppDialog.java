package it.bugbuster.asilapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import it.bugbuster.asilapp.utils.AuthUtils;

public class ReviewAppDialog {
    private Context context;
    private FirebaseFirestore db;

    public ReviewAppDialog(@NonNull Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }

    public void showDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String userId = AuthUtils.getCurrentUserId();
        final boolean[] dontAskAgain = {false};

        dialogBuilder.setTitle("Valuta l'app");
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_starbar_checkbox, null);

        dialogBuilder.setMessage("Ti piacciono i contenuti di questa app? Inserisci una valutazione da 1 a 5 stelle:");
        dialogBuilder.setView(dialogView);
        RatingBar userRating = dialogView.findViewById(R.id.userRatingBar);
        CheckBox dontAskCheckBox = dialogView.findViewById(R.id.dontAskCheckBox);

        dontAskCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dontAskAgain[0] = true;
                    userRating.setVisibility(View.GONE);
                } else {
                    dontAskAgain[0] = false;
                    userRating.setVisibility(View.VISIBLE);
                }
            }
        });

        dialogBuilder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(context.getString(R.string.send), (dialog, which) -> {
                    float rating = userRating.getRating();

                    if (dontAskAgain[0]) {
                        editor.putBoolean("dont_ask_again_" + userId, true);
                        editor.apply();
                        return;
                    }

                    if (rating == 0) {
                        Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("num_ratings", FieldValue.increment(1));
                        updates.put("sum_ratings", FieldValue.increment(rating));
                        db.collection("reviews_app").document("reviews").update(updates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Recensione inviata con successo!", Toast.LENGTH_SHORT).show();
                                        editor.putBoolean("dont_ask_again_" + userId, true);
                                        editor.apply();
                                        dialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Recensione non inviata", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                    }
                });
        dialogBuilder.create();
        dialogBuilder.show();
    }
}