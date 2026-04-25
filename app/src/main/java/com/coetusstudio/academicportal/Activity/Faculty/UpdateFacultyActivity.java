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

import com.bumptech.glide.Glide;
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

public class UpdateFacultyActivity extends AppCompatActivity {

    private CircleImageView updateFacultyImage;
    private EditText updateFacultyName, updateFacultyEmail, updateFacultyId, updateFacultyPassword;
    private Spinner updateSpinnerFacultySubject, updateSpinnerFacultySubjectCode, updateSpinnerFacultyBranch, updateSpinnerFacultySemester;
    private Button btnUpdateFaculty;
    private Uri imageUri;
    private ProgressDialog pd;
    private String downloadUrl = "";
    private String name, email, id, subject, subjectCode, branch, semester, section, password, imageUrl;

    private DatabaseReference reference;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_faculty);

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data");
        pd = new ProgressDialog(this);

        name = getIntent().getStringExtra("facultyName");
        email = getIntent().getStringExtra("facultyEmail");
        id = getIntent().getStringExtra("facultyId");
        subject = getIntent().getStringExtra("facultySubject");
        subjectCode = getIntent().getStringExtra("facultySubjectCode");
        branch = getIntent().getStringExtra("facultyBranch");
        semester = getIntent().getStringExtra("facultySemester");
        section = getIntent().getStringExtra("facultySection");
        password = getIntent().getStringExtra("facultyPassword");
        imageUrl = getIntent().getStringExtra("facultyImage");

        initViews();
        setupSpinners();
        setData();

        updateFacultyImage.setOnClickListener(v -> openGallery());
        btnUpdateFaculty.setOnClickListener(v -> validateData());
    }

    private void initViews() {
        updateFacultyImage = findViewById(R.id.updateFacultyImage);
        updateFacultyName = findViewById(R.id.updateFacultyName);
        updateFacultyEmail = findViewById(R.id.updateFacultyEmail);
        updateFacultyId = findViewById(R.id.updateFacultyId);
        updateFacultyPassword = findViewById(R.id.updateFacultyPassword);

        updateSpinnerFacultySubject = findViewById(R.id.updateSpinnerFacultySubject);
        updateSpinnerFacultySubjectCode = findViewById(R.id.updateSpinnerFacultySubjectCode);
        updateSpinnerFacultyBranch = findViewById(R.id.updateSpinnerFacultyBranch);
        updateSpinnerFacultySemester = findViewById(R.id.updateSpinnerFacultySemester);

        btnUpdateFaculty = findViewById(R.id.btnUpdateFaculty);
    }

    private void setupSpinners() {
        String[] subjects = {"Select Subject", "ENGINEERING MATHEMATICS 1", "ENGINEERING MATHEMATICS 2", "ENGINEERING CHEMISTRY", "C PROGRAMING", "PYTHON PROGRAMING", "BASIC ELECTRICAL ENGINEERING"};
        updateSpinnerFacultySubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects));

        String[] subjectCodes = {"Select Subject Code", "CS0035", "CB0036", "CCS1234", "CS3956", "CS9876", "CB7890"};
        updateSpinnerFacultySubjectCode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectCodes));

        String[] branches = {
                "Select Branch",
                "COMPUTER SCIENCE AND ENGINEERING",
                "AI&DS ENGINEERING",
                "ELECTRONICS AND COMMUNICATION ENGINEERING",
                "ELECTRICAL AND ELECTRONICS ENGINEERING",
                "MECHANICAL ENGINEERING",
                "CIVIL ENGINEERING"
        };
        updateSpinnerFacultyBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches));

        String[] semesters = {"Select Semester", "FIRST SEMESTER", "SECOND SEMESTER", "THIRD SEMESTER", "FOURTH SEMESTER", "FIFTH SEMESTER", "SIXTH SEMESTER", "SEVENTH SEMESTER", "EIGHT SEMESTER"};
        updateSpinnerFacultySemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters));
    }

    private void setData() {
        updateFacultyName.setText(name);
        updateFacultyEmail.setText(email);
        updateFacultyId.setText(id);
        updateFacultyPassword.setText(password);
        downloadUrl = imageUrl;

        Glide.with(this).load(imageUrl).placeholder(R.drawable.manimg).into(updateFacultyImage);

        setSpinnerValue(updateSpinnerFacultySubject, subject);
        setSpinnerValue(updateSpinnerFacultySubjectCode, subjectCode);
        setSpinnerValue(updateSpinnerFacultyBranch, branch);
        setSpinnerValue(updateSpinnerFacultySemester, semester);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (value != null && adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
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
                updateFacultyImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateData() {
        if (updateFacultyName.getText().toString().isEmpty()) {
            updateFacultyName.setError("Required");
        } else if (updateFacultyEmail.getText().toString().isEmpty()) {
            updateFacultyEmail.setError("Required");
        } else if (updateFacultyId.getText().toString().isEmpty()) {
            updateFacultyId.setError("Required");
        } else if (updateSpinnerFacultySubject.getSelectedItem().toString().equals("Select Subject")) {
            Toast.makeText(this, "Please select subject", Toast.LENGTH_SHORT).show();
        } else if (updateSpinnerFacultyBranch.getSelectedItem().toString().equals("Select Branch")) {
            Toast.makeText(this, "Please select branch", Toast.LENGTH_SHORT).show();
        } else if (updateFacultyPassword.getText().toString().isEmpty()) {
            updateFacultyPassword.setError("Required");
        } else {
            if (imageUri != null) {
                uploadImage();
            } else {
                updateData();
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
                        updateData();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        pd.dismiss();
                        Toast.makeText(UpdateFacultyActivity.this, "Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void updateData() {
        pd.setMessage("Updating Faculty...");
        if (!pd.isShowing()) pd.show();

        AddFaculty faculty = new AddFaculty(
                downloadUrl,
                name,
                updateFacultyEmail.getText().toString(),
                updateFacultyId.getText().toString(),
                updateSpinnerFacultySubject.getSelectedItem().toString(),
                updateSpinnerFacultySubjectCode.getSelectedItem().toString(),
                branch,
                updateSpinnerFacultySemester.getSelectedItem().toString(),
                section,
                updateFacultyPassword.getText().toString(),
                null // Uid if needed
        );

        reference.child(branch).child(name).setValue(faculty).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(UpdateFacultyActivity.this, "Faculty Updated Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(UpdateFacultyActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
        });
    }
}
