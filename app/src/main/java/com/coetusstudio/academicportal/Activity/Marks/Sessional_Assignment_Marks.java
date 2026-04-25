package com.coetusstudio.academicportal.Activity.Marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.coetusstudio.academicportal.Adapter.SessionalMarksAdapter;
import com.coetusstudio.academicportal.Model.SessionalMarks;
import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Sessional_Assignment_Marks extends AppCompatActivity {

    RecyclerView recviewSessional;
    SessionalMarksAdapter sessionalMarksAdapter;
    String studentRollNumber, studentSection, studentName;
    ArrayList<SessionalMarks> list;
    ProgressDialog progressDialog;
    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessional_assignment_marks);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please! wait");
        progressDialog.setMessage("Loading...");

        progressDialog.show();
        try {
            Intent intent = getIntent();
            studentSection = intent.getStringExtra("section");
            studentRollNumber = intent.getStringExtra("rollNumber");
            studentName = intent.getStringExtra("name");

            progressDialog.dismiss();

        }catch (Exception e){
            e.printStackTrace();
        }

        
        list = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Sessional_Assignment_Marks.this);
        recviewSessional = (RecyclerView) findViewById(R.id.rcSessionalMarks);
        recviewSessional.setLayoutManager(linearLayoutManager);

        sessionalMarksAdapter = new SessionalMarksAdapter(this, list, studentName);
        recviewSessional.setAdapter(sessionalMarksAdapter);

        if (studentSection != null && studentRollNumber != null) {
            FirebaseDatabase.getInstance(DB_URL).getReference().child("SessionalMarks").child(studentSection).child(studentRollNumber).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        SessionalMarks sessionalMarks = dataSnapshot.getValue(SessionalMarks.class);
                        list.add(sessionalMarks);
                    }
                    sessionalMarksAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
