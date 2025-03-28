package it.bugbuster.asilapp.diseases;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.TabsFragment;
import it.bugbuster.asilapp.database.DiseasesDatabase;
import it.bugbuster.asilapp.entity.User;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NavigationUtil;

public class DiseasesListFragment extends Fragment {
    private OnBackPressedCallback callback;
    private static final String ARG_USER = "user";
    private User user;
    private DiseasesDatabase diseasesDatabase;
    private ListView diseaseListView;

    public DiseasesListFragment() {

    }



    public static DiseasesListFragment newInstance(User user) {
        DiseasesListFragment fragment = new DiseasesListFragment();
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

    public void onResume() {
        super.onResume();
        NavigationUtil.showBackButton(this);

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                    View parentView = requireView().getRootView();
                    TabLayout tabLayout = parentView.findViewById(R.id.tabLayout);

                    // Select the "Profile" tab when the user presses back (tab position = 1)
                    tabLayout.getTabAt(0).select();

                callback.remove();
            }
        };
        Fragment currentFragment = getParentFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof TabsFragment) {
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        }
    }

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
        String userId;
        if (user != null) {
            userId = user.getId();
        } else {
            userId = AuthUtils.getCurrentUserId();
        }

        if (userId == null) return;
        Cursor cursor = diseasesDatabase.getDiseases(userId);

        if (cursor == null) return;

        cursor.moveToFirst();


        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_card,
                cursor,
                new String[]{"disease", "therapy"},
                new int[]{R.id.item_disease, R.id.item_therapy},
                0
        ) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                TextView fieldDoctor = view.findViewById(R.id.item_doctor);
                String idDoctor = cursor.getString(cursor.getColumnIndexOrThrow("doctor_id"));


                db.collection("doctors").document(idDoctor).get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String nameSurname = document.getString("name") + " " + document.getString("surname");
                                fieldDoctor.setText(nameSurname);
                            }
                        });
            }
        };

        diseaseListView.setAdapter(adapter);
    }
}
