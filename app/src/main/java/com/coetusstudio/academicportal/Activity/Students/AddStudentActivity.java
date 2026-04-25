package com.coetusstudio.academicportal.Activity.Students;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.coetusstudio.academicportal.Model.StudentDetails;
import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddStudentActivity extends AppCompatActivity {

    private CircleImageView addStudentImage;
    private EditText addStudentName, addStudentEmail, addStudentAdmission, addStudentRoll, addStudentEnrollment, addStudentGrade, addStudentPassword;
    private Spinner spinnerStudentBranch, spinnerStudentSemester, spinnerStudentSection;
    private Button btnAddStudent;
    private Uri imageUri;
    private ProgressDialog pd;
    private String downloadUrl = "";

    private DatabaseReference reference;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data");
        pd = new ProgressDialog(this);

        initViews();
        setupSpinners();

        addStudentImage.setOnClickListener(v -> openGallery());
        btnAddStudent.setOnClickListener(v -> validateData());
    }

    private void initViews() {
        addStudentImage = findViewById(R.id.addStudentImage);
        addStudentName = findViewById(R.id.addStudentName);
        addStudentEmail = findViewById(R.id.addStudentEmail);
        addStudentAdmission = findViewById(R.id.addStudentAdmission);
        addStudentRoll = findViewById(R.id.addStudentRoll);
        addStudentEnrollment = findViewById(R.id.addStudentEnrollment);
        addStudentGrade = findViewById(R.id.addStudentGrade);
        addStudentPassword = findViewById(R.id.addStudentPassword);

        spinnerStudentBranch = findViewById(R.id.spinnerStudentBranch);
        spinnerStudentSemester = findViewById(R.id.spinnerStudentSemester);
        spinnerStudentSection = findViewById(R.id.spinnerStudentSection);

        btnAddStudent = findViewById(R.id.btnAddStudent);
    }

    private void setupSpinners() {
        String[] branches = {
                "Select Branch",
                "COMPUTER SCIENCE AND ENGINEERING",
                "MECHANICAL ENGINEERING",
                "AI&DS ENGINEERING",
                "CIVIL ENGINEERING",
                "ELECTRONICS AND COMMUNICATION ENGINEERING",
                "ELECTRICAL AND ELECTRONICS ENGINEERING"
        };
        spinnerStudentBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches));

        String[] semesters = {"Select Semester", "FIRST SEMESTER", "SECOND SEMESTER", "THIRD SEMESTER", "FOURTH SEMESTER", "FIFTH SEMESTER", "SIXTH SEMESTER", "SEVENTH SEMESTER", "EIGHT SEMESTER"};
        spinnerStudentSemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters));

        String[] sections = {"Select Section", "A", "B", "C", "D", "E"};
        spinnerStudentSection.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sections));
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
                addStudentImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateData() {
        if (addStudentName.getText().toString().isEmpty()) {
            addStudentName.setError("Required");
        } else if (addStudentEmail.getText().toString().isEmpty()) {
            addStudentEmail.setError("Required");
        } else if (spinnerStudentBranch.getSelectedItem().toString().equals("Select Branch")) {
            Toast.makeText(this, "Please select branch", Toast.LENGTH_SHORT).show();
        } else if (spinnerStudentSection.getSelectedItem().toString().equals("Select Section")) {
            Toast.makeText(this, "Please select section", Toast.LENGTH_SHORT).show();
        } else if (addStudentPassword.getText().toString().isEmpty()) {
            addStudentPassword.setError("Required");
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
                        Toast.makeText(AddStudentActivity.this, "Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void uploadData() {
        pd.setMessage("Adding Student...");
        if (!pd.isShowing()) pd.show();

        String section = spinnerStudentSection.getSelectedItem().toString();
        String roll = addStudentRoll.getText().toString();

        StudentDetails student = new StudentDetails(
                downloadUrl,
                addStudentName.getText().toString(),
                addStudentEmail.getText().toString(),
                addStudentAdmission.getText().toString(),
                addStudentEnrollment.getText().toString(),
                roll,
                spinnerStudentBranch.getSelectedItem().toString(),
                spinnerStudentSemester.getSelectedItem().toString(),
                section,
                addStudentGrade.getText().toString(),
                "0", // Attendance
                addStudentPassword.getText().toString()
        );

        reference.child(section).child(roll).setValue(student).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(AddStudentActivity.this, "Student Added Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(AddStudentActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
        });
    }
}
