package com.coetusstudio.academicportal.Activity.Notes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.coetusstudio.academicportal.Model.Notes;
import com.coetusstudio.academicportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class UploadNotesActivity extends AppCompatActivity {

    private CardView addPdf;
    private EditText pdfTitle;
    private TextView pdfTextView;
    private Button btnUploadPdf;
    private Uri pdfUri;
    private String pdfName = "";
    private ProgressDialog pd;

    private DatabaseReference reference, facultyRef;
    private String facultyDept = "";
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notes);

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Notes");
        facultyRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data");
        
        pd = new ProgressDialog(this);

        addPdf = findViewById(R.id.addPdf);
        pdfTitle = findViewById(R.id.pdfTitle);
        pdfTextView = findViewById(R.id.pdfTextView);
        btnUploadPdf = findViewById(R.id.btnUploadPdf);

        getFacultyDetails();

        addPdf.setOnClickListener(v -> openGallery());

        btnUploadPdf.setOnClickListener(v -> {
            if (pdfTitle.getText().toString().isEmpty()) {
                pdfTitle.setError("Required");
                pdfTitle.requestFocus();
            } else if (pdfUri == null) {
                Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
            } else {
                uploadToCloudinary(pdfUri);
            }
        });
    }

    private void uploadToCloudinary(Uri uri) {
        pd.setMessage("Uploading PDF...");
        pd.setCancelable(false);
        pd.show();

        MediaManager.get().upload(uri)
                .option("unsigned", true)
                .option("upload_preset", "academic_notice")
                .option("resource_type", "raw") // FORCED to raw for PDFs
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (100.0 * bytes) / totalBytes;
                        pd.setMessage("Uploading: " + (int)progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String downloadUrl = (String) resultData.get("secure_url");
                        uploadData(downloadUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        pd.dismiss();
                        Toast.makeText(UploadNotesActivity.this, "Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void getFacultyDetails() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        facultyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        facultyDept = dept.getKey();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            if (pdfUri != null && pdfUri.toString().startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(pdfUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        pdfName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
            }
            pdfTextView.setText(pdfName);
        }
    }

    private void uploadData(String downloadUrl) {
        String title = pdfTitle.getText().toString();
        Notes notesData = new Notes(title, downloadUrl);
        
        String finalDept = (facultyDept == null || facultyDept.isEmpty()) ? "General" : facultyDept;

        reference.child(finalDept).push().setValue(notesData).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(UploadNotesActivity.this, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(UploadNotesActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
        });
    }
}
