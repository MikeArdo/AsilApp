package it.bugbuster.asilapp.utils;

import android.app.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import it.bugbuster.asilapp.R;

public class NavigationUtil {

    public static void showHomeButton(AppCompatActivity activity) {
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setLogo(R.drawable.home_white_24px);
            }
        }
    }

    public static void showHomeButton(Fragment fragment) {
        if (fragment != null) {
            Activity activity = fragment.getActivity();
            if (activity instanceof AppCompatActivity) {
                showHomeButton((AppCompatActivity) activity);
            }
        }
    }

    public static void showBackButton(AppCompatActivity activity) {
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
            }
        }

    }

    public static void showBackButton(Fragment fragment) {
        if (fragment != null) {
            Activity activity = fragment.getActivity();
            if (activity instanceof AppCompatActivity) {
                showBackButton((AppCompatActivity) activity);
            }
        }
    }
}
