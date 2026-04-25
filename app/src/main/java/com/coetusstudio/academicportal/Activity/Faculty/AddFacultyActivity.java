package com.coetusstudio.academicportal.Activity.Faculty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.coetusstudio.academicportal.Model.AddFaculty;
import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFacultyActivity extends AppCompatActivity {

    private CircleImageView addFacultyImage;
    private EditText addFacultyName, addFacultyEmail, addFacultyId, addFacultyPassword;
    private Spinner spinnerFacultySubject, spinnerFacultySubjectCode, spinnerFacultyBranch, spinnerFacultySemester;
    private Button btnAddFaculty;
    private Uri imageUri;
    private ProgressDialog pd;
    private String downloadUrl = "";

    private DatabaseReference reference;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_faculty);

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data");
        pd = new ProgressDialog(this);

        initViews();
        setupSpinners();

        addFacultyImage.setOnClickListener(v -> openGallery());
        btnAddFaculty.setOnClickListener(v -> validateData());
    }

    private void initViews() {
        addFacultyImage = findViewById(R.id.addFacultyImage);
        addFacultyName = findViewById(R.id.addFacultyName);
        addFacultyEmail = findViewById(R.id.addFacultyEmail);
        addFacultyId = findViewById(R.id.addFacultyId);
        addFacultyPassword = findViewById(R.id.addFacultyPassword);

        spinnerFacultySubject = findViewById(R.id.spinnerFacultySubject);
        spinnerFacultySubjectCode = findViewById(R.id.spinnerFacultySubjectCode);
        spinnerFacultyBranch = findViewById(R.id.spinnerFacultyBranch);
        spinnerFacultySemester = findViewById(R.id.spinnerFacultySemester);

        btnAddFaculty = findViewById(R.id.btnAddFaculty);
    }

    private void setupSpinners() {
        String[] subjects = {"Select Subject", "ENGINEERING MATHEMATICS 1", "ENGINEERING MATHEMATICS 2", "ENGINEERING CHEMISTRY", "C PROGRAMING", "PYTHON PROGRAMING", "BASIC ELECTRICAL ENGINEERING"};
        spinnerFacultySubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects));

        String[] subjectCodes = {"Select Subject Code", "CS0035", "CB0036", "CCS1234", "CS3956", "CS9876", "CB7890"};
        spinnerFacultySubjectCode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectCodes));

        String[] branches = {
                "Select Branch",
                "COMPUTER SCIENCE AND ENGINEERING",
                "AI&DS ENGINEERING",
                "ELECTRONICS AND COMMUNICATION ENGINEERING",
                "ELECTRICAL AND ELECTRONICS ENGINEERING",
                "MECHANICAL ENGINEERING",
                "CIVIL ENGINEERING"
        };
        spinnerFacultyBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches));

        String[] semesters = {"Select Semester", "FIRST SEMESTER", "SECOND SEMESTER", "THIRD SEMESTER", "FOURTH SEMESTER", "FIFTH SEMESTER", "SIXTH SEMESTER", "SEVENTH SEMESTER", "EIGHT SEMESTER"};
        spinnerFacultySemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                addFacultyImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateData() {
        if (addFacultyName.getText().toString().isEmpty()) {
            addFacultyName.setError("Required");
        } else if (addFacultyEmail.getText().toString().isEmpty()) {
            addFacultyEmail.setError("Required");
        } else if (addFacultyId.getText().toString().isEmpty()) {
            addFacultyId.setError("Required");
        } else if (spinnerFacultySubject.getSelectedItem().toString().equals("Select Subject")) {
            Toast.makeText(this, "Please select subject", Toast.LENGTH_SHORT).show();
        } else if (spinnerFacultyBranch.getSelectedItem().toString().equals("Select Branch")) {
            Toast.makeText(this, "Please select branch", Toast.LENGTH_SHORT).show();
        } else if (addFacultyPassword.getText().toString().isEmpty()) {
            addFacultyPassword.setError("Required");
        } else {
            if (imageUri != null) {
                uploadImage();
            } else {
                uploadData();
            }
        }
    }

    private void uploadImage() {
        pd.setMessage("Uploading Image...");
        pd.show();

        MediaManager.get().upload(imageUri)
                .option("unsigned", true)
                .option("upload_preset", "academic_notice")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        downloadUrl = (String) resultData.get("secure_url");
                        uploadData();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        pd.dismiss();
                        Toast.makeText(AddFacultyActivity.this, "Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void uploadData() {
        pd.setMessage("Adding Faculty...");
        if (!pd.isShowing()) pd.show();

        String branch = spinnerFacultyBranch.getSelectedItem().toString();
        String name = addFacultyName.getText().toString();

        AddFaculty faculty = new AddFaculty(
                downloadUrl,
                name,
                addFacultyEmail.getText().toString(),
                addFacultyId.getText().toString(),
                spinnerFacultySubject.getSelectedItem().toString(),
                spinnerFacultySubjectCode.getSelectedItem().toString(),
                branch,
                spinnerFacultySemester.getSelectedItem().toString(),
                addFacultyPassword.getText().toString()
        );

        reference.child(branch).child(name).setValue(faculty).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(AddFacultyActivity.this, "Faculty Added Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(AddFacultyActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
        });
    }
}
