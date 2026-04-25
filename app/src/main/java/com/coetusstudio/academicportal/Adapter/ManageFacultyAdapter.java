package com.coetusstudio.academicportal.Adapter;

import android.app.AlertDialog;
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
import com.coetusstudio.academicportal.Activity.Faculty.UpdateFacultyActivity;
import com.coetusstudio.academicportal.Model.AddFaculty;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class ManageFacultyAdapter extends FirebaseRecyclerAdapter<AddFaculty, ManageFacultyAdapter.myviewholder> {

    private final String branch;

    public ManageFacultyAdapter(@NonNull FirebaseRecyclerOptions<AddFaculty> options, String branch) {
        super(options);
        this.branch = branch;
    }

    @Override
    protected void onBindViewHolder(@NonNull myviewholder holder, int position, @NonNull AddFaculty model) {
        holder.name.setText(model.getFacultyName());
        holder.email.setText(model.getFacultyEmail());
        holder.id.setText(model.getFacultyId());
        holder.subject.setText(model.getFacultySubject());
        holder.department.setText(model.getFacultyBranch());

        Glide.with(holder.img.getContext())
                .load(model.getFacultyImage())
                .placeholder(R.drawable.manimg)
                .error(R.drawable.manimg)
                .into(holder.img);

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.img.getContext(), UpdateFacultyActivity.class);
            intent.putExtra("facultyName", model.getFacultyName());
            intent.putExtra("facultyEmail", model.getFacultyEmail());
            intent.putExtra("facultyId", model.getFacultyId());
            intent.putExtra("facultySubject", model.getFacultySubject());
            intent.putExtra("facultySubjectCode", model.getFacultySubjectCode());
            intent.putExtra("facultyBranch", model.getFacultyBranch());
            intent.putExtra("facultySemester", model.getFacultySemester());
            intent.putExtra("facultySection", model.getFacultySection());
            intent.putExtra("facultyPassword", model.getFacultyPassword());
            intent.putExtra("facultyImage", model.getFacultyImage());
            holder.img.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.img.getContext());
            builder.setTitle("Are you Sure?");
            builder.setMessage("Deleted data can't be Undo.");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                FirebaseDatabase.getInstance("https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference().child("Faculty Data")
                        .child(branch).child(model.getFacultyName()).removeValue();
                Toast.makeText(holder.img.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.show();
        });
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_faculty_single_row, parent, false);
        return new myviewholder(view);
    }

    public static class myviewholder extends RecyclerView.ViewHolder {
        CircleImageView img;
        TextView name, email, id, subject, department;
        ImageView btnEdit, btnDelete;

        public myviewholder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.facultyRcImage);
            name = itemView.findViewById(R.id.facultyRcName);
            email = itemView.findViewById(R.id.facultyRcEmail);
            id = itemView.findViewById(R.id.facultyRcId);
            subject = itemView.findViewById(R.id.facultyRcSubjectName);
            department = itemView.findViewById(R.id.facultyRcDepartment);
            btnEdit = itemView.findViewById(R.id.btnEditFaculty);
            btnDelete = itemView.findViewById(R.id.btnDeleteFaculty);
        }
    }
}
