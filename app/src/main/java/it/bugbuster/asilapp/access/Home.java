package it.bugbuster.asilapp.access;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.bugbuster.asilapp.InformationFragment;
import it.bugbuster.asilapp.MedicalParametersFragment;
import it.bugbuster.asilapp.MyBottomSheetDialogFragment;
import it.bugbuster.asilapp.ProfileFragment;
import it.bugbuster.asilapp.R;
import it.bugbuster.asilapp.expenses.ExpenseListFragment;


public class Home extends AppCompatActivity {
    private FloatingActionButton fab;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        fab = findViewById(R.id.floating_action_button);


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);


        if (savedInstanceState == null) {
            showHomeButton();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MedicalParametersFragment())
                    .commit();
        }

        fab.setOnClickListener(view -> {
            //bottomNav.setSelectedItemId(R.id.nav_list);
            MyBottomSheetDialogFragment bottomSheet = new MyBottomSheetDialogFragment();
            bottomSheet.show(getSupportFragmentManager(), "MyBottomSheetDialog");
            /* TODO remove
            getSupportFragmentManager().beginTransaction()
                    //.replace(R.id.fragment_container, new AddExpenseFragment())
                    .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.GLYCEMIA))
                    .addToBackStack(null)
                    .commit();

             */
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                showHomeButton();
                selectedFragment = new MedicalParametersFragment();
            } else if (item.getItemId() == R.id.nav_info){
                selectedFragment = new InformationFragment();
            } else if (item.getItemId() == R.id.nav_list){
                selectedFragment = new ExpenseListFragment();
            } else if (item.getItemId() == R.id.nav_profile){
                //myToolbar.setElevation(0f);
                selectedFragment = new ProfileFragment();
            }


            if (item.getItemId() != R.id.nav_home) {
                showBackButton();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack("home")
                        .commit();
            }
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

                if (backStackCount > 0) {
                    String backStackName = getSupportFragmentManager().getBackStackEntryAt(backStackCount - 1).getName();

                    if ("home".equals(backStackName)) {
                        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                        if (!(currentFragment instanceof MedicalParametersFragment)) {
                            bottomNav.setSelectedItemId(R.id.nav_home);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new MedicalParametersFragment())
                                    .commit();
                        } else {
                            finish();
                        }
                    } else {

                        getSupportFragmentManager().popBackStack();
                    }
                } else {
                    finish();
                }
            }
        });


    }

    private void showHomeButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.drawable.home_white_24px); // Your home logo/icon
        }
    }

    private void showBackButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}