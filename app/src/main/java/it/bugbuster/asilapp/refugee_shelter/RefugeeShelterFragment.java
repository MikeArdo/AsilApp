package it.bugbuster.asilapp.refugee_shelter;


import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.entity.RefugeeShelter;
import it.bugbuster.asilapp.utils.JsonUtils;
import it.bugbuster.asilapp.utils.LanguageUtils;

public class RefugeeShelterFragment extends Fragment {
    private FirebaseFirestore db;
    private String filename = "case_accoglienza.json";
    private RefugeeViewModel refugeeViewModel;
    private List<RefugeeShelter> localRefugeeShelters;
    private TextView nameField, descriptionField, averageRatingText;
    private ImageView imageRefugee;
    private Button btnRules, btnServices;
    private RatingBar ratingBar;
    private RefugeeShelter selectedRefugeeShelter;

    public RefugeeShelterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localRefugeeShelters = JsonUtils.parseRefugeeShelters(requireContext(), filename);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Casa accoglienza");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_refugee_shelter, container, false);
        nameField = view.findViewById(R.id.textViewName);
        descriptionField = view.findViewById(R.id.textViewDescription);
        imageRefugee = view.findViewById(R.id.imageShelter);
        btnRules = view.findViewById(R.id.btnRules);
        btnServices = view.findViewById(R.id.btnServices);
        ratingBar = view.findViewById(R.id.ratingBar);
        averageRatingText = view.findViewById(R.id.averageRatingText);
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        String cityRefugeeShelter = sharedPreferences.getString("refugeeShelter", null);
        setSelectedRefugeeShelter(cityRefugeeShelter);
        showRefugeeShelter();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    submitReview(selectedRefugeeShelter.getId(), rating);
                }
            }
        });


        
        refugeeViewModel = new ViewModelProvider(this).get(RefugeeViewModel.class);
        refugeeViewModel.getRefugeeShelter(requireContext(), filename).observe(getViewLifecycleOwner(), new Observer<List<RefugeeShelter>>() {
            @Override
            public void onChanged(List<RefugeeShelter> refugeeShelters) {
                localRefugeeShelters = refugeeShelters;
                showRefugeeShelter();
            }
        });
        
        btnRules.setOnClickListener(view1 -> {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.rules)
                    .setNegativeButton(R.string.close, (dialog, which) -> {
                        
                    });
            if (LanguageUtils.getCurrentLanguage().equals("it")) {
                dialogBuilder.setMessage(dottedStringList(selectedRefugeeShelter.getRules().getIt()));
            } else {
                dialogBuilder.setMessage(dottedStringList(selectedRefugeeShelter.getRules().getEn()));
            }
            dialogBuilder.create();
            dialogBuilder.show();
        });

        btnServices.setOnClickListener(view1 -> {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.services)
                    .setNegativeButton(R.string.close, (dialog, which) -> {

                    });
            if (LanguageUtils.getCurrentLanguage().equals("it")) {
                dialogBuilder.setMessage(dottedStringList(selectedRefugeeShelter.getServices().getIt()));
            } else {
                dialogBuilder.setMessage(dottedStringList(selectedRefugeeShelter.getServices().getEn()));
            }
            dialogBuilder.create();
            dialogBuilder.show();
        });

        return view;
    }

    private void showRefugeeShelter() {
        String imageSrc = selectedRefugeeShelter.getImage();
        DrawableCrossFadeFactory factory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(this)
                .load(imageSrc)
                .transition(withCrossFade(factory))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.md_theme_surfaceDim)
                .centerCrop()
                .into(imageRefugee);
        nameField.setText(selectedRefugeeShelter.getName());
        if (LanguageUtils.getCurrentLanguage().equals("it")) {
            descriptionField.setText(selectedRefugeeShelter.getDescription().getIt());
        } else {
            descriptionField.setText(selectedRefugeeShelter.getDescription().getEn());
        }
    }

    private Spanned dottedStringList(List<String> items) {
        StringBuilder bulletList = new StringBuilder();
        for (String item : items) {
            bulletList.append("&#8226; ").append(item).append("<br>"); // &#8226; è il simbolo del pallino
        }
        return Html.fromHtml(bulletList.toString(), Html.FROM_HTML_MODE_LEGACY);
    }

    private void setSelectedRefugeeShelter(String cityRefugee) {
        selectedRefugeeShelter = localRefugeeShelters.stream()
                .filter(city -> cityRefugee.equals(city.getCity()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Location not found!"));
        calculateAverageRating(selectedRefugeeShelter.getId());
    }

    private void calculateAverageRating(String cityRefugee) {
        db.collection("refugee_shelter").document(cityRefugee) // Fetch all reviews from Firestore
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot querySnapshot = task.getResult();
                        Long numberRatingsLong = querySnapshot.getLong("num_ratings");
                        int numberRatings = 0;
                        if (numberRatingsLong != null)
                            numberRatings = numberRatingsLong.intValue();
                        Long sumRatingsLong = querySnapshot.getLong("sum_ratings");
                        int sumRatings = 0;
                        if (sumRatingsLong != null)
                            sumRatings = sumRatingsLong.intValue();


                        if (numberRatings > 0) {
                            double averageRating = (double) sumRatings / numberRatings;
                            showRating(averageRating);
                        } else {
                            showRating(0);
                        }
                    } else {
                        showRating(0);
                    }
                });
    }


    private void showRating(double averageRating) {
        ratingBar.setRating((float) averageRating);

        // Also update the numeric text display
        averageRatingText.setText(getString(R.string.average, averageRating));
    }

    private void submitReview(String cityRefugee, float userRatingValue) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        dialogBuilder.setTitle("Inserisci una recensione");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_starbar, null);

        dialogBuilder.setView(dialogView);
        RatingBar userRating = dialogView.findViewById(R.id.userRatingBar);
        userRating.setRating(userRatingValue);

        dialogBuilder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    calculateAverageRating(cityRefugee);
                })
                .setPositiveButton(getString(R.string.send), (dialog, which) -> {
                    float rating = userRating.getRating();

                    if (rating == 0) {
                        Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("num_ratings", FieldValue.increment(1));
                        updates.put("sum_ratings", FieldValue.increment(rating));
                        db.collection("refugee_shelter").document(cityRefugee).update(updates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        calculateAverageRating(cityRefugee);
                                        Toast.makeText(requireContext(), "Recensione inviata con successo!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        calculateAverageRating(cityRefugee);
                                        Toast.makeText(requireContext(), "Recensione non inviata", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setOnDismissListener(dialogInterface -> {
                    calculateAverageRating(cityRefugee);
                });
        dialogBuilder.create();
        dialogBuilder.show();
    }


}