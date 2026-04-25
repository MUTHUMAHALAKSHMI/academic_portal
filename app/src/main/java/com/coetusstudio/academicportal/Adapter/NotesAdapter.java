package com.coetusstudio.academicportal.Adapter;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coetusstudio.academicportal.Model.Notes;
import com.coetusstudio.academicportal.R;
import com.coetusstudio.academicportal.Activity.Notes.ViewpdfActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class NotesAdapter extends FirebaseRecyclerAdapter<Notes,NotesAdapter.myviewholder>{

    private boolean isDeleteVisible = false;

    public NotesAdapter(@NonNull FirebaseRecyclerOptions<Notes> options) {
        super(options);
    }

    public NotesAdapter(@NonNull FirebaseRecyclerOptions<Notes> options, boolean isDeleteVisible) {
        super(options);
        this.isDeleteVisible = isDeleteVisible;
    }

    @Override
    protected void onBindViewHolder(@NonNull final myviewholder holder, int position, @NonNull final Notes Notes) {

        holder.header.setText(Notes.getFilename());

        holder.img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.img1.getContext(), ViewpdfActivity.class);
                intent.putExtra("filename",Notes.getFilename());
                intent.putExtra("fileurl",Notes.getFileurl());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                holder.img1.getContext().startActivity(intent);
            }
        });

        holder.header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.header.getContext(), ViewpdfActivity.class);
                intent.putExtra("filename",Notes.getFilename());
                intent.putExtra("fileurl",Notes.getFileurl());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                holder.header.getContext().startActivity(intent);
            }
        });

        holder.notesDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile(holder.header.getContext(),Notes.getFilename(),".pdf",DIRECTORY_DOWNLOADS,Notes.getFileurl());
            }
        });

        if (isDeleteVisible) {
            holder.notesDelete.setVisibility(View.VISIBLE);
            holder.notesDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.header.getContext());
                    builder.setTitle("Warning");
                    builder.setMessage("Are you sure want to delete...?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getRef(holder.getAbsoluteAdapterPosition()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(holder.header.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(holder.header.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
            });
        } else {
            holder.notesDelete.setVisibility(View.GONE);
        }

    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        downloadmanager.enqueue(request);
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.singlerownotes,parent,false);
        return  new myviewholder(view);
    }

    public class myviewholder extends RecyclerView.ViewHolder
    {
        ImageView img1, notesDownload, notesDelete;
        TextView header;
        public myviewholder(@NonNull View itemView)
        {
            super(itemView);

            img1=itemView.findViewById(R.id.imgNotes);
            header=itemView.findViewById(R.id.notesTitle);
            notesDownload=itemView.findViewById(R.id.notesDownload);
            notesDelete=itemView.findViewById(R.id.notesDelete);

        }
    }

}
