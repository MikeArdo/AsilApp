package it.bugbuster.asilapp.utils;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import it.bugbuster.asilapp.R;


public class DatePickerUtils {

    public static MaterialDatePicker<Long> setupDatePicker() {
        MaterialDatePicker.Builder<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker();
        materialDatePicker.setTitleText(R.string.select_a_date);

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setEnd(MaterialDatePicker.todayInUtcMilliseconds());
        constraintsBuilder.setValidator(DateValidatorPointBackward.before(MaterialDatePicker.todayInUtcMilliseconds()));
        materialDatePicker.setCalendarConstraints(constraintsBuilder.build());
        materialDatePicker.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        return materialDatePicker.build();
    }

}