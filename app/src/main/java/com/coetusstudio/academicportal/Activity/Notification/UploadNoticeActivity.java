package com.coetusstudio.academicportal.Activity.Notification;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.coetusstudio.academicportal.Model.NoticeData;
import com.coetusstudio.academicportal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class UploadNoticeActivity extends AppCompatActivity {

    private CardView addNoticeImage;
    private ImageView noticeImageView;
    private Spinner noticeTypeSpinner;
    private EditText noticeDescription;
    private Button btnUploadNotice, btnViewStudentNotice, btnViewFacultyNotice;
    private Uri imageUri;
    private String downloadUrl = "";
    private ProgressDialog pd;

    private DatabaseReference reference, facultyRef, hodRef;
    private String facultyDept = "", facultyImage = "";
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notice);

        reference = FirebaseDatabase.getInstance(DB_URL).getReference().child("Notice");
        facultyRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data");
        hodRef = FirebaseDatabase.getInstance(DB_URL).getReference().child("Hod Data");
        
        pd = new ProgressDialog(this);

        addNoticeImage = findViewById(R.id.addNoticeImage);
        noticeImageView = findViewById(R.id.noticeImageView);
        noticeTypeSpinner = findViewById(R.id.noticeTypeSpinner);
        noticeDescription = findViewById(R.id.noticeDescription);
        btnUploadNotice = findViewById(R.id.btnUploadNotice);
        btnViewStudentNotice = findViewById(R.id.btnViewStudentNotice);
        btnViewFacultyNotice = findViewById(R.id.btnViewFacultyNotice);

        String[] items = new String[]{"Select Notification Type*", "All Faculty And Students", "Only Faculty"};
        noticeTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));

        getUserDetails();

        addNoticeImage.setOnClickListener(v -> openGallery());

        btnUploadNotice.setOnClickListener(v -> {
            if (noticeDescription.getText().toString().isEmpty()) {
                noticeDescription.setError("Required");
                noticeDescription.requestFocus();
            } else if (noticeTypeSpinner.getSelectedItem().toString().equals("Select Notification Type*")) {
                Toast.makeText(this, "Please select notification type", Toast.LENGTH_SHORT).show();
            } else if (imageUri == null) {
                uploadData();
            } else {
                uploadToCloudinary(imageUri);
            }
        });

        btnViewStudentNotice.setOnClickListener(v -> {
            Intent intent = new Intent(UploadNoticeActivity.this, NotificationActivity.class);
            intent.putExtra("type", "All Faculty And Students");
            startActivity(intent);
        });

        btnViewFacultyNotice.setOnClickListener(v -> {
            Intent intent = new Intent(UploadNoticeActivity.this, NotificationActivity.class);
            intent.putExtra("type", "Only Faculty");
            startActivity(intent);
        });
    }

    private void uploadToCloudinary(Uri uri) {
        pd.setMessage("Uploading Image...");
        pd.setCancelable(false);
        pd.show();

        MediaManager.get().upload(uri)
                .unsigned("academic_notice")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Start");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d("Cloudinary", "Progress");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        downloadUrl = (String) resultData.get("secure_url");
                        uploadData();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        pd.dismiss();
                        Toast.makeText(UploadNoticeActivity.this, "Cloudinary Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        Log.e("Cloudinary", error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d("Cloudinary", "Reschedule");
                    }
                }).dispatch();
    }

    private void getUserDetails() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        facultyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        facultyDept = dept.getKey();
                        facultyImage = dept.child(uid).child("facultyImage").getValue(String.class);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    hodRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dept : snapshot.getChildren()) {
                                if (dept.hasChild(uid)) {
                                    facultyDept = dept.getKey();
                                    facultyImage = dept.child(uid).child("hodImage").getValue(String.class);
                                    break;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void openGallery() {
        Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                noticeImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadData() {
        if (!pd.isShowing()) {
             pd.setMessage("Posting...");
             pd.show();
        }
        
        String type = noticeTypeSpinner.getSelectedItem().toString();
        String description = noticeDescription.getText().toString();

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yy");
        String date = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        String time = currentTime.format(calForTime.getTime());

        NoticeData noticeData = new NoticeData(description, downloadUrl, date, time, facultyImage);
        
        String finalDept = (facultyDept == null || facultyDept.isEmpty()) ? "General" : facultyDept;

        reference.child(finalDept).child(type).push().setValue(noticeData).addOnSuccessListener(unused -> {
            pd.dismiss();
            Toast.makeText(UploadNoticeActivity.this, "Notice Uploaded Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(UploadNoticeActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
        });
    }
}
