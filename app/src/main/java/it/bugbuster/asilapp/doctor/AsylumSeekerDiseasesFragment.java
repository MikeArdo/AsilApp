package it.bugbuster.asilapp.doctor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.diseases.DiseasesListFragment;
import it.bugbuster.asilapp.entity.User;
import it.bugbuster.asilapp.utils.UserAvatarUtil;


public class AsylumSeekerDiseasesFragment extends Fragment {


    private static final String ARG_USER = "user";

    private User user;

    public AsylumSeekerDiseasesFragment() {
        // Required empty public constructor
    }

    public static AsylumSeekerDiseasesFragment newInstance(User user) {
        AsylumSeekerDiseasesFragment fragment = new AsylumSeekerDiseasesFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asylum_seeker_diseases, container, false);
        TextView itemName = view.findViewById(R.id.item_name);
        TextView itemBirthdate = view.findViewById(R.id.item_birthdate);
        ImageView itemAvatar = view.findViewById(R.id.user_avatar);

        if (user != null) {
            String name = user.getName();
            String surname = user.getSurname();
            String nameSurname = name + " " + surname;
            itemName.setText(nameSurname);
            itemBirthdate.setText(user.getBirthDate());
            UserAvatarUtil.setUserAvatar(name, surname, itemAvatar);
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_diseases, DiseasesListFragment.newInstance(user));
        transaction.commit();
        // Inflate the layout for this fragment
        return view;
    }
}