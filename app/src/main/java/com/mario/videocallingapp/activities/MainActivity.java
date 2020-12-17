package com.mario.videocallingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mario.videocallingapp.R;
import com.mario.videocallingapp.adapters.UsersAdapter;
import com.mario.videocallingapp.listeners.UsersListeners;
import com.mario.videocallingapp.models.Users;
import com.mario.videocallingapp.utilities.Constants;
import com.mario.videocallingapp.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListeners {

    private PreferenceManager preferenceManager;
    private List<Users> usersList;
    private UsersAdapter usersAdapter;
    private TextView textErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());

        TextView textTitle = findViewById(R.id.textTitle);
        textTitle.setText(
                String.format(
                        "%s %s",
                        preferenceManager.getString(Constants.KET_FIRST_NAME),
                        preferenceManager.getString(Constants.KEY_LAST_NAME)
                )
        );
        findViewById(R.id.textSignOut).setOnClickListener(v -> signOut());

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                sendFCMTokenToDatabase(task.getResult().getToken());
            }
        });

        RecyclerView usersRecyclerView = findViewById(R.id.usersRecyclerView);
        textErrorMessage = findViewById(R.id.textErrorMessage);

        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList, this);
        usersRecyclerView.setAdapter(usersAdapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();
    }

    private void getUsers() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        usersList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (myUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            Users users = new Users();
                            users.firstName = documentSnapshot.getString(Constants.KET_FIRST_NAME);
                            users.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            users.email = documentSnapshot.getString(Constants.KET_EMAIL);
                            users.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            usersList.add(users);
                        }
                        if (usersList.size() > 0) {
                            usersAdapter.notifyDataSetChanged();
                        } else {
                            textErrorMessage.setText(String.format("%s", "no users available"));
                            textErrorMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        textErrorMessage.setText(String.format("%s", "no users available"));
                        textErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendFCMTokenToDatabase(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> Toast.makeText(this, "Unable to send token: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void signOut() {
        Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreference();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Unable to sign out", Toast.LENGTH_SHORT).show());
    }


    @Override
    public void initiateVideoMeeting(Users users) {
        if (users.token == null || users.token.trim().isEmpty()) {
            Toast.makeText(this,
                    users.firstName + " " + users.lastName + " is not available for meeting",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext() , OutgoingInvitationActivity.class);
            intent.putExtra("user",users);
            intent.putExtra("type","video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(Users users) {
        if (users.token == null || users.token.trim().isEmpty()) {
            Toast.makeText(this,
                    users.firstName + " " + users.lastName + " is not available for meeting",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext() , OutgoingInvitationActivity.class);
            intent.putExtra("user",users);
            intent.putExtra("type","audio");
            startActivity(intent);
        }
    }
}