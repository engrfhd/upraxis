package com.engr.fhd.hired.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.engr.fhd.hired.Models.Users;
import com.engr.fhd.hired.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class UserProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;
    private DatabaseReference mRef;

    private ImageView profile_picture;
    private TextView name, age, email, birthday, address, contact_person, contact_number;

    private Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();
        if (null != intent) {

            Users users = intent.getParcelableExtra("user");
            if (null != users) {
                this.users = users;
            }
        }

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

        populateFields();
    }

    private void populateFields() {
        Glide.with(getApplicationContext()).load(users.getPhoto_url()).fitCenter().into(profile_picture);
        name.setText(users.getFirst_name() + " " + users.getLast_name());
        age.setText(users.getAge() + " years old");
        birthday.setText(users.getBirthday());
        address.setText(users.getAddress());
        email.setText(users.getEmail());
        contact_person.setText(users.getContact_person());
        contact_number.setText(users.getContact_number());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
