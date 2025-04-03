package it.bugbuster.asilapp.measurements;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.database.MeasurementsDatabase;
import it.bugbuster.asilapp.utils.DateRangePickerUtils;
import it.bugbuster.asilapp.utils.NavigationUtil;
import it.bugbuster.asilapp.utils.ResourcesUtil;

public class MedicalParametersFragment extends Fragment {

    private MeasurementsDatabase measurementsDatabase;
    private ListView measurementsListView;
    private AutoCompleteTextView typeMeasurements;
    private EditText dateFilter;
    private MaterialDatePicker<Pair<Long, Long>> dateRangePicker;
    private SimpleDateFormat dateFormat;


    @Override
    public void onResume() {
        super.onResume();
        NavigationUtil.showHomeButton(this);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        measurementsDatabase = new MeasurementsDatabase(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_parameters, container, false);
        measurementsListView = view.findViewById(R.id.listViewMeasurements);
        typeMeasurements = view.findViewById(R.id.spinnerFilterCategory);
        dateFilter = view.findViewById(R.id.editTextFilterDate);
        measurementsDatabase.syncFirestoreToLocal(requireContext(), success -> {
            if (success) {
                loadMeasurements(false); // Load measurements only after sync completes
            }
        });
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateRangePicker = DateRangePickerUtils.setupDateRangePicker();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;

            String startDateString = dateFormat.format(new Date(startDate));
            String endDateString = dateFormat.format(new Date(endDate));
            String dates = startDateString + " - " + endDateString;
            dateFilter.setText(dates);
        });

        dateFilter.setOnClickListener(v -> {
            dateRangePicker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
        });

        typeMeasurements.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                loadMeasurements(true);
            }
        });

        dateFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                loadMeasurements(true);
            }
        });





        loadMeasurements(false);

        return view;
    }

    private void loadMeasurements(boolean filtered) {
        Cursor cursor = null;
        if (filtered) {
            String selectedType = typeMeasurements.getText().toString();
            String selectedDate = dateFilter.getText().toString();
            String typeResourceId = ResourcesUtil.getStringResourceName(requireContext(), selectedType);
            if (typeResourceId != null) {
                cursor = measurementsDatabase.getFilteredMeasurements(typeResourceId, selectedDate);
            } else {
                typeResourceId = "";
                cursor = measurementsDatabase.getFilteredMeasurements(typeResourceId, selectedDate);
            }
        } else {
            cursor = measurementsDatabase.getMeasurements();
        }


        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_list,
                cursor,
                new String[]{"type", "date", "value"},
                new int[]{R.id.item_title, R.id.item_subtitle, R.id.item_metadata},
                0
        ){
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);

                TextView typeView = view.findViewById(R.id.item_title);
                String type = typeView.getText().toString();
                int stringIdentifier = 0;

                switch (type) {
                    case "TEMPERATURE":
                        stringIdentifier = R.string.TEMPERATURE;
                        break;
                    case "HEARTBEAT":
                        stringIdentifier = R.string.HEARTBEAT;
                        break;
                    case "WEIGHT":
                        stringIdentifier = R.string.WEIGHT;
                        break;
                    case "GLYCEMIA":
                        stringIdentifier = R.string.GLYCEMIA;
                        break;
                }
                typeView.setText(stringIdentifier);
            }
        };

        measurementsListView.setAdapter(adapter);
    }
}