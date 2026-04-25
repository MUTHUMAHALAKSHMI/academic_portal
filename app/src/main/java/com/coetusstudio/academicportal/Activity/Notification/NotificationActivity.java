package com.coetusstudio.academicportal.Activity.Notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.coetusstudio.academicportal.Adapter.NoticeAdapter;
import com.coetusstudio.academicportal.Model.NoticeData;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationActivity extends AppCompatActivity {


    RecyclerView recviewNotice;
    NoticeAdapter noticeAdapter;
    String studentSection, type;
    TextView title;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        title = findViewById(R.id.notification_title);
        type = getIntent().getStringExtra("type");

        if (type != null) {
            title.setText(type);
        }

        FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                studentSection = null;

                // Check if user is a student
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    if (dsp.hasChild(uid)) {
                        studentSection = dsp.child(uid).child("studentSection").getValue(String.class);
                        break;
                    }
                }

                // If not found in students, it might be a faculty or HOD
                if (studentSection == null) {
                     loadFacultyOrHODNotices();
                } else {
                     setupRecyclerView(studentSection, type != null ? type : "All Faculty And Students");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFacultyOrHODNotices() {
        String uid = FirebaseAuth.getInstance().getUid();
        // First check Faculty Data
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        setupRecyclerView(dept.getKey(), type != null ? type : "Only Faculty");
                        found = true;
                        break;
                    }
                }
                
                // If not found in Faculty, check Hod Data
                if (!found) {
                    FirebaseDatabase.getInstance(DB_URL).getReference().child("Hod Data").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dept : snapshot.getChildren()) {
                                if (dept.hasChild(uid)) {
                                    setupRecyclerView(dept.getKey(), type != null ? type : "Only Faculty");
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

    private void setupRecyclerView(String section, String noticeType) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationActivity.this);
        recviewNotice = findViewById(R.id.rcNotice);
        recviewNotice.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        FirebaseRecyclerOptions<NoticeData> options =
                new FirebaseRecyclerOptions.Builder<NoticeData>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Notice").child(section).child(noticeType), NoticeData.class)
                        .build();

        noticeAdapter = new NoticeAdapter(options);
        recviewNotice.setAdapter(noticeAdapter);
        noticeAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noticeAdapter != null) {
            noticeAdapter.stopListening();
        }
    }
}
