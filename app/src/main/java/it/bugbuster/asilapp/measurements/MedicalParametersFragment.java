package it.bugbuster.asilapp.measurements;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.utils.NavigationUtil;

public class MedicalParametersFragment extends Fragment {


    public MedicalParametersFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        NavigationUtil.showHomeButton(this);
        return inflater.inflate(R.layout.fragment_medical_parameters, container, false);
    }



}