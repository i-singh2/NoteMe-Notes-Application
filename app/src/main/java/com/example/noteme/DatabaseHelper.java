package com.example.noteme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SUBTITLE = "subtitle";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_IMAGE = "image";
    private static final String DATABASE_NAME = "NOTES_RECORD";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_SUBTITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_COLOR + " TEXT, " +
                COLUMN_IMAGE + " TEXT)";
        db.execSQL(createTable);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If upgrading from an older version, add the 'image' column
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_IMAGE + " TEXT");
        }
    }

    //insert new note into db
    public boolean insertData(String title, String subtitle, String description, String color, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_SUBTITLE, subtitle);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_IMAGE, imagePath);
        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    //Save new changes made to an existing note into db
    public boolean updateData(int id, String title, String subtitle, String description, String color, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_SUBTITLE, subtitle);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_IMAGE, imagePath);
        int result = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    //Get a note by its ID
    public Cursor getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_SUBTITLE, COLUMN_DESCRIPTION, COLUMN_COLOR, COLUMN_IMAGE},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }

    // Delete a note by its ID
    public boolean deleteNoteById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0; // Return true if a row was deleted
    }
}

