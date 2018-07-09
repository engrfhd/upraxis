package com.engr.fhd.hired.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.engr.fhd.hired.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;
    private DatabaseReference mRef;

    private TextInputLayout login_email, login_password;
    private String email, password, reset_email = "";
    private TextView forgot_password;
    private Button login_button, sign_up_button;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        initializeTextFields();
        initialize_register();
        initialize_login();
        initialize_forgot_password();

    }

    private void initialize_forgot_password() {
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                    builder.setTitle("RESET PASSWORD");
                    builder.setMessage("Please enter your email");

                    final EditText input = new EditText(LoginActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton("REQUEST", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            reset_email = input.getText().toString();
                            resetPassword(reset_email);
                        }
                    });

                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();
                        }
                    });

                    builder.show();

                } else {

                    Toast.makeText(LoginActivity.this, "Can't proceed. No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void resetPassword(final String reset_email) {
        mAuth.sendPasswordResetEmail(reset_email)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            hideProgressDialog();

                            Toast.makeText(LoginActivity.this, "We have successfully sent your password request to "
                                    + reset_email, Toast.LENGTH_SHORT).show();
                        } else {

                            hideProgressDialog();

                            Toast.makeText(LoginActivity.this, "Failed to send reset password request to "
                                    + reset_email + ". Please try again", Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void initializeTextFields() {

        login_email = (TextInputLayout) findViewById(R.id.login_email);
        login_password = (TextInputLayout) findViewById(R.id.login_password);
    }

    private void initialize_register() {
        sign_up_button = (Button) findViewById(R.id.signup_btn);
        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    finish();

                } else {

                    Toast.makeText(LoginActivity.this, "Can't proceed. No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initialize_login() {
        login_button = (Button) findViewById(R.id.login_btn);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

                    if (field_checker()) {

                        String emails = email;
                        String pass = password;

                        showProgressDialog("Signing in...");

                        authWithEmailAndPassword(emails, pass);
                    }

                } else {

                    Toast.makeText(LoginActivity.this, "Can't proceed. No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void authWithEmailAndPassword(final String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            current_user = mAuth.getCurrentUser();
                            hideProgressDialog();

                            //checkIfEmailVerified();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        } else {

                            hideProgressDialog();
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(LoginActivity.this, "Login failed due to " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void checkIfEmailVerified() {
        if (current_user.isEmailVerified()) {

            checkIfFirstTimeUser();

        } else {

            Toast.makeText(LoginActivity.this, "Email is not verified", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (current_user != null) {

            checkIfFirstTimeUser();
        }
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {

            mProgressDialog.dismiss();
        }
    }

    public void checkIfFirstTimeUser() {
        mRef.child("users/" + current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    startActivity(new Intent(LoginActivity.this, SplashActivity.class));
                    finish();

                } else {

                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public final static boolean isValidEmail(CharSequence target) {

        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private Boolean field_checker() {

        Boolean errorExist = false;

        if (!login_email.getEditText().getText().toString().isEmpty()) {
            if (isValidEmail(login_email.getEditText().getText().toString())) {

                email = login_email.getEditText().getText().toString();
            } else {

                login_email.getEditText().setError("Invalid email address");
                errorExist = true;
            }

        } else {

            login_email.getEditText().setError("Enter an email");
            errorExist = true;
        }

        if (!login_password.getEditText().getText().toString().isEmpty()) {

            password = login_password.getEditText().getText().toString();
        } else {

            login_password.getEditText().setError("Enter a password");
            errorExist = true;
        }

        if (errorExist) {

            return false;
        } else {

            return true;
        }

    }
}
