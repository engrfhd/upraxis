package com.engr.fhd.hired.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.engr.fhd.hired.Models.Users;
import com.engr.fhd.hired.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;
    private DatabaseReference mRef;

    private ImageView profile_picture;
    private Button my_profile_btn, view_user_btn;

    private Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

            initializeView();

        } else {

            mRef.child("users/" + current_user.getUid()).keepSynced(true);
            initializeView();
        }

        initializeView();
    }

    private void initializeView() {
        profile_picture = (ImageView) findViewById(R.id.profile_picture);

        mRef.child("users/" + current_user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    users = dataSnapshot.getValue(Users.class);

                    Glide.with(getApplicationContext()).load(users.getPhoto_url()).fitCenter().into(profile_picture);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        initializeViewProfile();
        initializeViewUsers();
    }

    private void initializeViewProfile() {
        my_profile_btn = (Button) findViewById(R.id.my_profile_btn);
        my_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            }
        });
    }

    private void initializeViewUsers() {
        view_user_btn = (Button) findViewById(R.id.view_all_btn);
        view_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, UsersActivity.class));
            }
        });
    }
}
