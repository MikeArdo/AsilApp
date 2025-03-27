package it.bugbuster.asilapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.Set;

import it.bugbuster.asilapp.entity.Disease;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NetworkUtils;

public class DiseasesDatabase extends DatabaseHelper {
    private static final String DATABASE_NAME = "asilapp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_DISEASES = "diseases";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_DOCTOR_ID = "doctor_id";
    private static final String COLUMN_DISEASE = "disease";
    private static final String COLUMN_THERAPY = "therapy";

    public DiseasesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public boolean isTableExists(SQLiteDatabase db, String tableName) {
        boolean exists = false;
        Cursor cursor = null;
        try {
            String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            cursor = db.rawQuery(query, new String[]{tableName});
            exists = cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }


    public void syncLocalDataToFirestore(Context context) {
        if (!NetworkUtils.isNetworkAvailable(context)) return;

        String doctor_id = AuthUtils.getCurrentUserId();
        if (doctor_id == null) return;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference diseasesRef = firestore.collection(TABLE_DISEASES);

        diseasesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> firestoreDiseasesIds = new HashSet<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                firestoreDiseasesIds.add(doc.getId());
            }

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            if (isTableExists(db, TABLE_DISEASES)) {
                cursor = db.rawQuery("SELECT * FROM " + TABLE_DISEASES + " WHERE " + COLUMN_DOCTOR_ID + " = ?", new String[]{doctor_id});
            } else {
                return;
            }


            if (cursor.moveToFirst()) {
                do {
                    int column_id = cursor.getColumnIndex(COLUMN_ID);
                    int column_user_id = cursor.getColumnIndex(COLUMN_USER_ID);
                    int column_doctor_id = cursor.getColumnIndex(COLUMN_DOCTOR_ID);
                    String disease_id = cursor.getString(column_id);
                    String user_id = cursor.getString(column_user_id);
                    String firestore_disease_id = disease_id + "-" + user_id + "-" + doctor_id;


                    if (!firestoreDiseasesIds.contains(firestore_disease_id)) {
                        int columnDisease = cursor.getColumnIndex(COLUMN_DISEASE);
                        int columnTherapy = cursor.getColumnIndex(COLUMN_THERAPY);
                        String disease = cursor.getString(columnDisease);
                        String therapy = cursor.getString(columnTherapy);

                        saveDiseaseToFirestore(disease_id, user_id, doctor_id, disease, therapy);
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
        });
    }

    public void syncFirestoreToLocal(Context context) {
        if (!NetworkUtils.isNetworkAvailable(context)) return;

        String logged_id = AuthUtils.getCurrentUserId();
        if (logged_id == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference diseasesRef = db.collection("diseases");

        diseasesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            SQLiteDatabase localDb = this.getWritableDatabase();
            localDb.beginTransaction();

            try {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String firestore_disease_id = document.getId();
                    String disease_id = firestore_disease_id.split("-")[0];
                    String user_id = document.getString("user_id");
                    String doctor_id = document.getString("doctor_id");
                    String disease = document.getString("disease");
                    String therapy = document.getString("therapy");

                    if (!isRecordAlreadyInLocal(localDb, disease_id, user_id, doctor_id)) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_USER_ID, user_id);
                        values.put(COLUMN_DOCTOR_ID, doctor_id);
                        values.put(COLUMN_DISEASE, disease);
                        values.put(COLUMN_THERAPY, therapy);

                        localDb.insert(TABLE_DISEASES, null, values);
                    }

                }

                localDb.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                localDb.endTransaction();
                localDb.close();
            }
        }).addOnFailureListener(e -> {
            Log.e("SyncError", "Failed to sync Firestore to SQLite: " + e.getMessage());
        });
    }

    private boolean isRecordAlreadyInLocal(SQLiteDatabase db, String disease_id, String user_id, String doctor_id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_DISEASES + " WHERE " + COLUMN_ID +" = ? AND " +
                COLUMN_USER_ID + " = ? AND " + COLUMN_DOCTOR_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{disease_id, user_id, doctor_id});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }


    public void saveDiseaseToFirestore(String disease_id, String user_id, String doctor_id, String disease, String therapy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userDiseasesRef = db.collection(TABLE_DISEASES);

        // Create a new disease object
        Disease diseaseObj = new Disease(user_id, doctor_id, disease, therapy);

        String firestore_disease_id = disease_id + '-' + user_id + '-' + doctor_id;

        // Add disease to Firestore
        userDiseasesRef.document(firestore_disease_id).set(diseaseObj)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                    Log.d("Firestore", "Measurement added with ID: " + documentReference);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.w("Firestore", "Error adding measurement", e);
                });
    }

    public boolean addDisease(Context context, String user_id, String disease, String therapy) {
        String doctor_id = AuthUtils.getCurrentUserId();
        if (doctor_id == null) {
            Log.e("MeasurementDatabase", "Utente non loggato!");
            return false;
        }


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, user_id);
        values.put(COLUMN_DOCTOR_ID, doctor_id);
        values.put(COLUMN_DISEASE, disease);
        values.put(COLUMN_THERAPY, therapy);
        long result = db.insert(TABLE_DISEASES, null, values);
        if (NetworkUtils.isNetworkAvailable(context)) {
            saveDiseaseToFirestore(String.valueOf(result), user_id, doctor_id, disease, therapy);
        }
        return result != -1;
    }

    public Cursor getDiseases(String user_id) {
        String logged_id = AuthUtils.getCurrentUserId();
        if (logged_id == null) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        if (isTableExists(db, TABLE_DISEASES)) {
            return db.rawQuery("SELECT * FROM " + TABLE_DISEASES + " WHERE user_id = ?", new String[]{user_id});
        } else {
            return null;
        }

    }
}
