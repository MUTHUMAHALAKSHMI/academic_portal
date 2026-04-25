package com.coetusstudio.academicportal.Activity.Students;

import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.Adapter.StudentAdapter;
import com.coetusstudio.academicportal.Model.StudentDetails;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class ViewStudentDataActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private String semester, section;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_student_data);

        semester = getIntent().getStringExtra("semester");
        section = getIntent().getStringExtra("section");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<StudentDetails> options =
                new FirebaseRecyclerOptions.Builder<StudentDetails>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").child(section).orderByChild("studentSemester").equalTo(semester), StudentDetails.class)
                        .build();

        adapter = new StudentAdapter(options);
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                processSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processSearch(newText);
                return false;
            }
        });
    }

    private void processSearch(String s) {
        FirebaseRecyclerOptions<StudentDetails> options =
                new FirebaseRecyclerOptions.Builder<StudentDetails>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").child(section).orderByChild("studentAdmissionNumber").startAt(s).endAt(s + "\uf8ff"), StudentDetails.class)
                        .build();

        adapter = new StudentAdapter(options);
        adapter.startListening();
        recyclerView.setAdapter(adapter);
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