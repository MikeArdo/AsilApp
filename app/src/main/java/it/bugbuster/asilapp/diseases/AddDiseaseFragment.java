package it.bugbuster.asilapp.diseases;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.database.DiseasesDatabase;
import it.bugbuster.asilapp.database.ExpensesDatabase;
import it.bugbuster.asilapp.entity.User;


public class AddDiseaseFragment extends Fragment {
    private OnBackPressedCallback callback;
    private DiseasesDatabase diseasesDatabase;
    private static final String ARG_USER = "user";
    private EditText diseaseField, therapyField;
    private Button btnSaveDisease;
    private User user;

    public AddDiseaseFragment() {
        // Required empty public constructor
    }


    public static AddDiseaseFragment newInstance(User user) {
        AddDiseaseFragment fragment = new AddDiseaseFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_disease, container, false);
        diseasesDatabase = new DiseasesDatabase(getContext());
        diseaseField = view.findViewById(R.id.editTextDisease);
        therapyField = view.findViewById(R.id.editTextTherapy);
        btnSaveDisease = view.findViewById(R.id.btnSaveDisease);

        btnSaveDisease.setOnClickListener(view1 -> {
            saveDisease();
        });

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStack();
                callback.remove();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        return view;
    }

    private void saveDisease() {
        String diseaseStr = diseaseField.getText().toString();
        String therapyStr = therapyField.getText().toString();

        if (diseaseStr.isEmpty() || therapyStr.isEmpty()) {
            Toast.makeText(getContext(), R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = diseasesDatabase.addDisease(getContext(), user.getId(), diseaseStr, therapyStr);

        if (inserted) {
            Toast.makeText(getContext(), getString(R.string.disease_saved), Toast.LENGTH_SHORT).show();
            diseaseField.setText("");
            therapyField.setText("");
            getParentFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), getString(R.string.disease_not_saved), Toast.LENGTH_SHORT).show();
        }
    }
}