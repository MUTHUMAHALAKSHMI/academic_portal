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
import com.coetusstudio.academicportal.Model.HODData;
import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddHODActivity extends AppCompatActivity {

    private CircleImageView addHODImage;
    private EditText addHODName, addHODEmail, addHODPhone, addHODId, addHODPassword;
    private Spinner spinnerHODBranch;
    private Button btnAddHOD;
    private Uri imageUri;
    private ProgressDialog pd;
    private String downloadUrl = "";

    private DatabaseReference reference;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hod);

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("HOD Data");
        pd = new ProgressDialog(this);

        initViews();
        setupSpinner();

        addHODImage.setOnClickListener(v -> openGallery());
        btnAddHOD.setOnClickListener(v -> validateData());
    }

    private void initViews() {
        addHODImage = findViewById(R.id.addHODImage);
        addHODName = findViewById(R.id.addHODName);
        addHODEmail = findViewById(R.id.addHODEmail);
        addHODPhone = findViewById(R.id.addHODPhone);
        addHODId = findViewById(R.id.addHODId);
        addHODPassword = findViewById(R.id.addHODPassword);
        spinnerHODBranch = findViewById(R.id.spinnerHODBranch);
        btnAddHOD = findViewById(R.id.btnAddHOD);
    }

    private void setupSpinner() {
        String[] branches = {
                "Select Branch",
                "COMPUTER SCIENCE AND ENGINEERING",
                "AI&DS ENGINEERING",
                "ELECTRONICS AND COMMUNICATION ENGINEERING",
                "ELECTRICAL AND ELECTRONICS ENGINEERING",
                "MECHANICAL ENGINEERING",
                "CIVIL ENGINEERING"
        };
        spinnerHODBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches));
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
                addHODImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateData() {
        if (addHODName.getText().toString().isEmpty()) {
            addHODName.setError("Required");
        } else if (addHODEmail.getText().toString().isEmpty()) {
            addHODEmail.setError("Required");
        } else if (addHODPhone.getText().toString().isEmpty()) {
            addHODPhone.setError("Required");
        } else if (spinnerHODBranch.getSelectedItem().toString().equals("Select Branch")) {
            Toast.makeText(this, "Please select branch", Toast.LENGTH_SHORT).show();
        } else if (addHODPassword.getText().toString().isEmpty()) {
            addHODPassword.setError("Required");
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
                        Toast.makeText(AddHODActivity.this, "Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void uploadData() {
        pd.setMessage("Adding HOD...");
        if (!pd.isShowing()) pd.show();

        String branch = spinnerHODBranch.getSelectedItem().toString();
        String name = addHODName.getText().toString();

        HODData hod = new HODData(
                downloadUrl,
                name,
                addHODEmail.getText().toString(),
                addHODId.getText().toString(),
                branch,
                addHODPhone.getText().toString(),
                addHODPassword.getText().toString()
        );

        reference.child(branch).child(name).setValue(hod).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(AddHODActivity.this, "HOD Added Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(AddHODActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
        });
    }
}
