package com.coetusstudio.academicportal.Activity.Faculty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.Activity.Home.LoginActivity;
import com.coetusstudio.academicportal.Activity.Notification.NotificationActivity;
import com.coetusstudio.academicportal.Activity.Notification.UploadNoticeActivity;
import com.coetusstudio.academicportal.Activity.Lecture.LectureActivity;
import com.coetusstudio.academicportal.Activity.Queries.QueriesActivity;
import com.coetusstudio.academicportal.Activity.Queries.ResolveQueriesActivity;
import com.coetusstudio.academicportal.Activity.Notes.NotesActivity;
import com.coetusstudio.academicportal.Activity.Notes.UploadNotesActivity;
import com.coetusstudio.academicportal.Activity.Marks.UploadMarksActivity;
import com.coetusstudio.academicportal.Activity.Attendance.SelectSubjectAttendance;
import com.coetusstudio.academicportal.Activity.Attendance.ManageAttendanceActivity;
import com.coetusstudio.academicportal.Activity.Students.AddStudentActivity;
import com.coetusstudio.academicportal.Activity.Students.ManageStudentDataActivity;
import com.coetusstudio.academicportal.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FacultyMainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    String userRole, facultyName, facultyImage, facultyDept;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    MaterialCardView studentDetails, addNewStudent, attendance, sendNotification, uploadMarks, queries, uploadNotes, manageNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_main);

        auth = FirebaseAuth.getInstance();
        userRole = getIntent().getStringExtra("userRole");

        drawerLayout = findViewById(R.id.drawer_layout_faculty);
        navigationView = findViewById(R.id.nav_view_faculty);
        toolbar = findViewById(R.id.toolbar_faculty);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                // Updated Title to SMTEC FACULTY
                getSupportActionBar().setTitle("SMTEC FACULTY");
            }
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Initialize Cards
        studentDetails = findViewById(R.id.card1);
        addNewStudent = findViewById(R.id.card2);
        attendance = findViewById(R.id.card3);
        sendNotification = findViewById(R.id.card4);
        uploadMarks = findViewById(R.id.card5);
        queries = findViewById(R.id.card6);
        uploadNotes = findViewById(R.id.card7);
        manageNotes = findViewById(R.id.card8);

        // Click Listeners
        if (studentDetails != null) studentDetails.setOnClickListener(v -> startActivity(new Intent(this, ManageStudentDataActivity.class)));

        if (attendance != null) attendance.setOnClickListener(v -> startActivity(new Intent(this, ManageAttendanceActivity.class)));
        
        // Updated: Launches AddStudentActivity
        if (addNewStudent != null) addNewStudent.setOnClickListener(v -> startActivity(new Intent(this, AddStudentActivity.class)));

        if (sendNotification != null) sendNotification.setOnClickListener(v -> startActivity(new Intent(this, UploadNoticeActivity.class)));
        
        if (uploadMarks != null) uploadMarks.setOnClickListener(v -> startActivity(new Intent(this, UploadMarksActivity.class)));

        if (queries != null) queries.setOnClickListener(v -> startActivity(new Intent(this, ResolveQueriesActivity.class)));
        
        if (uploadNotes != null) uploadNotes.setOnClickListener(v -> startActivity(new Intent(this, UploadNotesActivity.class)));
        
        if (manageNotes != null) manageNotes.setOnClickListener(v -> {
            if (facultyDept != null) {
                Intent intent = new Intent(this, NotesActivity.class);
                intent.putExtra("section", facultyDept);
                intent.putExtra("canDelete", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Fetching details, please wait...", Toast.LENGTH_SHORT).show();
            }
        });

        updateHeader();

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.logout) {
                    auth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else if (id == R.id.profile) {
                    startActivity(new Intent(this, FacultyProfileActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });

            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                headerView.setOnClickListener(v -> {
                    startActivity(new Intent(FacultyMainActivity.this, FacultyProfileActivity.class));
                    drawerLayout.closeDrawer(GravityCompat.START);
                });
            }
        }
    }

    private void updateHeader() {
        if (navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;
        
        TextView navName = headerView.findViewById(R.id.studentNameProfile);
        TextView navEmail = headerView.findViewById(R.id.studentEmailIdProfile);
        CircleImageView navImage = headerView.findViewById(R.id.studentImageProfile);

        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        try {
                            facultyDept = dept.getKey();
                            DataSnapshot user = dept.child(uid);
                            Object nameVal = user.child("facultyName").getValue();
                            facultyName = (nameVal != null) ? String.valueOf(nameVal) : "Jenisha..A";
                            facultyImage = String.valueOf(user.child("facultyImage").getValue());
                            
                            if (navName != null) navName.setText(facultyName);
                            if (navEmail != null) navEmail.setText(String.valueOf(user.child("facultyEmail").getValue()));
                            
                            if (navImage != null && facultyImage != null && !facultyImage.equals("null")) {
                                Glide.with(getApplicationContext()).load(facultyImage).placeholder(R.drawable.manimg).into(navImage);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
