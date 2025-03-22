package it.bugbuster.asilapp.qrcode;

import com.google.zxing.LuminanceSource;

public class MyRGBLuminanceSource extends LuminanceSource {
    private final byte[] luminances;

    public MyRGBLuminanceSource(int width, int height, int[] pixels) {
        super(width, height);
        luminances = new byte[width * height];

        // Iterate over each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Extract the red, green, and blue components for each pixel
                int pixel = pixels[y * width + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // Convert RGB to grayscale
                luminances[y * width + x] = (byte) ((r + g + b) / 3);
            }
        }
    }

    @Override
    public byte[] getMatrix() {
        return luminances;
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (row == null || row.length < getWidth()) {
            row = new byte[getWidth()];
        }
        System.arraycopy(luminances, y * getWidth(), row, 0, getWidth());
        return row;
    }
}
