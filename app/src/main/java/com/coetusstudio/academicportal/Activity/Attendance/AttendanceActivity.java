package com.coetusstudio.academicportal.Activity.Attendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coetusstudio.academicportal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AttendanceActivity extends AppCompatActivity {

    String sid, subid, subjectName, studentName;
    int tpresent = 0;
    int tattencount = 0;

    TextView studentNameAttendance, studentRollNumberAttendance, studentSubjectAttendance;
    TextView atten_tv;
    TextView absent_tv;
    ListView listViewbydate;

    DatabaseReference attendancerecord, attendancerecbydate;
    ArrayList<String> attendance = new ArrayList<>();
    ProgressBar mpercentprogressbarr;
    TextView mpercent_tv;
    ValueEventListener attendanceListener;

    private final String DB_URL = "https://academic-portal-f2eac-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Intent intent = getIntent();
        sid = intent.getStringExtra("studentRollNumber");
        subid = intent.getStringExtra("studentSection");
        subjectName = intent.getStringExtra("subjectName");
        studentName = intent.getStringExtra("studentName");

        if (subid == null || subjectName == null || sid == null) {
            Toast.makeText(this, "Missing data to load attendance", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        attendancerecord = FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet").child(subid).child(subjectName);
        attendancerecord.keepSynced(true);

        atten_tv = findViewById(R.id.attendence_tv);
        absent_tv = findViewById(R.id.absent_tv);
        mpercent_tv = findViewById(R.id.tv);
        studentNameAttendance = findViewById(R.id.studentNameAttendance);
        studentRollNumberAttendance = findViewById(R.id.studentRollNumberAttendance);
        studentSubjectAttendance = findViewById(R.id.studentSubjectAttendance);

        studentNameAttendance.setText(studentName);
        studentRollNumberAttendance.setText(sid);
        studentSubjectAttendance.setText(subjectName);

        listViewbydate = findViewById(R.id.listviewbydate);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular);
        mpercentprogressbarr = findViewById(R.id.circularProgressbar);
        if (mpercentprogressbarr != null) {
            mpercentprogressbarr.setProgress(0);
            mpercentprogressbarr.setSecondaryProgress(100);
            mpercentprogressbarr.setMax(100);
            mpercentprogressbarr.setProgressDrawable(drawable);
        }

        attendanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tpresent = 0;
                tattencount = 0;

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    DataSnapshot studentSnap = dsp.child(sid);
                    if (studentSnap.exists()) {
                        Object valObj = studentSnap.child("atvalue").getValue();
                        if (valObj == null) continue;

                        String aval = String.valueOf(valObj);
                        try {
                            if (aval.length() >= 3) {
                                tpresent = tpresent + Integer.parseInt(aval.substring(0, 1));
                                tattencount += Integer.parseInt(aval.substring(2, 3));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                int abs = tattencount - tpresent;
                float percent = 0;
                if (tattencount > 0) {
                    percent = (((float) tpresent) / ((float) tattencount)) * 100;
                }

                String attendence = tpresent + "/" + tattencount;
                String absent = String.valueOf(abs);
                if (atten_tv != null) atten_tv.setText(attendence);
                if (absent_tv != null) absent_tv.setText(absent);

                int mpercentint = (int) Math.round(percent);
                String mmper = mpercentint + "%";

                if (mpercent_tv != null) {
                    mpercent_tv.setText(mmper);
                    if (percent <= 75) {
                        mpercent_tv.setTextColor(Color.RED);
                    } else if (percent > 75 && percent < 85) {
                        mpercent_tv.setTextColor(Color.BLUE);
                    } else if (percent >= 85) {
                        mpercent_tv.setTextColor(Color.MAGENTA);
                    }
                }

                if (mpercentprogressbarr != null) {
                    mpercentprogressbarr.setProgress(mpercentint);
                }

                rec();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        attendancerecord.addValueEventListener(attendanceListener);
    }

    public void rec() {
        attendancerecbydate = FirebaseDatabase.getInstance(DB_URL).getReference().child("AttendenRecordSheet").child(subid).child(subjectName);
        attendancerecbydate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isFinishing()) return;

                attendance.clear();
                attendance.add("Date/Time" + "                                   " + "Attendance");
                for (DataSnapshot dspp : dataSnapshot.getChildren()) {
                    String datetime = dspp.getKey();
                    Object avalueObj = dspp.child(sid).child("atvalue").getValue();
                    String avalue = (avalueObj != null) ? String.valueOf(avalueObj) : "N/A";

                    attendance.add(datetime + "                   " + avalue);
                }
                listshow(attendance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void listshow(ArrayList<String> attendancelist) {
        if (isFinishing()) return;
        if (listViewbydate == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, attendancelist);
        listViewbydate.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attendancerecord != null && attendanceListener != null) {
            attendancerecord.removeEventListener(attendanceListener);
        }
    }
}
