package com.example.dblogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView tvSchool;
    boolean[] selectedSchool;
    List<Integer> schoolList = new ArrayList<>();
    boolean isAllSelected = false;
    String[] schoolArray;
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    List<String> schoolNames = new ArrayList<>();
    TextView tvRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, LoginActivity.class));
        tvSchool = findViewById(R.id.select_schools);

        tvRole = findViewById(R.id.tvRole);
        FirebaseAuth firebaseRef = FirebaseAuth.getInstance();

        tvSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customSwitch();
                checkAdminRole(firebaseRef.getCurrentUser().getUid());

            }
        });

        FirebaseApp.initializeApp(this); // Initialize Firebase
        db = FirebaseFirestore.getInstance(); // Get Firestore instance


//        List<String> schoolNames = new ArrayList<>();
//        schoolNames.add("School 1");
//        schoolNames.add("School 2");
//        schoolNames.add("School 3");

        List<String> uids = new ArrayList<>();
        uids.add("uid1");
        uids.add("uid2");
        uids.add("uid3");


//        addSchoolNames(schoolNames, uids);
        readSchoolNames();
    }

    private void checkAdminRole(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                if (user.getRole().equals("admin")) {
                                    // User is an admin
                                    // Add your logic for handling admin user here
                                    tvRole.setText("Admin");
                                    Toast.makeText(MainActivity.this, "User is an admin", Toast.LENGTH_SHORT).show();
                                } else {
                                    // User is not an admin
                                    // Add your logic for handling non-admin user here
                                    tvRole.setText("User");
                                    Toast.makeText(MainActivity.this, "User is not an admin", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            // User document does not exist
                            Toast.makeText(MainActivity.this, "User document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to check admin role
                        Toast.makeText(MainActivity.this, "Failed to check admin role.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addSchoolNames(List<String> schoolNames, List<String> uids) {
        CollectionReference schoolsCollection = db.collection("schools");

        if (schoolNames.size() != uids.size()) {
            Log.e(TAG, "Error: School names and UIDs have different sizes.");
            return;
        }

        for (int i = 0; i < schoolNames.size(); i++) {
            String schoolName = schoolNames.get(i);
            String uid = uids.get(i);

            DocumentReference documentReference = schoolsCollection.document(uid);

            Map<String, Object> schoolData = new HashMap<>();
            schoolData.put("name", schoolName);
            schoolData.put("uid", uid);

            documentReference.set(schoolData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "School added: " + schoolName + ", UID: " + uid);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error adding school: " + schoolName, e);
                        }
                    });
        }
    }


    private void readSchoolNames() {
        CollectionReference schoolsCollection = db.collection("schools");

        schoolsCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String schoolName = documentSnapshot.getString("name");
                            String uid = documentSnapshot.getString("uid");
                            Log.d(TAG, "School name: " + schoolName + ", UID: " + uid);
                            schoolNames.add(schoolName);
                        }
                        schoolArray = new String[schoolNames.size()];

                        for (int i = 0; i < schoolNames.size(); i++) {
                            schoolArray[i] = schoolNames.get(i);
                        }

                        selectedSchool = new boolean[schoolArray.length];
                        tvSchool.setTextColor(Color.BLACK);

                        Arrays.fill(selectedSchool, true);
                        for (int i = 0; i < schoolArray.length; i++) {
                            schoolList.add(i);
                        }
                        tvSchool.setText("All");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving school names", e);
                    }
                });
    }

    private void deleteSchool(String uid) {
        CollectionReference schoolsCollection = db.collection("schools");
        DocumentReference documentReference = schoolsCollection.document(uid);

        documentReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "School with UID: " + uid + " deleted successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting school with UID: " + uid, e);
                    }
                });
    }

    private void customSwitch() {
        isAllSelected = true;


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Select school")
                .setMultiChoiceItems(schoolArray, selectedSchool, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        selectedSchool[which] = isChecked; // Update selectedSchool array at the clicked position
                        if (isChecked) {
                            schoolList.add(which);
                            Collections.sort(schoolList);
                        } else {
                            schoolList.remove(Integer.valueOf(which)); // Remove the clicked position from schoolList
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < schoolList.size(); i++) {
                            stringBuilder.append(schoolArray[schoolList.get(i)]);
                            if (i != schoolList.size() - 1) {
                                stringBuilder.append(", ");
                            }
                        }
                        for (boolean i : selectedSchool) {
                            if (!i) {
                                isAllSelected = false;
                                break;
                            }
                        }
                        if (isAllSelected) {
                            tvSchool.setText("All");
                        } else {
                            tvSchool.setText(stringBuilder.toString());
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Arrays.fill(selectedSchool, false);
                        schoolList.clear();
                        tvSchool.setText("");
                    }
                });

        builder.show();
    }
}
