package it.bugbuster.asilapp.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.adapter.ViewPagerAdapter;


public class TabsFragment extends Fragment {

    public TabsFragment() {
        // Required empty public constructor
    }

    public static TabsFragment newInstance(String param1, String param2) {
        TabsFragment fragment = new TabsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tabs, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        int tabCount = 2;
        ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity(), tabCount);
        viewPager.setAdapter(adapter);

        String[] tabNames = {getString(R.string.medical_parameters), getString(R.string.disease)};

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabNames[position]);
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
                    if (getActivity() != null) {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.diseases);
                    }
                } else {
                    callback.remove();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselected (optional)
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselection (optional)
            }
        });

        return view;
    }

    private OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            View parentView = requireView().getRootView();
            TabLayout tabLayout = parentView.findViewById(R.id.tabLayout);

            if (tabLayout.getSelectedTabPosition() == 1) {
                tabLayout.getTabAt(0).select();
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();

        View parentView = requireView().getRootView();
        TabLayout tabLayout = parentView.findViewById(R.id.tabLayout);
        if (tabLayout.getSelectedTabPosition() == 1) {
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        }
    }
}