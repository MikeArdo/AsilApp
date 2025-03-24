package it.bugbuster.asilapp.measurements;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.utils.NavigationUtil;

public class ChooseMeasurementFragment extends Fragment {

    private CardView cardTemperature, cardHeartBeat, cardWeight, cardGlycemia;
    public ChooseMeasurementFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_measurement, container, false);
        cardTemperature = view.findViewById(R.id.cardTemperature);
        cardHeartBeat = view.findViewById(R.id.cardHeartbeat);
        cardWeight = view.findViewById(R.id.cardWeight);
        cardGlycemia = view.findViewById(R.id.cardGlycemia);

        NavigationUtil.showBackButton(this);

        if (cardTemperature != null) {
            cardTemperature.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.TEMPERATURE))
                        .addToBackStack(null)
                        .commit();

            });
        }

        if (cardHeartBeat != null) {
            cardHeartBeat.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.HEARTBEAT))
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (cardWeight != null) {
            cardWeight.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.WEIGHT))
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (cardGlycemia != null) {
            cardGlycemia.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.GLYCEMIA))
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }
}