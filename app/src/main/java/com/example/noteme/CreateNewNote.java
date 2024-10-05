package com.example.noteme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CreateNewNote extends AppCompatActivity {

    private String selectedColor = "#DEDEDE"; // sets a default colour until they choose
    private DatabaseHelper dbHelper;

    private EditText inputNoteTitle;
    private EditText inputNoteSubtitle;
    private EditText inputNoteDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_note);

        // Initialize the note inputs
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteDescription = findViewById(R.id.InputText);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize color buttons
        Button colorYellow = findViewById(R.id.colorYellow);
        Button colorOrange = findViewById(R.id.colorOrange);
        Button colorGreen = findViewById(R.id.colorGreen);

        // set note color to yellow
        colorYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#FFFF00";
                Toast.makeText(CreateNewNote.this, "Selected Yellow", Toast.LENGTH_SHORT).show();
            }
        });

        // set note color to orange
        colorOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#FFA500";
                Toast.makeText(CreateNewNote.this, "Selected Orange", Toast.LENGTH_SHORT).show();
            }
        });

        // set note color to green
        colorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#008000"; // green
                Toast.makeText(CreateNewNote.this, "Selected Green", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup the FloatingActionButtons
        FloatingActionButton returnHome = findViewById(R.id.goBack); //button to cancel and return home
        FloatingActionButton saveNote = findViewById(R.id.saveNote); //button to save note and return to home

        // Set an OnClickListener to respond to return home button clicks
        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to go to home page when the return home button is clicked
                Intent intent1 = new Intent(CreateNewNote.this, MainActivity.class);
                startActivity(intent1);
            }
        });

        // Set an OnClickListener to respond to save button clicks
        saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote(); //call SaveNote method on save button click
            }
        });
    }

    //method to save note
    private void saveNote() {
        // get text from the input fields
        String title = inputNoteTitle.getText().toString();
        String subtitle = inputNoteSubtitle.getText().toString();
        String description = inputNoteDescription.getText().toString();

        // check if title empty
        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // insert the note into the database
        boolean isInserted = dbHelper.insertData(title, subtitle, description, selectedColor);

        // show a message depending on whether the data was saved successfully
        if (isInserted) {
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
            // Intent to go to home page when the save button is clicked
            Intent intent2 = new Intent(CreateNewNote.this, MainActivity.class);
            startActivity(intent2);
        } else {
            Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
        }
    }
}