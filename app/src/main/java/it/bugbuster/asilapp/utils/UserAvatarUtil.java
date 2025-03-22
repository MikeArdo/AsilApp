package it.bugbuster.asilapp.utils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

import it.bugbuster.asilapp.R;

public class UserAvatarUtil {

    public static void setUserAvatar(String firstName, String lastName, ImageView imageView) {
        String initials = getInitials(firstName, lastName);

        Bitmap avatarBitmap = createCircleWithInitials(initials);

        imageView.setImageBitmap(avatarBitmap);
    }

    private static String getInitials(String firstName, String lastName) {
        String initials = String.valueOf(firstName.charAt(0)) + lastName.charAt(0);
        return initials.toUpperCase();
    }

    private static Bitmap createCircleWithInitials(String initials) {
        int size = 200;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FFC107"));
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        Rect bounds = new Rect();
        paint.getTextBounds(initials, 0, initials.length(), bounds);
        int x = size / 2;
        int y = size / 2 - (bounds.centerY());

        canvas.drawText(initials, x, y, paint);

        return bitmap;
    }
}

