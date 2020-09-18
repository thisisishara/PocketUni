package com.example.pocketuni.organizer.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pocketuni.R;
import com.example.pocketuni.model.CurrentUser;
import com.example.pocketuni.model.Timetable;
import com.example.pocketuni.model.TimetableItem;
import com.example.pocketuni.organizer.common.TimetableListAdapter;
import com.example.pocketuni.security.SigninActivity;
import com.example.pocketuni.timeline.std.MainActivity;
import com.example.pocketuni.util.StdBottomNavigationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class AdminTimetableActivity extends AppCompatActivity implements AddTimetableDialog.AddTimetableDialogListener {
    private FloatingActionButton addTimetable;
    private TextView textViewInfo;
    private RecyclerView recyclerViewTimetables;
    private String yearSemester;
    private String course;
    private String batch;
    private String year;
    private String semester;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onResume() {
        super.onResume();
        getAvailableTimeTables();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_timetable);

        addTimetable = findViewById(R.id.addTimetablesFloatingActionButton);
        textViewInfo = findViewById(R.id.textView);
        recyclerViewTimetables = findViewById(R.id.timetablesRecyclerView);
        recyclerViewTimetables.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //redirect if an invalid user
        if(firebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            finish();
        }

        getAvailableTimeTables();

        addTimetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show add timetable dialog
                showAddTimetableDialog();
            }
        });
    }

    private void showAddTimetableDialog () {
        AddTimetableDialog addTimetableDialog = new AddTimetableDialog();
        addTimetableDialog.show(getSupportFragmentManager(), "Add Timetable Dialog");
    }

    @Override
    public void getNewTimetableData(String yearSemesterSpinner, String courseSpinner, String batchNumber) {
        this.yearSemester = yearSemesterSpinner;
        getYearAndSemester(this.yearSemester);
        this.course = courseSpinner;
        this.batch = "Y"+this.year+"S"+this.semester+"G"+batchNumber;

        final String timetableName = course + " " + this.batch;

        final Map<String, Object> newTimetable = new HashMap<>();
        newTimetable.put("timetable_name", timetableName);
        newTimetable.put("timetable_year", this.year);
        newTimetable.put("timetable_semester", this.semester);
        newTimetable.put("timetable_course", this.course);
        newTimetable.put("timetable_batch", this.batch);
        newTimetable.put("timetable_reminders", false);
        newTimetable.put("timetable_hours", 0);
        newTimetable.put("timetable_minutes", 0);

        final DocumentReference documentReference = firebaseFirestore.collection("timetables").document(timetableName);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    showToast("TIMETABLE ALREADY EXISTS");
                }
                else{
                    documentReference.set(newTimetable).addOnSuccessListener(new OnSuccessListener<Void>() {
                        private static final String TAG = "ADDTIMETABLE";

                        @Override
                        public void onSuccess(Void aVoid) {

                            Log.d(TAG, "Timetable \"" + timetableName + "\" created successfully.");

                            showToast("TIMETABLE ADDED SUCCESSFULLY");
                            Intent intent = new Intent(getApplicationContext(), AdminViewTimetableActivity.class);
                            intent.putExtra("timetableName", timetableName);
                            intent.putExtra("timetableBatch", batch);
                            intent.putExtra("timetableYear", year);
                            intent.putExtra("timetableSemester", semester);
                            intent.putExtra("timetableCourse", course);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    private void showToast (String message) {
        Toast.makeText(AdminTimetableActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void getYearAndSemester(String year_semester) {
        switch (year_semester) {
            case "Year 1 Semester 1":
                year = "1";
                semester = "1";
                break;
            case "Year 1 Semester 2":
                year = "1";
                semester = "2";
                break;
            case "Year 2 Semester 1":
                year = "2";
                semester = "1";
                break;
            case "Year 2 Semester 2":
                year = "2";
                semester = "2";
                break;
            case "Year 3 Semester 1":
                year = "3";
                semester = "1";
                break;
            case "Year 3 Semester 2":
                year = "3";
                semester = "2";
                break;
            case "Year 4 Semester 1":
                year = "4";
                semester = "1";
                break;
            case "Year 4 Semester 2":
                year = "4";
                semester = "2";
                break;
            default:
                year = "N/A";
                semester = "N/A";
                break;
        }
    }

    private void getAvailableTimeTables(){
        //show currently available timetables
        CollectionReference collectionReference = firebaseFirestore.collection("timetables");
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()) {
                    textViewInfo.setText(getResources().getString(R.string.admin_timetable_description));
                    recyclerViewTimetables.setVisibility(View.VISIBLE);

                    List<Timetable> timetables = new ArrayList<Timetable>();

                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                        //putting into the recycler view
                        Timetable timetable = documentSnapshot.toObject(Timetable.class);
                        timetables.add(timetable);
                    }

                    TimetableListAdapter timetableListAdapter = new TimetableListAdapter(AdminTimetableActivity.this, timetables);
                    recyclerViewTimetables.setAdapter(timetableListAdapter);
                    recyclerViewTimetables.setLayoutManager(new LinearLayoutManager(AdminTimetableActivity.this));

                } else {
                    textViewInfo.setText(getResources().getString(R.string.admin_timetable_description_error));
                    showToast("ERROR OCCURRED WHILE GETTING TIMETABLES.");
                }
            }
        });
    }
}