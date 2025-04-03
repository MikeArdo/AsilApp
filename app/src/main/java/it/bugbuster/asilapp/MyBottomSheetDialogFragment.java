package it.bugbuster.asilapp;

import static it.bugbuster.asilapp.AnimationFragment.setFragmentAnimation;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.bugbuster.asilapp.expenses.AddExpenseFragment;
import it.bugbuster.asilapp.measurements.ChooseMeasurementFragment;
import it.bugbuster.asilapp.utils.NavigationUtil;

public class MyBottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    private static final int BLUETOOTH_PERMISSION_REQUEST = 200;
    private static final String CORRECT_PASSWORD = "password";

    private BottomSheetDialog bottomSheetDialog;
    private BottomNavigationView bottomNav;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bottomSheetDialog = new BottomSheetDialog(
                requireContext(), R.style.ModalBottomSheetDialog
        );
        bottomSheetDialog.setContentView(R.layout.fragment_bottom_sheet);

        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        // Close button functionality
        Button btnAddExpanse = bottomSheetDialog.findViewById(R.id.add_expanse);
        Button btnChooseMeasurements = bottomSheetDialog.findViewById(R.id.chooseMeasurement);

        if (btnAddExpanse != null) {
            btnAddExpanse.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_list);
                Fragment fragment = new AddExpenseFragment();
                setFragmentAnimation(fragment);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                NavigationUtil.showBackButton(this);
            });
        }

        if (btnChooseMeasurements != null) {
            btnChooseMeasurements.setOnClickListener(v -> {
                showPasswordDialog();
            });
        }



        return bottomSheetDialog;
    }

    private void bluetoothConnection() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(requireContext(), getString(R.string.bluetooth_not_found), Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.BLUETOOTH_CONNECT) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    bluetoothLauncher.launch(enableBtIntent);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)) {
                    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.bluetooth_permission_request)
                            .setMessage(R.string.bluetooth_permission_description)
                            .setNegativeButton(requireContext().getResources().getString(R.string.no_thanks), (dialog, which) -> {
                                Toast.makeText(requireContext(), getString(R.string.bluetooth_permission),
                                        Toast.LENGTH_LONG).show();
                            })
                            .setPositiveButton(requireContext().getResources().getString(R.string.continue_text), (dialog, which) -> {
                                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                            });
                    dialogBuilder.create();
                    dialogBuilder.show();
                } else {
                    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.bluetooth_permission_request)
                            .setMessage(R.string.bluetooth_permission_description)
                            .setPositiveButton(requireContext().getResources().getString(R.string.continue_text), (dialog, which) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                                } else {
                                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    bluetoothLauncher.launch(enableBtIntent);
                                }
                            });
                    dialogBuilder.create();
                    dialogBuilder.show();

                }

            } else {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_home);
                Fragment fragment = new ChooseMeasurementFragment();
                setFragmentAnimation(fragment);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                NavigationUtil.showBackButton(this);
            }
        }
    }

    private void showPasswordDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.title_biomedical_container);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);

        builder.setView(dialogView);
        EditText passwordField = dialogView.findViewById(R.id.password);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String password = passwordField.getText().toString();
            if (password.equals(CORRECT_PASSWORD)) {
                bluetoothConnection();
                Toast.makeText(requireContext(), R.string.access_allowed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private final ActivityResultLauncher<Intent> bluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                    result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_home);
                Fragment fragment = new ChooseMeasurementFragment();
                setFragmentAnimation(fragment);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                NavigationUtil.showBackButton(this);
            } else {
                Toast.makeText(requireContext(), getString(R.string.bluetooth_not_enabled),
                        Toast.LENGTH_LONG).show();
            }
    });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    bluetoothLauncher.launch(enableBtIntent);
                } else {
                    Toast.makeText(requireContext(), getString(R.string.bluetooth_permission),
                            Toast.LENGTH_LONG).show();
                }
            });


    @Override
    public int getTheme() {
        return R.style.ModalBottomSheetDialog;
    }
}