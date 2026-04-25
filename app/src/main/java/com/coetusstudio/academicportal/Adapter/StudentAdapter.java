package com.coetusstudio.academicportal.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.Activity.Students.UpdateStudentDataActivity;
import com.coetusstudio.academicportal.Model.StudentDetails;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentAdapter extends FirebaseRecyclerAdapter<StudentDetails, StudentAdapter.myViewHolder> {

    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public StudentAdapter(@NonNull FirebaseRecyclerOptions<StudentDetails> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull StudentDetails model) {
        holder.name.setText("Name: " + model.getStudentName());
        holder.email.setText("Email Id: " + model.getStudentEmail());
        holder.roll.setText("Roll No.: " + model.getStudentRollNumber());
        holder.admission.setText("Admission No.: " + model.getStudentAdmissionNumber());
        holder.enrollment.setText("Enrollment No.: " + model.getStudentEnrollmentNumber());
        holder.branch.setText("Branch: " + model.getStudentBranch());
        holder.semester.setText("Semester: " + model.getStudentSemester());
        holder.section.setText("Section: " + model.getStudentSection());
        holder.grade.setText("Grade: " + model.getStudentGrade());

        Glide.with(holder.img.getContext())
                .load(model.getStudentImage())
                .placeholder(R.drawable.manimg)
                .error(R.drawable.manimg)
                .into(holder.img);

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.name.getContext(), UpdateStudentDataActivity.class);
            intent.putExtra("studentSection", model.getStudentSection());
            intent.putExtra("studentRollNumber", model.getStudentRollNumber());
            holder.name.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.name.getContext());
            builder.setTitle("Warning");
            builder.setMessage("Are you sure want to delete Student Data...?");
            builder.setPositiveButton("YES", (dialog, which) -> {
                FirebaseDatabase.getInstance(DB_URL).getReference().child("Student Data")
                        .child(model.getStudentSection()).child(model.getStudentRollNumber()).removeValue();
                Toast.makeText(holder.name.getContext(), "Student Deleted Successfully", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_student_item, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        CircleImageView img;
        TextView name, email, roll, admission, enrollment, branch, semester, section, grade;
        ImageView btnEdit, btnDelete;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.studentImage);
            name = itemView.findViewById(R.id.studentName);
            email = itemView.findViewById(R.id.studentEmail);
            roll = itemView.findViewById(R.id.studentRoll);
            admission = itemView.findViewById(R.id.studentAdmission);
            enrollment = itemView.findViewById(R.id.studentEnrollment);
            branch = itemView.findViewById(R.id.studentBranch);
            semester = itemView.findViewById(R.id.studentSemester);
            section = itemView.findViewById(R.id.studentSection);
            grade = itemView.findViewById(R.id.studentGrade);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}