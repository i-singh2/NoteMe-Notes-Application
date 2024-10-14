package com.example.noteme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateNewNote extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private String selectedColor = "#DEDEDE"; // Default color
    private DatabaseHelper dbHelper;

    private EditText inputNoteTitle;
    private EditText inputNoteSubtitle;
    private EditText inputNoteDescription;
    private ImageView noteImageView; // ImageView to display selected or captured image
    private Uri imageUri; // URI of selected or captured image
    private String currentPhotoPath; // Store the captured image path
    private int noteId = -1; // Track if editing an existing note
    private File savedImageFile; // File to store the image locally
    private ActivityResultLauncher<Intent> captureImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_note);

        // Initialize the note inputs
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteDescription = findViewById(R.id.InputText);
        noteImageView = findViewById(R.id.noteImageView); // ImageView for displaying image

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Check if we're editing an existing note
        Intent intent = getIntent();
        if (intent.hasExtra("noteId")) {
            noteId = intent.getIntExtra("noteId", -1);
            loadNoteForEditing(noteId); // Load existing note data
        }

        // Initialize color buttons
        Button colorYellow = findViewById(R.id.colorYellow);
        Button colorOrange = findViewById(R.id.colorOrange);
        Button colorGreen = findViewById(R.id.colorGreen);

        // Set note color to yellow
        colorYellow.setOnClickListener(v -> {
            selectedColor = "#FFFF00";
            Toast.makeText(CreateNewNote.this, "Selected Yellow", Toast.LENGTH_SHORT).show();
        });

        // Set note color to orange
        colorOrange.setOnClickListener(v -> {
            selectedColor = "#FFA500";
            Toast.makeText(CreateNewNote.this, "Selected Orange", Toast.LENGTH_SHORT).show();
        });

        // Set note color to green
        colorGreen.setOnClickListener(v -> {
            selectedColor = "#008000"; // Green
            Toast.makeText(CreateNewNote.this, "Selected Green", Toast.LENGTH_SHORT).show();
        });

        // Setup the FloatingActionButtons
        FloatingActionButton returnHome = findViewById(R.id.goBack); // Button to cancel and return home
        FloatingActionButton saveNote = findViewById(R.id.saveNote); // Button to save note and return to home

        // Set an OnClickListener to respond to return home button clicks
        returnHome.setOnClickListener(view -> {
            // Intent to go to home page when the return home button is clicked
            Intent intent1 = new Intent(CreateNewNote.this, MainActivity.class);
            startActivity(intent1);
        });

        // Set an OnClickListener to respond to save button clicks
        saveNote.setOnClickListener(view -> saveNote());

        // Initialize the ActivityResultLauncher
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        noteImageView.setImageURI(imageUri);  // Display the captured image
                    } else {
                        Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Button to choose or capture an image
        Button buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonChooseImage.setOnClickListener(v -> showImageChooserDialog());
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
                        requestCameraPermission();  // Capture from Camera
                    }
                })
                .show();
    }

    // Open the device gallery to choose an image
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Check for camera permissions and capture image if allowed
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            captureImage();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    // Capture a new image using the device's camera
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there is an app that can handle the camera intent
        PackageManager packageManager = getPackageManager();
        if (packageManager != null) {
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (activities != null && !activities.isEmpty()) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();  // Create a file to store the captured image
                } catch (IOException ex) {
                    Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                }

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.example.noteme.fileprovider", photoFile);  // Provide the file's URI
                    imageUri = photoURI;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);  // Tell the camera to save the photo to this file
                    captureImageLauncher.launch(intent);  // Launch the camera intent
                }
            } else {
                Toast.makeText(this, "No camera apps available", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // Create a file for the captured image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Handle the result of image selection or capture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData(); // Get the URI of the selected image
                noteImageView.setImageURI(imageUri); // Display the selected image
                saveImageToInternalStorage(imageUri); // Copy the image to internal storage
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                noteImageView.setImageURI(imageUri); // Display the captured image
            }
        }
    }

    // Copy image from its original location to internal storage
    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return;

            File imageFile = new File(getFilesDir(), "note_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            savedImageFile = imageFile; // Save the file for later use
        } catch (IOException e) {
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Load existing note data into the fields for editing
    private void loadNoteForEditing(int id) {
        Cursor cursor = dbHelper.getNoteById(id); // Fetch note from database
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

        // Return to the main screen
        Intent intent = new Intent(CreateNewNote.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureImage(); // Permission granted, capture image
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
