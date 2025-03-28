package it.bugbuster.asilapp.doctor;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.UserAdapter;
import it.bugbuster.asilapp.database.DiseasesDatabase;
import it.bugbuster.asilapp.entity.AsylumSeeker;
import it.bugbuster.asilapp.entity.User;
import it.bugbuster.asilapp.measurements.Measurements;
import it.bugbuster.asilapp.measurements.TakeMeasurementsFragment;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NavigationUtil;
import it.bugbuster.asilapp.utils.UserAvatarUtil;


public class AsylumSeekersListFragment extends Fragment {
    private ListView asylumSeekersListView;
    private UserAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    public AsylumSeekersListFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asylum_seekers_list, container, false);

        asylumSeekersListView = view.findViewById(R.id.listViewAsylumSeekers);
        userList = new ArrayList<>();
        adapter = new UserAdapter(requireContext(), userList);
        asylumSeekersListView.setAdapter(adapter);

        String logged_id = AuthUtils.getCurrentUserId();
        if (logged_id == null) return null;


        db = FirebaseFirestore.getInstance();
        fetchUsers();

        asylumSeekersListView.setOnItemClickListener((parent, v, position, id) -> {
            User clickedUser = userList.get(position);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, AsylumSeekerDiseasesFragment.newInstance(clickedUser))
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void fetchUsers() {
        CollectionReference usersRef = db.collection("users"); // Collection name

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);
                    userList.add(user);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}