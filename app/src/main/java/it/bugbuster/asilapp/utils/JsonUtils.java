package it.bugbuster.asilapp.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import it.bugbuster.asilapp.entity.RefugeeShelter;
import it.bugbuster.asilapp.entity.RefugeeShelterList;

public class JsonUtils {

    public static String loadJSONFromAsset(Context context, String filename) {
        String json = null;

        File file = new File(context.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                json = stringBuilder.toString();
                reader.close();
                return json;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            InputStream is = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            json = stringBuilder.toString();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static List<RefugeeShelter> parseRefugeeShelters(Context context, String filename) {
        String json = loadJSONFromAsset(context, filename);
        if (json != null) {
            Gson gson = new Gson();
            RefugeeShelterList refugeeShelterList = gson.fromJson(json, RefugeeShelterList.class);
            return refugeeShelterList != null ? refugeeShelterList.getRefugee_shelter() : new ArrayList<>();
        } else {
            return new ArrayList<>();
        }
    }

    public static void overwriteLocalData(Context context, List<RefugeeShelter> shelters, String filename) {
        Gson gson = new Gson();
        RefugeeShelterList refugeeShelterList = new RefugeeShelterList();
        refugeeShelterList.setRefugee_shelter(shelters);

        String json = gson.toJson(refugeeShelterList);

        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
            Log.d("Data Update", "Dati locali sovrascritti con successo.");
        } catch (IOException e) {
            Log.e("Data Update", "Errore durante la sovrascrittura dei dati locali.", e);
        }
    }
}
