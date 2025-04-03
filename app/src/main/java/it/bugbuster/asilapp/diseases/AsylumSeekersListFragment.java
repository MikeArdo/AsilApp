package it.bugbuster.asilapp.diseases;

import static it.bugbuster.asilapp.AnimationFragment.setFragmentAnimation;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.adapter.UserAdapter;
import it.bugbuster.asilapp.entity.User;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NavigationUtil;


public class AsylumSeekersListFragment extends Fragment {
    private OnBackPressedCallback callback;
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

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asylum_seekers_list, container, false);

        asylumSeekersListView = view.findViewById(R.id.listViewAsylumSeekers);
        userList = new ArrayList<>();
        adapter = new UserAdapter(requireContext(), userList);
        asylumSeekersListView.setAdapter(adapter);
        NavigationUtil.showHomeButton(this);

        String logged_id = AuthUtils.getCurrentUserId();
        if (logged_id == null) return null;


        db = FirebaseFirestore.getInstance();
        fetchUsers();

        asylumSeekersListView.setOnItemClickListener((parent, v, position, id) -> {
            User clickedUser = userList.get(position);
            Fragment fragment = AsylumSeekerDiseasesFragment.newInstance(clickedUser);
            setFragmentAnimation(fragment);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
/*
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStack();
                callback.remove();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


 */


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