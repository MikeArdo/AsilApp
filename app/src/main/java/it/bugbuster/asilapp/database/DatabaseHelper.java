package it.bugbuster.asilapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_EXPANSES_TABLE = "CREATE TABLE expenses (_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT, amount REAL, category TEXT, date TEXT)";
    private static final String CREATE_MEASUREMENTS_TABLE = "CREATE TABLE measurements (_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT, type TEXT, date TEXT, value TEXT)";
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EXPANSES_TABLE);
        db.execSQL(CREATE_MEASUREMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS expanses");
        db.execSQL("DROP TABLE IF EXISTS measurements");
        onCreate(db);
    }

}
