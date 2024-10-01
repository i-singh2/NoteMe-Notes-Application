package com.example.noteme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CreateNewNote extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_note);

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
                // Intent to go to home page when the save button is clicked
                Intent intent2 = new Intent(CreateNewNote.this, MainActivity.class);
                startActivity(intent2);
            }
        });
    }
}