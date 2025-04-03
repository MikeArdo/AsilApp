package it.bugbuster.asilapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.bugbuster.asilapp.entity.Expense;
import it.bugbuster.asilapp.utils.AuthUtils;
import it.bugbuster.asilapp.utils.NetworkUtils;

public class ExpensesDatabase extends DatabaseHelper {
    private static final String DATABASE_NAME = "asilapp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_USER_ID = "user_id";

    public ExpensesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void syncLocalDataToFirestore(Context context) {
        if (!NetworkUtils.isNetworkAvailable(context)) return;

        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) return;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference expensesRef = firestore.collection("users").document(userId).collection("expenses");

        expensesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> firestoreExpenseIds = new HashSet<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                firestoreExpenseIds.add(doc.getId());
            }

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            if (isTableExists(db, TABLE_EXPENSES)) {
                cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " WHERE user_id = ?", new String[]{userId});
            } else {
                return;
            }


            if (cursor.moveToFirst()) {
                do {
                    int columnId = cursor.getColumnIndex(COLUMN_ID);

                    String expenseId = cursor.getString(columnId);

                    if (!firestoreExpenseIds.contains(expenseId)) {
                        int columnAmount = cursor.getColumnIndex(COLUMN_AMOUNT);
                        int columnCategory = cursor.getColumnIndex(COLUMN_CATEGORY);
                        int columnDate = cursor.getColumnIndex(COLUMN_DATE);
                        double amount = cursor.getDouble(columnAmount);
                        String category = cursor.getString(columnCategory);
                        String date = cursor.getString(columnDate);

                        saveExpenseToFirestore(userId, expenseId, amount, category, date);
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
        CollectionReference expensesRef = db.collection("users").document(userId).collection("expenses");

        expensesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            SQLiteDatabase localDb = this.getWritableDatabase();
            localDb.beginTransaction();

            try {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String expanseId = document.getId();
                    double amount = document.getDouble("amount");
                    String category = document.getString("category");
                    String date = document.getString("date");

                    if (!isRecordAlreadyInLocal(localDb, expanseId)) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_USER_ID, userId);
                        values.put(COLUMN_AMOUNT, amount);
                        values.put(COLUMN_CATEGORY, category);
                        values.put(COLUMN_DATE, date);

                        localDb.insert(TABLE_EXPENSES, null, values);
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

    private boolean isRecordAlreadyInLocal(SQLiteDatabase db, String expanseId) {
        String query = "SELECT COUNT(*) FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_ID +" = ?";
        Cursor cursor = null;

        if (isTableExists(db, TABLE_EXPENSES)) {
            cursor = db.rawQuery(query, new String[]{expanseId});
        } else {
            return false;
        }
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }


    public void saveExpenseToFirestore(String userId, String expanseId, double amount, String category, String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userExpensesRef = db.collection("users").document(userId).collection("expenses");

        // Create a new expense object
        Expense expense = new Expense(amount, category, date);

        // Add expense to Firestore
        userExpensesRef.document(expanseId).set(expense)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                    Log.d("Firestore", "Expense added with ID: " + documentReference);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.w("Firestore", "Error adding expense", e);
                });
    }

    public boolean addExpense(Context context, double amount, String category, String date) {
        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) {
            Log.e("ExpensesDatabase", "Utente non loggato!");
            return false;
        }


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        long result = db.insert(TABLE_EXPENSES, null, values);
        if (NetworkUtils.isNetworkAvailable(context)) {
            saveExpenseToFirestore(userId, String.valueOf(result), amount, category, date);
        }
        return result != -1;
    }

    public Cursor getExpenses() {
        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        if (isTableExists(db, TABLE_EXPENSES)) {
            return db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " WHERE user_id = ?", new String[]{userId});
        } else {
            return null;
        }

    }

    public Cursor getFilteredExpenses(String category, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM expenses WHERE 1=1";
        List<String> args = new ArrayList<>();

        if (!category.isEmpty()) {
            query += " AND category = ?";
            args.add(category);
        }

        if (!date.isEmpty()) {
            String[] parts = date.split(" - ");
            String startDate = parts[0];
            String endDate = parts[1];
            query += " AND date >= ? AND date <= ?";
            args.add(startDate);
            args.add(endDate);
        }

        if (isTableExists(db, TABLE_EXPENSES)) {
            return db.rawQuery(query, args.toArray(new String[0]));
        } else {
            return null;
        }

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

}
