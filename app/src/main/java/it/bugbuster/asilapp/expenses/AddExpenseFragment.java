package it.bugbuster.asilapp.expenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.bugbuster.asilapp.database.ExpensesDatabase;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.utils.DatePickerUtils;

public class AddExpenseFragment extends Fragment {
    private ExpensesDatabase expensesDatabase;
    private EditText amountInput, dateInput;
    private AutoCompleteTextView categorySpinner;
    private Button saveButton;
    private MaterialDatePicker<Long> datePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        expensesDatabase = new ExpensesDatabase(getContext());
        amountInput = view.findViewById(R.id.editTextAmount);
        dateInput = view.findViewById(R.id.editTextDate);
        categorySpinner = view.findViewById(R.id.spinnerCategory);
        saveButton = view.findViewById(R.id.buttonSave);
        datePicker = DatePickerUtils.setupDatePicker();

        datePicker.addOnPositiveButtonClickListener (selection ->  {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateInput.setText(dateFormat.format(new Date(selection)));
        });

        saveButton.setOnClickListener(v -> saveExpense());

        dateInput.setOnClickListener(v -> datePicker.show(getChildFragmentManager(), "DATE_PICKER"));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Aggiungi spesa");
        }
    }

    private void saveExpense() {
        String amountStr = amountInput.getText().toString();
        String date = dateInput.getText().toString();
        String category = categorySpinner.getText().toString();

        if (amountStr.isEmpty() || date.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        boolean inserted = expensesDatabase.addExpense(getContext(), amount, category, date);

        if (inserted) {
            Toast.makeText(getContext(), getString(R.string.expanse_saved), Toast.LENGTH_SHORT).show();
            amountInput.setText("");
            dateInput.setText("");
            getParentFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), getString(R.string.expanse_not_saved), Toast.LENGTH_SHORT).show();
        }
    }
}