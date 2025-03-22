package it.bugbuster.asilapp.utils;

import androidx.core.util.Pair;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;
import java.util.TimeZone;

import it.bugbuster.asilapp.R;

public class DateRangePickerUtils {
    public static MaterialDatePicker<Pair<Long, Long>> setupDateRangePicker() {

            MaterialDatePicker.Builder<Pair<Long, Long>> dateRangePickerBuilder = MaterialDatePicker.Builder.dateRangePicker();
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            calendar.setTimeInMillis(today);
            calendar.add(Calendar.YEAR, -1);
            long oneYearAgo = calendar.getTimeInMillis();

            constraintsBuilder.setStart(oneYearAgo);
            constraintsBuilder.setEnd(today);
            constraintsBuilder.setValidator(DateValidatorPointBackward.before(today));

            dateRangePickerBuilder.setTitleText(R.string.select_dates);
            dateRangePickerBuilder.setSelection(new Pair<>(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    today
            ));

            dateRangePickerBuilder.setCalendarConstraints(constraintsBuilder.build());
            dateRangePickerBuilder.setTheme(R.style.ThemeOverlay_App_DateRangePicker);

            return dateRangePickerBuilder.build();
    }
}
