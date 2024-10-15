package com.example.noteme;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class CreateNewNote extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private String selectedColor = "#DEDEDE"; // Default color
    private DatabaseHelper dbHelper;

    private EditText inputNoteTitle;
    private EditText inputNoteSubtitle;
    private EditText inputNoteDescription;
    private ImageView noteImageView; // ImageView to display selected or captured image
    private Uri imageUri; // URI of selected or captured image
    private File savedImageFile; // File to store the image locally

    private int noteId = -1; // Initialize noteId to -1 (used for new notes or editing existing)

    // ActivityResultLauncher for camera result and gallery selection
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_note);

        // Initialize the note inputs
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteDescription = findViewById(R.id.InputText);
        noteImageView = findViewById(R.id.noteImageView); // ImageView for displaying image

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Check if we are editing an existing note (if noteId is passed via intent)
        Intent intent = getIntent();
        if (intent.hasExtra("noteId")) {
            noteId = intent.getIntExtra("noteId", -1);  // Get the noteId from the intent
            loadNoteForEditing(noteId);  // Load the note data for editing
        }

        // Initialize the ActivityResultLauncher for both camera and gallery intents
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                // This means the result is from the gallery (we got a Uri from gallery)
                                Uri selectedImageUri = data.getData();
                                noteImageView.setImageURI(selectedImageUri);  // Display the gallery image
                            } else if (imageUri != null) {
                                // This means the result is from the camera (we have our pre-created Uri)
                                noteImageView.setImageURI(imageUri);  // Display the captured image
                            } else {
                                Toast.makeText(CreateNewNote.this, "Image selection failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CreateNewNote.this, "Image selection canceled", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Button to choose or capture an image
        Button buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonChooseImage.setOnClickListener(v -> showImageChooserDialog());

        // Color selection buttons
        Button colorYellow = findViewById(R.id.colorYellow);
        Button colorOrange = findViewById(R.id.colorOrange);
        Button colorGreen = findViewById(R.id.colorGreen);

        // Color selection functionality
        colorYellow.setOnClickListener(v -> {
            selectedColor = "#FFFF00";
            Toast.makeText(CreateNewNote.this, "Selected Yellow", Toast.LENGTH_SHORT).show();
        });

        colorOrange.setOnClickListener(v -> {
            selectedColor = "#FFA500";
            Toast.makeText(CreateNewNote.this, "Selected Orange", Toast.LENGTH_SHORT).show();
        });

        colorGreen.setOnClickListener(v -> {
            selectedColor = "#008000";
            Toast.makeText(CreateNewNote.this, "Selected Green", Toast.LENGTH_SHORT).show();
        });

        // Save and Cancel buttons functionality
        FloatingActionButton returnHome = findViewById(R.id.goBack);
        FloatingActionButton saveNote = findViewById(R.id.saveNote);

        returnHome.setOnClickListener(view -> {
            Intent intent1 = new Intent(CreateNewNote.this, MainActivity.class);
            startActivity(intent1);
        });

        saveNote.setOnClickListener(view -> saveNote());
    }

    // Show a dialog to choose between uploading from gallery or capturing a new image
    private void showImageChooserDialog() {
        String[] options = {"Upload from Gallery", "Capture from Camera"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choose an option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openImageChooser();  // Upload from Gallery
                    } else {
                        captureImage();  // Capture from Camera
                    }
                })
                .show();
    }

    // Open the device gallery to choose an image
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);  // Launch the gallery intent
    }

    // Capture a new image using the device's camera
    private void captureImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();  // Create a file to store the captured image
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file for image", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(this, "com.example.noteme.provider", photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            activityResultLauncher.launch(cameraIntent);  // Launch the camera intent using the new Activity Result API
        }
    }

    // Create a file to store the captured image
    private File createImageFile() throws IOException {
        String imageFileName = "captured_image_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);  // Save in external storage
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        savedImageFile = image;
        return image;
    }

    // Load existing note data into the fields for editing
    private void loadNoteForEditing(int id) {
        Cursor cursor = dbHelper.getNoteById(id); // Fetch note from the database
        if (cursor != null && cursor.moveToFirst()) {
            inputNoteTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
            inputNoteSubtitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTITLE)));
            inputNoteDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
            selectedColor = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR)); // Set the color for editing

            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));
            if (imagePath != null && !imagePath.isEmpty()) {
                savedImageFile = new File(imagePath);
                noteImageView.setImageURI(Uri.fromFile(savedImageFile)); // Display the image from file
            }
            cursor.close();
        }
    }

    // Method to save or update the note
    private void saveNote() {
        String title = inputNoteTitle.getText().toString();
        String subtitle = inputNoteSubtitle.getText().toString();
        String description = inputNoteDescription.getText().toString();
        String imagePath = savedImageFile != null ? savedImageFile.getAbsolutePath() : ""; // Save the path to the file

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noteId == -1) {
            // New note
            boolean isInserted = dbHelper.insertData(title, subtitle, description, selectedColor, imagePath);
            if (isInserted) {
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update existing note
            boolean isUpdated = dbHelper.updateData(noteId, title, subtitle, description, selectedColor, imagePath);
            if (isUpdated) {
                Toast.makeText(this, "Note updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show();
            }
        }

        // Return to the main screen after the note is saved
        Intent intent = new Intent(CreateNewNote.this, MainActivity.class);
        startActivity(intent);
    }
}
