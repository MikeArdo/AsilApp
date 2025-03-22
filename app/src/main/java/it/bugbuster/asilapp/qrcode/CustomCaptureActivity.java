package it.bugbuster.asilapp.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import it.bugbuster.asilapp.R;


public class CustomCaptureActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ActivityResultLauncher<Intent> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_capture);

        // Initialize BarcodeScannerView
        barcodeScannerView = findViewById(R.id.camera_preview);

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.setShowMissingCameraPermissionDialog(false);
        capture.decode();

        // Creation of "Scan from Gallery" button
        Button galleryButton = new Button(this);
        galleryButton.setText(R.string.scan_from_gallery);
        galleryButton.setPadding(20, 20, 20, 20);
        galleryButton.setBackgroundColor(getResources().getColor(R.color.primaryColor, getTheme()));
        galleryButton.setTextColor(0xFFFFFFFF);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );

        layoutParams.bottomMargin = 50;

        // Add the button to the scanner UI
        addContentView(galleryButton, layoutParams);

        //handle the result of an image picker intent
        pickImageLauncher = registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            decodeQRCodeFromImage(imageUri);
                        }
                    }
                });


        // Click listener for picking an image
        galleryButton.setOnClickListener(v -> pickImageFromGallery());
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void decodeQRCodeFromImage(Uri imageUri) {
        try {
            // Convert the selected image URI into a Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Create an integer array to store pixel data of the bitmap
            int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            // Convert the pixel data into a LuminanceSource
            LuminanceSource source = new MyRGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);

            // Convert the LuminanceSource into a BinaryBitmap for QR code processing
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Set decoding hints to improve QR code detection
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            // Decode the QR code using the MultiFormatReader
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);

            // Return the scanned result to the calling activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("QR_RESULT", result.getText());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } catch (IOException | NotFoundException e) {
            Toast.makeText(this, R.string.error_decode_qrcode, Toast.LENGTH_SHORT).show();
        }
    }
}