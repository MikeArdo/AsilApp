package it.bugbuster.asilapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Objects;

import it.bugbuster.asilapp.access.Login;
import it.bugbuster.asilapp.expenses.AddExpenseFragment;
import it.bugbuster.asilapp.measurements.TakeMeasurementsFragment;

public class MyBottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    private static final int BLUETOOTH_PERMISSION_REQUEST = 200;
    private static final String CORRECT_PASSWORD = "password";

    private BottomSheetDialog bottomSheetDialog;
    private BottomNavigationView bottomNav;
/*
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);



        return view;
    }

 */

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
        Button btnTemperature = bottomSheetDialog.findViewById(R.id.temperature);
        Button btnHeartBeat = bottomSheetDialog.findViewById(R.id.hearthbeat);

        if (btnAddExpanse != null) {
            btnAddExpanse.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_list);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddExpenseFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (btnTemperature != null) {
            btnTemperature.setOnClickListener(v -> {
                showPasswordDialog();
                /*
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_home);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.TEMPERATURE))
                        .addToBackStack(null)
                        .commit();

                 */
            });
        }

        if (btnHeartBeat != null) {
            btnHeartBeat.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_home);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.HEARTBEAT))
                        .addToBackStack(null)
                        .commit();
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
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.TEMPERATURE))
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    private void showPasswordDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.title_biomedical_container);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);

        builder.setView(dialogView);
        EditText passwordField = dialogView.findViewById(R.id.password);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = passwordField.getText().toString();
            if (password.equals(CORRECT_PASSWORD)) {
                bluetoothConnection();
                Toast.makeText(requireContext(), R.string.access_allowed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private final ActivityResultLauncher<Intent> bluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                    result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                bottomSheetDialog.dismiss();
                bottomNav.setSelectedItemId(R.id.nav_home);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TakeMeasurementsFragment.newInstance(Measurements.TEMPERATURE))
                        .addToBackStack(null)
                        .commit();
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