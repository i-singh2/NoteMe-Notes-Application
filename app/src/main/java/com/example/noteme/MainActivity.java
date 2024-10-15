package com.example.noteme;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Database Helper and list of notes
        dbHelper = new DatabaseHelper(this);
        noteList = findViewById(R.id.noteList);

        // Load the notes and display them
        loadNotes("");

        // Setup the SearchView to filter notes
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);

        // Listening for the input in the search bar
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Filter notes based on query text
                loadNotes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Load all notes if search query is empty, otherwise filter notes
                if (newText.isEmpty()) {
                    loadNotes("");
                } else {
                    loadNotes(newText);
                }
                return false;
            }
        });

        // Setup the FloatingActionButton for new note creation
        FloatingActionButton newNoteButton = findViewById(R.id.newNote);

        // Set an OnClickListener to respond to new note button clicks
        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to go to new note page when the new note button is clicked
                Intent intent = new Intent(MainActivity.this, CreateNewNote.class);
                startActivity(intent);
            }
        });
    }

    // Method to load and display notes from the database
    private void loadNotes(String filter) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to select notes, filtering by title if provided
        Cursor cursor;
        if (filter.isEmpty()) {
            // Get all the notes
            cursor = db.query(DatabaseHelper.TABLE_NAME,
                    new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_SUBTITLE, DatabaseHelper.COLUMN_DESCRIPTION, DatabaseHelper.COLUMN_COLOR},
                    null, null, null, null, null);
        } else {
            // Filter notes by title
            cursor = db.query(DatabaseHelper.TABLE_NAME,
                    new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_SUBTITLE, DatabaseHelper.COLUMN_DESCRIPTION, DatabaseHelper.COLUMN_COLOR},
                    DatabaseHelper.COLUMN_TITLE + " LIKE ?",
                    new String[]{"%" + filter + "%"}, null, null, null);
        }

        noteList.removeAllViews();  // Clear previous notes

        while (cursor.moveToNext()) {
            // Get note data from the cursor
            int noteId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
            String subtitle = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            String color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR));

            // Create a TextView for each note
            TextView noteView = new TextView(this);
            noteView.setText(title + "\n\t\t\t" + subtitle + "\n\n" + description);
            noteView.setBackgroundColor(Color.parseColor(color)); // Set the background color
            noteView.setPadding(16, 16, 16, 16);
            noteView.setTextColor(Color.BLACK);
            noteView.setTextSize(18);

            // Adjust the note view to wrap content, this is what makes it change dynamically based on the size of the TextView
            LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Set margins
            parameters.setMargins(0, 0, 0, 20);

            // Apply parameters
            noteView.setLayoutParams(parameters);

            // Set a click listener to edit the note when clicked
            noteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start CreateNewNote activity for editing, pass the noteId
                    Intent intent = new Intent(MainActivity.this, CreateNewNote.class);
                    intent.putExtra("noteId", noteId);  // Pass the noteId for editing
                    startActivity(intent);
                }
            });

            // Set a long-click listener to delete the note when long pressed
            noteView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Confirm delete dialog
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Note")
                            .setMessage("Are you sure you want to delete this note?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Delete the note
                                boolean isDeleted = dbHelper.deleteNoteById(noteId);
                                if (isDeleted) {
                                    Toast.makeText(MainActivity.this, "Note deleted!", Toast.LENGTH_SHORT).show();
                                    loadNotes("");  // Reload the notes after deletion
                                } else {
                                    Toast.makeText(MainActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;  // Return true to indicate the long-click was handled
                }
            });

            // Add the note view to the LinearLayout
            noteList.addView(noteView);
        }

        cursor.close();
    }
}
