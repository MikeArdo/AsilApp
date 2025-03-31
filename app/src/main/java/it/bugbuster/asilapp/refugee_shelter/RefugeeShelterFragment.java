package it.bugbuster.asilapp.refugee_shelter;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.stream.Collectors;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.entity.RefugeeShelter;
import it.bugbuster.asilapp.utils.JsonUtils;
import it.bugbuster.asilapp.utils.LanguageUtils;

public class RefugeeShelterFragment extends Fragment {
    private String filename = "case_accoglienza.json";
    private RefugeeViewModel refugeeViewModel;
    private List<RefugeeShelter> localRefugeeShelters;
    private TextView nameField, descriptionField;
    private ImageView imageRefugee;
    private Button btnRules, btnServices;
    private RefugeeShelter selectedRefugeeShelter;

    public RefugeeShelterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localRefugeeShelters = JsonUtils.parseRefugeeShelters(requireContext(), filename);
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
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        String cityRefugeeShelter = sharedPreferences.getString("refugeeShelter", null);
        setSelectedRefugeeShelter(cityRefugeeShelter);
        showRefugeeShelter();


        
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
        Glide.with(this)
                .load(imageSrc)
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
    }


}