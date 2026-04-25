package com.coetusstudio.academicportal.Activity.Faculty;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.Adapter.ManageFacultyAdapter;
import com.coetusstudio.academicportal.Model.AddFaculty;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class ManageFacultyListActivity extends AppCompatActivity {

    private RecyclerView rvManageFaculty;
    private ManageFacultyAdapter adapter;
    private String branch, semester;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_faculty_list);

        branch = getIntent().getStringExtra("branch");
        semester = getIntent().getStringExtra("semester");

        rvManageFaculty = findViewById(R.id.rvManageFaculty);
        rvManageFaculty.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<AddFaculty> options =
                new FirebaseRecyclerOptions.Builder<AddFaculty>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").child(branch).orderByChild("facultySemester").equalTo(semester), AddFaculty.class)
                        .build();

        adapter = new ManageFacultyAdapter(options, branch);
        rvManageFaculty.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
