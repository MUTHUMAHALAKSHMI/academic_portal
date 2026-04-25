package com.coetusstudio.academicportal.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.coetusstudio.academicportal.Model.Queries;
import com.coetusstudio.academicportal.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class QueriesAdapter extends FirebaseRecyclerAdapter<Queries,QueriesAdapter.myviewholder> {

    public QueriesAdapter(@NonNull FirebaseRecyclerOptions<Queries> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final QueriesAdapter.myviewholder holder, @SuppressLint("RecyclerView") final int position, @NonNull final Queries Queries)
    {
        // Use queriesName (sender name) as the primary display name
        holder.queriesFacultyName.setText(Queries.getQueriesName());
        holder.queriesStudentRollNumber.setText(Queries.getQueriesRollNumber());
        holder.queriesTitleAdapter.setText(Queries.getQueriesTitle());

        // Determine which image to show (faculty image or student image)
        String imageUrl = Queries.getFacultyImage();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = Queries.getStudentImage();
        }

        Glide.with(holder.img.getContext()).load(imageUrl)
                .placeholder(R.drawable.manimg)
                .circleCrop()
                .error(R.drawable.manimg)
                .into(holder.img);

    } // End of OnBindViewMethod

    @NonNull
    @Override
    public QueriesAdapter.myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.singlerowqueries,parent,false);
        return new QueriesAdapter.myviewholder(view);
    }


    class myviewholder extends RecyclerView.ViewHolder
    {
        TextView queriesFacultyName, queriesStudentRollNumber, queriesTitleAdapter;
        CircleImageView img;
        public myviewholder(@NonNull View itemView)
        {
            super(itemView);
            queriesFacultyName=itemView.findViewById(R.id.queriesFacultyName);
            queriesStudentRollNumber=itemView.findViewById(R.id.queriesStudentRollNumber);
            queriesTitleAdapter=itemView.findViewById(R.id.queriesTitleAdapter);
            img=itemView.findViewById(R.id.queriesFacultyImage);
        }
    }
}
