package com.coetusstudio.academicportal.Activity.Home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.Activity.Attendance.SelectSubjectAttendance;
import com.coetusstudio.academicportal.Activity.Faculty.FacultyDeatilsActivity;
import com.coetusstudio.academicportal.Activity.Lecture.LectureActivity;
import com.coetusstudio.academicportal.Activity.Students.StudentdetailsActivity;
import com.coetusstudio.academicportal.Activity.Notes.NotesActivity;
import com.coetusstudio.academicportal.Activity.Notification.NotificationActivity;
import com.coetusstudio.academicportal.Activity.Queries.QueriesActivity;
import com.coetusstudio.academicportal.R;
import com.coetusstudio.academicportal.Activity.Marks.Sessional_Assignment_Marks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.coetusstudio.academicportal.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    CardView lecture, studentDetails, attendance, receivedNotification, marks, facultyDetails, notes, queries;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private View header;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FloatingActionButton emailFeedback;
    Toolbar toolbar;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    CircleImageView studentImageProfile;
    TextView studentNameProfile, studentEmailIdProfile, studentRollNumberProfile, studentAdmissionNumberProfile;
    String confirmEmail, section, rollNumber, studentName, studentImage;
    String userRole; // To track if user is student, faculty, or hod
    
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        Intent intent = getIntent();
        confirmEmail = intent.getStringExtra("confirmEmail");
        userRole = intent.getStringExtra("userRole"); // Get role from LoginActivity

        studentNameProfile = findViewById(R.id.studentNameProfile);
        studentImageProfile = findViewById(R.id.studentImageProfile);
        studentEmailIdProfile = findViewById(R.id.studentEmailIdProfile);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        emailFeedback = findViewById(R.id.emailFeedback);

        attendance = findViewById(R.id.attendance);
        facultyDetails = findViewById(R.id.facultyDetails);
        receivedNotification = findViewById(R.id.receivedNotification);
        lecture = findViewById(R.id.lecture); // This is now "Edit Profile"
        studentDetails = findViewById(R.id.studentDetails);
        notes = findViewById(R.id.notes);
        queries = findViewById(R.id.queries);
        marks = findViewById(R.id.marks);

        attendance.setOnClickListener(this);
        facultyDetails.setOnClickListener(this);
        receivedNotification.setOnClickListener(this);
        lecture.setOnClickListener(this);
        studentDetails.setOnClickListener(this);
        notes.setOnClickListener(this);
        queries.setOnClickListener(this);
        marks.setOnClickListener(this);

        // Hide Student-only features for Faculty/HOD
        if ("faculty".equals(userRole) || "hod".equals(userRole)) {
            marks.setVisibility(View.GONE);
            studentDetails.setVisibility(View.GONE);
            attendance.setVisibility(View.GONE);
            lecture.setVisibility(View.GONE); // Hide Edit Profile for faculty/hod here
            Toast.makeText(this, "Welcome " + userRole.toUpperCase() + " Dashboard", Toast.LENGTH_LONG).show();
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);
        toggle.syncState();
        
        // Updated Title to SMTEC STUDENT
        toolbar.setTitle("SMTEC STUDENT");

        updateNavHeader(confirmEmail);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.profile) {
                    if ("student".equals(userRole)) {
                        startActivity(new Intent(MainActivity.this, StudentdetailsActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Faculty profile coming soon", Toast.LENGTH_SHORT).show();
                    }
                } else if (id == R.id.profileSendNotification) {
                    startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                } else if (id == R.id.logout) {
                    auth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.attendance) {
            startActivity(new Intent(MainActivity.this, SelectSubjectAttendance.class));
        } else if (id == R.id.receivedNotification) {
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));
        } else if (id == R.id.marks) {
            Intent intent2 = new Intent(MainActivity.this, Sessional_Assignment_Marks.class);
            intent2.putExtra("section", section);
            intent2.putExtra("rollNumber", rollNumber);
            intent2.putExtra("name", studentName);
            startActivity(intent2);
        } else if (id == R.id.lecture) { // This is "Edit Profile" Card
            Intent intent = new Intent(MainActivity.this, StudentdetailsActivity.class);
            intent.putExtra("openEditMode", true);
            startActivity(intent);
        } else if (id == R.id.studentDetails) {
            startActivity(new Intent(MainActivity.this, StudentdetailsActivity.class));
        } else if (id == R.id.notes) {
            startActivity(new Intent(MainActivity.this, NotesActivity.class));
        } else if (id == R.id.facultyDetails) {
            startActivity(new Intent(MainActivity.this, FacultyDeatilsActivity.class));
        } else if (id == R.id.queries) {
            startActivity(new Intent(MainActivity.this, QueriesActivity.class));
        }
    }

    public void updateNavHeader(String confirmEmail) {
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.studentNameProfile);
        TextView navUserMail = headerView.findViewById(R.id.studentEmailIdProfile);
        ImageView navUserPhot = headerView.findViewById(R.id.studentImageProfile);

        if (currentUser == null) return;
        String uid = currentUser.getUid();

        // Load data based on role
        String node = "student".equals(userRole) ? "Student Data" : "Faculty Data";

        FirebaseDatabase.getInstance(DB_URL).getReference().child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sectionSnap : snapshot.getChildren()) {
                    if (sectionSnap.hasChild(uid)) {
                        try {
                            DataSnapshot userSnap = sectionSnap.child(uid);
                            // Fix for possible Long to String conversion crash
                            studentName = String.valueOf(userSnap.child(userSnap.child("studentName").exists() ? "studentName" : "facultyName").getValue());
                            navUsername.setText(studentName);
                            navUserMail.setText(String.valueOf(userSnap.child(userSnap.child("studentEmail").exists() ? "studentEmail" : "facultyEmail").getValue()));
                            
                            section = String.valueOf(userSnap.child("studentSection").getValue());
                            rollNumber = String.valueOf(userSnap.child("studentRollNumber").getValue());
                            studentImage = String.valueOf(userSnap.child(userSnap.child("studentImage").exists() ? "studentImage" : "facultyImage").getValue());
                            
                            if (studentImage != null && !studentImage.equals("null")) {
                                Glide.with(getApplicationContext()).load(studentImage).error(R.drawable.manimg).into(navUserPhot);
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
