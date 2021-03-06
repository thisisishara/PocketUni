package com.example.pocketuni.results.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pocketuni.R;
import com.example.pocketuni.security.SigninActivity;
import com.example.pocketuni.util.AdminBottomNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AdminResultsActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Context context = AdminResultsActivity.this;
    private static final int ACTIVITY_NUMBER = 3;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    CardView addResults, updateResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_admin_results);

        addResults = findViewById(R.id.results_tile_add);
        updateResults = findViewById(R.id.results_tile_update);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //redirect if already logged in
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            finish();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        AdminBottomNavigationHelper.enableNavigation(context, bottomNavigationView, ACTIVITY_NUMBER);

        addResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AdminResultsAdd.class);
                startActivity(intent);
            }
        });

        updateResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
    }

    public void openDialog() {
        DialogBox dialogBox = new DialogBox();
        dialogBox.show(getSupportFragmentManager(), "dialog box");
    }

    private void updateUserOnlineStatus(String status){
        HashMap<String,Object> userStatus = new HashMap<String, Object>();
        userStatus.put("status", status);

        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getUid());
        documentReference.update(userStatus);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserOnlineStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserOnlineStatus("online");
    }
}