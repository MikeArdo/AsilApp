package it.bugbuster.asilapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.bugbuster.asilapp.entity.Measurement;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NetworkUtils;

public class MeasurementsDatabase extends DatabaseHelper {
    private static final String DATABASE_NAME = "asilapp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_MEASUREMENTS = "measurements";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_USER_ID = "user_id";

    public MeasurementsDatabase(Context context) {
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

        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) return;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference measurementsRef = firestore.collection("users").document(userId).collection("measurements");

        measurementsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> firestoreMeasurementsIds = new HashSet<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                firestoreMeasurementsIds.add(doc.getId());
            }

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            if (isTableExists(db, TABLE_MEASUREMENTS)) {
                cursor = db.rawQuery("SELECT * FROM " + TABLE_MEASUREMENTS + " WHERE user_id = ?", new String[]{userId});
            } else {
                return;
            }


            if (cursor.moveToFirst()) {
                do {
                    int columnId = cursor.getColumnIndex(COLUMN_ID);

                    String measurementId = cursor.getString(columnId);

                    if (!firestoreMeasurementsIds.contains(measurementId)) {
                        int columnType = cursor.getColumnIndex(COLUMN_TYPE);
                        int columnDate = cursor.getColumnIndex(COLUMN_DATE);
                        int columnValue = cursor.getColumnIndex(COLUMN_VALUE);
                        String value = cursor.getString(columnValue);
                        String type = cursor.getString(columnType);
                        String date = cursor.getString(columnDate);

                        saveMeasurementsToFirestore(userId, measurementId, type, date, value);
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
        });
    }

    public void syncFirestoreToLocal(Context context) {
        if (!NetworkUtils.isNetworkAvailable(context)) return;

        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference measurementsRef = db.collection("users").document(userId).collection("measurements");

        measurementsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            SQLiteDatabase localDb = this.getWritableDatabase();
            localDb.beginTransaction();

            try {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String measurementId = document.getId();
                    String type = document.getString("type");
                    String date = document.getString("date");
                    String value = document.getString("value");

                    if (!isRecordAlreadyInLocal(localDb, measurementId)) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_USER_ID, userId);
                        values.put(COLUMN_TYPE, type);
                        values.put(COLUMN_DATE, date);
                        values.put(COLUMN_VALUE, value);

                        localDb.insert(TABLE_MEASUREMENTS, null, values);
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

    private boolean isRecordAlreadyInLocal(SQLiteDatabase db, String measurementId) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MEASUREMENTS + " WHERE " + COLUMN_ID +" = ?";
        Cursor cursor = db.rawQuery(query, new String[]{measurementId});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }


    public void saveMeasurementsToFirestore(String userId, String measurementId, String type, String date, String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userMeasurementsRef = db.collection("users").document(userId).collection("measurements");

        // Create a new measurement object
        Measurement measurement = new Measurement(type, date, value);

        // Add measurement to Firestore
        userMeasurementsRef.document(measurementId).set(measurement)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                    Log.d("Firestore", "Measurement added with ID: " + documentReference);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.w("Firestore", "Error adding measurement", e);
                });
    }

    public boolean addMeasurement(Context context, String type, String date, String value) {
        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) {
            Log.e("MeasurementDatabase", "Utente non loggato!");
            return false;
        }


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_VALUE, value);
        long result = db.insert(TABLE_MEASUREMENTS, null, values);
        if (NetworkUtils.isNetworkAvailable(context)) {
            saveMeasurementsToFirestore(userId, String.valueOf(result), type, date, value);
        }
        return result != -1;
    }

    public Cursor getMeasurements() {
        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        if (isTableExists(db, TABLE_MEASUREMENTS)) {
            return db.rawQuery("SELECT * FROM " + TABLE_MEASUREMENTS + " WHERE user_id = ?", new String[]{userId});
        } else {
            return null;
        }

    }

    public Cursor getFilteredMeasurements(String type, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM measurements WHERE 1=1";
        List<String> args = new ArrayList<>();

        if (!type.isEmpty()) {
            query += " AND type = ?";
            args.add(type);
        }

        if (!date.isEmpty()) {
            String[] parts = date.split(" - ");
            String startDate = parts[0];
            String endDate = parts[1];
            query += " AND SUBSTR(date, 1, 10) >= ? AND SUBSTR(date, 1, 10) <= ?";
            args.add(startDate);
            args.add(endDate);
        }


        if (isTableExists(db, TABLE_MEASUREMENTS)) {
            return db.rawQuery(query, args.toArray(new String[0]));
        } else {
            return null;
        }
    }
}
