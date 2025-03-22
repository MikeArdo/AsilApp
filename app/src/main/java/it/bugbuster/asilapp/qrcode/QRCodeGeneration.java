package it.bugbuster.asilapp.qrcode;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import java.util.Hashtable;

public class QRCodeGeneration {

    public static Bitmap generateQRCode(String data) throws Exception {
        MultiFormatWriter writer = new MultiFormatWriter();
        Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // Generate the BitMatrix from the input string (email:password)
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512, hints);

        // Convert the BitMatrix into a Bitmap for display
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        // Set the pixels for the Bitmap based on the BitMatrix
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Black and white
            }
        }

        return bitmap;
    }
}