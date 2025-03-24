package it.bugbuster.asilapp.expenses;

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
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import it.bugbuster.asilapp.database.DatabaseHelper;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.utils.DateRangePickerUtils;

public class ExpenseListFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private ListView expenseListView;
    private AutoCompleteTextView categoryFilter;
    private EditText dateFilter;
    private MaterialDatePicker<Pair<Long, Long>> dateRangePicker;
    private SimpleDateFormat dateFormat;
    private TextView amountView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses_list, container, false);

        dbHelper = new DatabaseHelper(getContext());
        expenseListView = view.findViewById(R.id.listViewExpenses);
        categoryFilter = view.findViewById(R.id.spinnerFilterCategory);
        dateFilter = view.findViewById(R.id.editTextFilterDate);
        amountView = view.findViewById(R.id.totalAmount);
        dbHelper.syncLocalDataToFirestore(getContext());
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

        categoryFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                loadExpenses(true);
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
                loadExpenses(true);
            }
        });


        loadExpenses(false);

        return view;
    }

    private void loadExpenses(boolean filtered) {
        Cursor cursor = null;
        if (filtered) {
            String selectedCategory = categoryFilter.getText().toString();
            String selectedDate = dateFilter.getText().toString();
            cursor = dbHelper.getFilteredExpenses(selectedCategory, selectedDate);
        } else {
            cursor = dbHelper.getExpenses();
        }

        double totalAmount = 0;

        List<String> amountsWithSymbol = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int columnIndex = cursor.getColumnIndex("amount");
                double amount = cursor.getDouble(columnIndex);
                totalAmount += amount;
                amountsWithSymbol.add(String.format(Locale.getDefault(), "- %.2f", amount) + "€");
            } while (cursor.moveToNext());
        }

        cursor.moveToFirst();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.expenses_list,
                cursor,
                new String[]{"category", "date", "amount"},
                new int[]{R.id.item_title, R.id.item_subtitle, R.id.item_metadata},
                0
        ) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);

                TextView amountTextView = view.findViewById(R.id.item_metadata);
                String amount = amountsWithSymbol.get(cursor.getPosition());
                amountTextView.setText(amount);
            }
        };

        expenseListView.setAdapter(adapter);
        String total = getString(R.string.total_amount) + String.format(Locale.getDefault(), " %.2f", totalAmount);
        amountView.setText(total);
    }
}
