package com.engr.fhd.hired.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.engr.fhd.hired.Adapter.UserAdapter;
import com.engr.fhd.hired.Models.Users;
import com.engr.fhd.hired.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserAdapter.OnItemClickListener {
    FirebaseUser current_user;
    FirebaseAuth mAuth;
    DatabaseReference mRef;
    RecyclerView user_rv;
    UserAdapter userAdapter;
    DividerItemDecoration dividerItemDecoration;

    private List<Users> users;

    String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();
        users = new ArrayList<>();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

            initializeList();

        } else {

            mRef.child("users/" + current_user.getUid()).keepSynced(true);
            initializeList();
        }

        initializeView();
    }

    private void initializeView() {
        user_rv = (RecyclerView) findViewById(R.id.user_rv);
        user_rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        user_rv.setLayoutManager(llm);
        dividerItemDecoration = new DividerItemDecoration(this, llm.getOrientation());
        user_rv.addItemDecoration(dividerItemDecoration);
        userAdapter = new UserAdapter(users);
        user_rv.setAdapter(userAdapter);
        userAdapter.setOnItemClickListener(this);
        userAdapter.notifyDataSetChanged();
    }

    private void initializeList() {

        mRef.child("users/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.e("eee", "USER KEY " + dataSnapshot.toString());
                userKey = dataSnapshot.getKey();

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

                } else {


                    mRef.child("users").orderByKey().equalTo(userKey).keepSynced(true);
                }

                mRef.child("users").orderByKey().equalTo(userKey).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()) {

                            Users usr = new Users(dataSnapshot);

                            users.add(usr);
                            userAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {

        Users user = users.get(position);

        if (user != null) {

            Intent intent = new Intent(UsersActivity.this,UserProfileActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        }
    }
}
