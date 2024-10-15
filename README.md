# NoteMe - Notes Application

This simple Android application allows users to create, edit, and manage notes. Users can add text, set colors, and attach images to their notes. Images can be uploaded from the gallery or captured via the camera. Notes can be saved, edited, deleted, and searched using the built-in search bar functionality.

![z1](https://github.com/user-attachments/assets/20d03348-bf4e-4b90-b802-70e7ca37fe1f)
![z2](https://github.com/user-attachments/assets/83e29de1-bdfa-4cc5-a196-e3be183f0b06)

## Features

- Create new notes with a title, subtitle, description, and color selection. If a title is not inputted, the user will be prompted to add a title and the note will nto save.
- Attach images to notes from the gallery or by capturing a new one with the camera.
- Edit existing notes, including updating the image.
- Delete notes by long-pressing on a note in the main list.
- Search through notes by title using the search bar.
- Persistently store all notes in a local SQLite database.

## Usage

### Creating a Note
1. Click on the **+** button on the main screen to create a new note.
2. Enter the **title**, **subtitle**, and **description** for your note.
3. Select a **color** for the note by tapping one of the color buttons (**Yellow**, **Orange**, or **Green**).
4. Optionally, add an **image** by:
   - **Uploading from Gallery**: Click the "Choose Image" button, select "Upload from Gallery," and choose an image.
   - **Capturing with Camera**: Click the "Choose Image" button, select "Capture from Camera," and take a new photo.
5. Click the **Save** button to save your note.

### Editing a Note
1. Tap on any existing note from the main list to open it for editing.
2. Update the **title**, **subtitle**, **description**, **color**, or **image**.
3. Click the **Save** button to save the changes.

### Deleting a Note
1. Long-press on any note in the main list.
2. A confirmation dialog will appear, asking if you want to delete the note.
3. Confirm the deletion by clicking **Yes**.

### Search Notes
1. Use the **search bar** at the top of the main screen to search for notes by title.
2. The list of notes will dynamically filter based on your search query.

### Attach Image to Note
1. Click the **Choose Image** button when creating or editing a note.
2. Select whether to upload from the **gallery** or capture a new image using the **camera**.
3. The selected or captured image will be displayed in the note.
