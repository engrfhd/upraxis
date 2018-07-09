package com.engr.fhd.hired.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.engr.fhd.hired.Models.Users;
import com.engr.fhd.hired.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MyProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;
    private DatabaseReference mRef;

    private ImageView profile_picture;
    private TextView name, age, email, birthday, address, contact_person, contact_number;
    private Button logout_btn;

    private Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

            initializeFields();

        } else {

            mRef.child("users/" + current_user.getUid()).keepSynced(true);
            initializeFields();
        }

        initializeView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        ShortcutBadger.removeCount(MyProfileActivity.this);
        if (current_user != null) {

            checkIfFirstTimeUser();

        } else {

        }
    }

    private void initializeView() {
        profile_picture = (ImageView) findViewById(R.id.profile_picture);
        name = (TextView) findViewById(R.id.profile_name);
        age = (TextView) findViewById(R.id.profile_age);
        birthday = (TextView) findViewById(R.id.profile_birthday);
        address = (TextView) findViewById(R.id.profile_address);
        email = (TextView) findViewById(R.id.profile_email);
        contact_person = (TextView) findViewById(R.id.profile_contact_person);
        contact_number = (TextView) findViewById(R.id.profile_contact_number);

        initializeLogout();
    }

    private void initializeFields() {
        mRef.child("users/" + current_user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    users = dataSnapshot.getValue(Users.class);

                    Glide.with(getApplicationContext()).load(users.getPhoto_url()).fitCenter().into(profile_picture);
                    populateFields();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateFields() {
        name.setText(users.getFirst_name()  + " " + users.getLast_name());
        age.setText(users.getAge() + " years old");
        birthday.setText(users.getBirthday());
        address.setText(users.getAddress());
        email.setText(users.getEmail());
        contact_person.setText(users.getContact_person());
        contact_number.setText(users.getContact_number());
    }

    private void initializeLogout() {
        logout_btn = (Button) findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

                    if (current_user != null) {
                        switch (current_user.getProviders().get(0)) {
                            case "password": {
                                FirebaseAuth.getInstance().signOut();
                            }
                        }
                        startActivity(new Intent(MyProfileActivity.this, LoginActivity.class));
                        finish();
                    }

                } else {

                    Toast.makeText(MyProfileActivity.this, "Can't proceed. No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void checkIfFirstTimeUser() {
        if (current_user != null) {
            mRef.child("users").orderByKey().equalTo(current_user.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (current_user != null) {

                        startActivity(new Intent(MyProfileActivity.this, LoginActivity.class));
                        finish();
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {

        finish();
        super.onBackPressed();
    }
}
