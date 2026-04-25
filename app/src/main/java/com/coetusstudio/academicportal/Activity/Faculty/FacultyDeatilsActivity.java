package com.coetusstudio.academicportal.Activity.Faculty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.coetusstudio.academicportal.Adapter.FacultyAdapter;
import com.coetusstudio.academicportal.Model.AddFaculty;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FacultyDeatilsActivity extends AppCompatActivity {

    RecyclerView recviewFaculty;
    FacultyAdapter facultyAdapter;
    String studentSection;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_deatils);
        setTitle("Enter Faculty ID");

        FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                    if (sectionSnapshot.hasChild(uid)) {
                        try {
                            studentSection = sectionSnapshot.child(uid).child("studentSection").getValue(String.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                if (studentSection == null) return;

                recviewFaculty=(RecyclerView)findViewById(R.id.rcFactulty);
                recviewFaculty.setLayoutManager(new LinearLayoutManager(FacultyDeatilsActivity.this));

                FirebaseRecyclerOptions<AddFaculty> options =
                        new FirebaseRecyclerOptions.Builder<AddFaculty>()
                                .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").child(studentSection), AddFaculty.class)
                                .build();

                facultyAdapter=new FacultyAdapter(options);
                recviewFaculty.setAdapter(facultyAdapter);
                facultyAdapter.startListening();

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyDeatilsActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onStop() {
        super.onStop();
        if (facultyAdapter != null) {
            facultyAdapter.stopListening();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.searchmenu,menu);

        MenuItem item=menu.findItem(R.id.search);

        SearchView searchView=(SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String s) {

                processsearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                processsearch(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void processsearch(String s)
    {
        if (studentSection == null) return;
        FirebaseRecyclerOptions<AddFaculty> options =
                new FirebaseRecyclerOptions.Builder<AddFaculty>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").child(studentSection).orderByChild("facultyId").startAt(s).endAt(s+"\uf8ff"), AddFaculty.class)
                        .build();

        facultyAdapter=new FacultyAdapter(options);
        facultyAdapter.startListening();
        recviewFaculty.setAdapter(facultyAdapter);


    }
}