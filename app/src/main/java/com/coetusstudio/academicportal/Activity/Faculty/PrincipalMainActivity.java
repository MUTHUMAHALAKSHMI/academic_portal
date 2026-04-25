package com.coetusstudio.academicportal.Activity.Faculty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.Activity.Attendance.ManageAttendanceActivity;
import com.coetusstudio.academicportal.Activity.Home.LoginActivity;
import com.coetusstudio.academicportal.Activity.Notification.UploadNoticeActivity;
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

public class PrincipalMainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    MaterialCardView cardDashboard, cardFacultyManagement, cardStudentManagement, cardAttendanceReports, cardAdminPortal, cardNotices, cardHODManagement, cardSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_main);

        auth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout_principal);
        navigationView = findViewById(R.id.nav_view_principal);
        toolbar = findViewById(R.id.toolbar_principal);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                // Updated Title to SMTEC PRINCIPAL
                getSupportActionBar().setTitle("SMTEC PRINCIPAL");
            }
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        cardDashboard = findViewById(R.id.cardDashboard);
        cardFacultyManagement = findViewById(R.id.cardFacultyManagement);
        cardHODManagement = findViewById(R.id.cardHODManagement);
        cardStudentManagement = findViewById(R.id.cardStudentManagement);
        cardAttendanceReports = findViewById(R.id.cardAttendanceReports);
        cardAdminPortal = findViewById(R.id.cardAdminPortal);
        cardNotices = findViewById(R.id.cardNotices);
        cardSettings = findViewById(R.id.cardSettings);

        if (cardDashboard != null) cardDashboard.setOnClickListener(v -> startActivity(new Intent(this, PrincipalDashboardActivity.class)));
        if (cardFacultyManagement != null) cardFacultyManagement.setOnClickListener(v -> startActivity(new Intent(this, ManageFacultyDataActivity.class)));
        if (cardHODManagement != null) cardHODManagement.setOnClickListener(v -> startActivity(new Intent(this, HodManagementActivity.class)));
        if (cardStudentManagement != null) cardStudentManagement.setOnClickListener(v -> startActivity(new Intent(this, ManageStudentDataActivity.class)));
        if (cardAttendanceReports != null) cardAttendanceReports.setOnClickListener(v -> startActivity(new Intent(this, ManageAttendanceActivity.class)));
        if (cardAdminPortal != null) cardAdminPortal.setOnClickListener(v -> startActivity(new Intent(this, AdministratorActivity.class)));
        if (cardNotices != null) cardNotices.setOnClickListener(v -> startActivity(new Intent(this, UploadNoticeActivity.class)));
        if (cardSettings != null) cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        updateHeader();

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.logout) {
                    auth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
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

        FirebaseDatabase.getInstance(DB_URL).getReference().child("principal and Admin Data").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                if (user.exists()) {
                    Object nameVal = user.child("principle name").getValue();
                    String name = (nameVal != null) ? String.valueOf(nameVal) : "Rajasubramaniyan";
                    String email = auth.getCurrentUser().getEmail();
                    
                    if (navName != null) navName.setText(name);
                    if (navEmail != null) navEmail.setText(email);
                    
                    String img = user.child("image").getValue(String.class);
                    if (navImage != null && img != null) {
                        Glide.with(getApplicationContext()).load(img).placeholder(R.drawable.manimg).into(navImage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
