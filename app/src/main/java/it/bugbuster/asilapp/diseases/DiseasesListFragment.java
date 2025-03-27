package it.bugbuster.asilapp.diseases;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.database.DiseasesDatabase;
import it.bugbuster.asilapp.utils.AuthUtils;

public class DiseasesListFragment extends Fragment {
    private DiseasesDatabase diseasesDatabase;
    private ListView diseaseListView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diseases_list, container, false);

        diseasesDatabase = new DiseasesDatabase(getContext());
        diseaseListView = view.findViewById(R.id.listViewDiseases);
        diseasesDatabase.syncFirestoreToLocal(getContext());

        loadDiseases();

        return view;
    }

    private void loadDiseases() {
        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) return;
        Cursor cursor = diseasesDatabase.getDiseases(userId);

        if (cursor == null) return;

        cursor.moveToFirst();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_card,
                cursor,
                new String[]{"disease", "therapy", "doctor_id"},
                new int[]{R.id.item_disease, R.id.item_therapy, R.id.item_doctor},
                0
        ) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);

               // TextView amountTextView = view.findViewById(R.id.item_metadata);
                //amountTextView.setText(amount);
            }
        };

        diseaseListView.setAdapter(adapter);
    }
}
