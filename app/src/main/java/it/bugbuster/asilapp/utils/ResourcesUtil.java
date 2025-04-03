package it.bugbuster.asilapp.utils;

import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.Field;

import it.bugbuster.asilapp.R;

public class ResourcesUtil {
    public static String getStringResourceName(Context context, String value) {
        Resources res = context.getResources();
        Field[] fields = R.string.class.getFields();

        for (Field field : fields) {
            try {
                int resId = field.getInt(null);
                if (res.getString(resId).equals(value)) {
                    return field.getName();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
