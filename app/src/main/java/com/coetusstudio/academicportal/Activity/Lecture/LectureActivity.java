package com.coetusstudio.academicportal.Activity.Lecture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.coetusstudio.academicportal.Adapter.LectureAdapter;
import com.coetusstudio.academicportal.Model.Lecture;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LectureActivity extends AppCompatActivity {

    RecyclerView recviewLecture;
    LectureAdapter lectureAdapter;
    String studentSection;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);

        setTitle("Enter Lecture");

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

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LectureActivity.this);
                recviewLecture = findViewById(R.id.rcLecture);
                recviewLecture.setLayoutManager(linearLayoutManager);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);

                FirebaseRecyclerOptions<Lecture> options =
                        new FirebaseRecyclerOptions.Builder<Lecture>()
                                .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Lecture").child(studentSection), Lecture.class)
                                .build();

                lectureAdapter = new LectureAdapter(options);
                recviewLecture.setAdapter(lectureAdapter);
                lectureAdapter.startListening();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LectureActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (lectureAdapter != null) {
            lectureAdapter.stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchmenu, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void processsearch(String s) {
        if (studentSection == null) return;
        FirebaseRecyclerOptions<Lecture> options =
                new FirebaseRecyclerOptions.Builder<Lecture>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Lecture").child(studentSection).orderByChild("lectureName").startAt(s).endAt(s + "\uf8ff"), Lecture.class)
                        .build();

        lectureAdapter = new LectureAdapter(options);
        lectureAdapter.startListening();
        recviewLecture.setAdapter(lectureAdapter);
    }
}
