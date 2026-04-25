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
import com.coetusstudio.academicportal.Activity.Attendance.ManageAttendanceActivity;
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

public class HODMainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    String userRole, facultyName, facultyImage;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    MaterialCardView addFaculty, updateFaculty, studentDetails, sendNotification, resolveQueries, attendance, administrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod_main);

        auth = FirebaseAuth.getInstance();
        userRole = getIntent().getStringExtra("userRole");

        drawerLayout = findViewById(R.id.drawer_layout_hod);
        navigationView = findViewById(R.id.nav_view_hod);
        toolbar = findViewById(R.id.toolbar_hod);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                // Updated Title from IIMTU HODs And Deans to SMTEC HOD
                getSupportActionBar().setTitle("SMTEC HOD");
            }
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Initialize Cards with IDs from content_hod_main.xml
        addFaculty = findViewById(R.id.cardAddFaculty);
        updateFaculty = findViewById(R.id.cardUpdateFaculty);
        studentDetails = findViewById(R.id.cardStudentDetailsHOD);
        sendNotification = findViewById(R.id.cardSendNotificationHOD);
        resolveQueries = findViewById(R.id.cardResolveQueriesHOD);
        attendance = findViewById(R.id.cardAttendanceHOD);
        administrator = findViewById(R.id.cardAdministrator);

        // Click Listeners
        if (addFaculty != null) addFaculty.setOnClickListener(v -> startActivity(new Intent(this, AddFacultyActivity.class)));
        if (updateFaculty != null) updateFaculty.setOnClickListener(v -> startActivity(new Intent(this, ManageFacultyDataActivity.class)));

        if (studentDetails != null) studentDetails.setOnClickListener(v -> startActivity(new Intent(this, ManageStudentDataActivity.class)));
        
        if (sendNotification != null) sendNotification.setOnClickListener(v -> startActivity(new Intent(this, UploadNoticeActivity.class)));
        if (resolveQueries != null) resolveQueries.setOnClickListener(v -> startActivity(new Intent(this, ResolveQueriesActivity.class)));

        if (attendance != null) attendance.setOnClickListener(v -> startActivity(new Intent(this, ManageAttendanceActivity.class)));
        
        if (administrator != null) administrator.setOnClickListener(v -> startActivity(new Intent(this, AdministratorActivity.class)));
        
        updateHeader();

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.logout) {
                    auth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else if (id == R.id.profile) {
                    startActivity(new Intent(this, HODProfileActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });

            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                headerView.setOnClickListener(v -> {
                    startActivity(new Intent(HODMainActivity.this, HODProfileActivity.class));
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

        // For HODs, we look in "Hod Data"
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Hod Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        DataSnapshot user = dept.child(uid);
                        String email = user.child("hodEmail").getValue(String.class);
                        
                        Object nameVal = user.child("hodName").getValue();
                        String name = (nameVal != null) ? String.valueOf(nameVal) : "Rajasubramaniyan";
                        
                        if (navName != null) navName.setText(name);
                        if (navEmail != null) navEmail.setText(email);
                        
                        String img = user.child("hodImage").getValue(String.class);
                        if (navImage != null && img != null) {
                            Glide.with(getApplicationContext()).load(img).placeholder(R.drawable.manimg).into(navImage);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
