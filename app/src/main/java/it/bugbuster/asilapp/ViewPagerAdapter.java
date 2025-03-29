package it.bugbuster.asilapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.bugbuster.asilapp.diseases.DiseasesListFragment;
import it.bugbuster.asilapp.measurements.MedicalParametersFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final int numOfTabs;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int numOfTabs) {
        super(fragmentActivity);
        this.numOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new MedicalParametersFragment();
        } else {
            return new DiseasesListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
