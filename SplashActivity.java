package com.engr.fhd.hired.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.engr.fhd.hired.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    static int SPLASH_TIME_OUT = 3000;

    private FirebaseAuth mAuth;
    private FirebaseUser current_user;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();

        splash();
    }

    public void splash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (current_user != null) {

                    if (networkConnected()) {

                        checkIfFirstTimeUser();

                    } else {

                        AlertDialog alert = new AlertDialog.Builder(SplashActivity.this).create();
                        alert.setMessage("Please check your internet connection");
                        alert.setCancelable(false);
                        alert.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                                finish();
                            }
                        });
                        alert.show();

                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                        if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

                            checkIfFirstTimeUser();

                        } else {

                            Toast.makeText(SplashActivity.this, "Can't proceed. No internet connection", Toast.LENGTH_LONG).show();
                        }
                    }

                } else {

                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }

            }
        }, SPLASH_TIME_OUT);
    }

    public final boolean networkConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null;
    }

    public void checkIfFirstTimeUser() {

        mRef.child("users/"+current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();

                } else {

                    startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
