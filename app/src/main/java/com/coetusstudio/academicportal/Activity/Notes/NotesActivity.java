package com.coetusstudio.academicportal.Activity.Notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.coetusstudio.academicportal.Adapter.NotesAdapter;
import com.coetusstudio.academicportal.Model.Notes;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotesActivity extends AppCompatActivity {


    RecyclerView recviewNotes;
    NotesAdapter notesAdapter;
    String studentSection;
    boolean canDelete = false;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        setTitle("Search PDF");

        studentSection = getIntent().getStringExtra("section");
        canDelete = getIntent().getBooleanExtra("canDelete", false);

        if (studentSection != null) {
            setupRecyclerView();
        } else {
            fetchStudentSection();
        }
    }

    private void fetchStudentSection() {
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
                
                if (studentSection == null) {
                    // Try faculty/HOD if not found in students (though they should pass section via intent)
                    fetchFacultySection(uid);
                    return;
                }

                setupRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotesActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFacultySection(String uid) {
        FirebaseDatabase.getInstance(DB_URL).getReference().child("Faculty Data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dept : snapshot.getChildren()) {
                    if (dept.hasChild(uid)) {
                        studentSection = dept.getKey();
                        setupRecyclerView();
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotesActivity.this);
        recviewNotes=(RecyclerView)findViewById(R.id.rcNotes);
        recviewNotes.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        FirebaseRecyclerOptions<Notes> options =
                new FirebaseRecyclerOptions.Builder<Notes>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Notes").child(studentSection), Notes.class)
                        .build();

        notesAdapter=new NotesAdapter(options, canDelete);
        recviewNotes.setAdapter(notesAdapter);
        notesAdapter.startListening();
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (notesAdapter != null) {
            notesAdapter.stopListening();
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
        FirebaseRecyclerOptions<Notes> options =
                new FirebaseRecyclerOptions.Builder<Notes>()
                        .setQuery(FirebaseDatabase.getInstance(DB_URL).getReference().child("Notes").child(studentSection).orderByChild("filename").startAt(s).endAt(s+"\uf8ff"), Notes.class)
                        .build();

        notesAdapter=new NotesAdapter(options, canDelete);
        notesAdapter.startListening();
        recviewNotes.setAdapter(notesAdapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
