package com.example.noteme;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Setup the FloatingActionButtons
        FloatingActionButton newNoteButton = findViewById(R.id.newNote);

        // Set an OnClickListener to respond to new note button clicks
        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to go to new note page when the new note button is clicked
                Intent intent=new Intent(MainActivity.this,CreateNewNote.class);
                startActivity(intent);
            }
        });
    }
}