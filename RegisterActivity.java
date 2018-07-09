package com.engr.fhd.hired.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.engr.fhd.hired.Models.Users;
import com.engr.fhd.hired.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;
    private DatabaseReference mRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int SELECT_PICTURE = 2;
    private Uri photoUri = null;

    private String register_first_name, register_last_name, register_email, register_address, register_birthday, register_password, register_contact_person, register_contact_number, mCurrentPhotoPath, provider = "";
    private String age;
    private TextInputLayout first_name, last_name, email, address, password, confirm_password, contact_person, contact_number;
    private TextView birthday;
    private ImageView profile_picture;
    private Button register_button;
    private CountryCodePicker ccp;
    private ProgressDialog mProgressDialog;

    private Users users;


    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        current_user = mAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        initializeTextFields();
        initializeRegisterButton();
        initializeAddPhoto();
    }

    public void initializeTextFields() {
        profile_picture = (ImageView) findViewById(R.id.register_picture);
        first_name = (TextInputLayout) findViewById(R.id.register_first_name);
        last_name = (TextInputLayout) findViewById(R.id.register_last_name);
        email = (TextInputLayout) findViewById(R.id.register_email);
        address = (TextInputLayout) findViewById(R.id.register_address);
        birthday = (TextView) findViewById(R.id.register_birthday);
        password = (TextInputLayout) findViewById(R.id.register_password);
        confirm_password = (TextInputLayout) findViewById(R.id.register_confirm_password);
        contact_person = (TextInputLayout) findViewById(R.id.register_contact_person);
        contact_number = (TextInputLayout) findViewById(R.id.register_contact_number);

        ccp = (CountryCodePicker) findViewById(R.id.country_code_picker);
        ccp.registerCarrierNumberEditText(contact_number.getEditText());

        if (current_user != null) {

            password.setVisibility(View.GONE);
            confirm_password.setVisibility(View.GONE);
            first_name.getEditText().setText(current_user.getDisplayName());
            email.setEnabled(false);
            email.getEditText().setText(current_user.getEmail());

        } else {

            email.setEnabled(true);
        }

        birthday.setOnClickListener(new View.OnClickListener() {
            public final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            private int mYear, mMonth, mDay;

            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(RegisterActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                age = "" + getAge (year , (month+1), dayOfMonth);

                                birthday.setText(MONTHS[month] + " " + dayOfMonth + ", " + year);
                                birthday.setError(null);
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
            }
        });
    }

    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age+1);
        String ageS = ageInt.toString();

        return ageS;
    }

    private void initializeRegisterButton() {
        register_button = (Button) findViewById(R.id.register_btn);
        register_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mdata = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if ((wifi != null & mdata != null) && (wifi.isConnected() | mdata.isConnected())) {

                    if (current_user != null) {
                        if (field_checker()) {

                            showProgressDialog("Registering...");
                            mStorageReference = mFirebaseStorage.getReference().child(current_user.getUid() + "/profile_photo/user_photo");

                            mStorageReference.putFile(photoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri photoUrl) {

                                            users = new Users(register_first_name, register_last_name, current_user.getEmail(), register_address, register_birthday, register_contact_person, register_contact_number, photoUrl.toString(), age);

                                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));

                                            hideProgressDialog();
                                            finish();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(RegisterActivity.this, "Registration failed due to " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    } else {

                        if (field_checker()) {

                            String emails = register_email;
                            String pass = register_password;
                            authWithEmailAndPassword(emails, pass);
                            showProgressDialog("Registering...");
                        }
                    }

                } else {

                    Toast.makeText(RegisterActivity.this, "Can't proceed. No internet connection", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    private Boolean field_checker() {
        Boolean errorExists = false;

        //first name
        if (!first_name.getEditText().getText().toString().trim().isEmpty()) {

            register_first_name = first_name.getEditText().getText().toString();

        } else {

            first_name.getEditText().setError("First name is required");
            errorExists = true;
        }

        //last name
        if (!last_name.getEditText().getText().toString().trim().isEmpty()) {

            register_last_name = last_name.getEditText().getText().toString();

        } else {

            first_name.getEditText().setError("Last name is required");
            errorExists = true;
        }

        //email
        if (!email.getEditText().getText().toString().trim().isEmpty()) {

            if (isValidEmail(email.getEditText().getText().toString())) {

                register_email = email.getEditText().getText().toString();

            } else {

                email.getEditText().setError("Invalid email address");
                errorExists = true;
            }

        } else {

            email.getEditText().setError("Email address is required");
            errorExists = true;
        }

        //address
        if (!address.getEditText().getText().toString().trim().isEmpty()) {

            register_address = address.getEditText().getText().toString();

        } else {

            address.getEditText().setError("Address is required");
            errorExists = true;
        }

        //birthday
        if (!birthday.getText().toString().trim().isEmpty()) {

            register_birthday = birthday.getText().toString();

        } else {

            Toast.makeText(this, "Set a profile picture", Toast.LENGTH_SHORT).show();
            errorExists = true;
        }

        //password
        if (current_user == null) {

            if (!password.getEditText().getText().toString().trim().isEmpty()) {

                if (password.getEditText().getText().toString().trim().length() >= 6) {

                    register_password = password.getEditText().getText().toString();

                } else {

                    password.getEditText().setError("Password should be at least 6 characters");
                }

            } else {

                password.getEditText().setError("Password is required");
                errorExists = true;
            }

            if (!confirm_password.getEditText().getText().toString().trim().isEmpty()) {

                if (confirm_password.getEditText().getText().toString().trim().equals(register_password)) {

                    register_password = password.getEditText().getText().toString();

                } else {

                    confirm_password.getEditText().setError("Password do not match");
                    errorExists = true;
                }

            } else {

                confirm_password.getEditText().setError("Confirm your password");
                errorExists = true;
            }
        }

        //contact person
        if (!contact_person.getEditText().getText().toString().trim().isEmpty()) {

            register_contact_person = contact_person.getEditText().getText().toString();

        } else {

            contact_person.getEditText().setError("Contact person is required");
            errorExists = true;
        }

        //contact number
        if (!contact_number.getEditText().getText().toString().trim().isEmpty()) {

            register_contact_number = ccp.getFullNumberWithPlus();

        } else {

            contact_number.getEditText().setError("Contact number is required");
            errorExists = true;
        }

        if (contact_number.getEditText().getText().toString().trim().length() > 10) {

            contact_number.getEditText().setError("Invalid mobile number");
            errorExists = true;
        }

        //profile picture
        if (photoUri == null) {

            Toast.makeText(this, "Set a profile picture", Toast.LENGTH_SHORT).show();
            errorExists = true;
        }

        if (errorExists) {

            return false;

        } else {

            return true;
        }
    }

    private void authWithEmailAndPassword(String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            current_user = mAuth.getCurrentUser();

                            users = new Users(register_first_name, register_last_name, current_user.getEmail(), register_address, register_birthday, register_contact_person, register_contact_number, photoUri.toString(), age);

                            mRef.child("users/" + current_user.getUid()).setValue(users);

                            mStorageReference = mFirebaseStorage.getReference().child(current_user.getUid() + "/profile_photo/user_photo");
                            mStorageReference.putFile(photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            mRef.child("users/" + current_user.getUid() + "/photo_url").setValue(uri.toString());
                                        }
                                    });

                                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));

                                    hideProgressDialog();

                                    sendEmailVerification();
                                    finish();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    hideProgressDialog();
                                    Toast.makeText(RegisterActivity.this, "Registration failed due to " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else

                            hideProgressDialog();

                    }

                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();

                Toast.makeText(RegisterActivity.this, "Registration failed due to " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailVerification() {
        if (current_user != null) {
            current_user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(RegisterActivity.this, "We sent email verification to " + current_user.getEmail() + " to verify your account.", Toast.LENGTH_LONG).show();

                    } else {

                        overridePendingTransition(0, 0);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);

                } else {

                }
            }
        }
    }

    private void initializeGalleryButton() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        } else {

            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);

    }

    private void initializeCameraButton() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (ActivityCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        } else {

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {

                    photoFile = createImageFile();

                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {

                    Uri fileproviderUri = FileProvider.getUriForFile(RegisterActivity.this, "${applicationId}.fileprovider", photoFile);
                    photoUri = fileproviderUri;
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileproviderUri);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE: {
                    try {

                        setPic();

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    break;
                }

                case SELECT_PICTURE: {

                    Uri originalUri = data.getData();
                    profile_picture.setImageURI(originalUri);
                    this.photoUri = originalUri;
                }
            }
        }

    }

    private void setPic() throws IOException {
        // Get the dimensions of the View
        ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);
        Matrix matrix = new Matrix();
        if (rotation != 0f) {

            matrix.preRotate(rotationInDegrees);
        }
        int targetW = profile_picture.getWidth() / 2;
        int targetH = profile_picture.getHeight() / 2;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] data = baos.toByteArray();
        Bitmap adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, targetW, targetH, matrix, true);

        profile_picture.setImageBitmap(adjustedBitmap);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {

            return 90;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {

            return 180;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {

            return 270;
        }
        return 0;
    }

    private void initializeAddPhoto() {
        profile_picture = (ImageView) findViewById(R.id.register_picture);
        profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items = {"Camera", "Gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setTitle("ADD PHOTO");

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (items[i].equals("Camera")) {

                            initializeCameraButton();

                        } else if (items[i].equals("Gallery")) {

                            initializeGalleryButton();
                        }
                    }
                });
                builder.show();
            }
        });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (current_user != null) {

            switch (current_user.getProviders().get(0)) {

                case "password": {

                    FirebaseAuth.getInstance().signOut();

                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                    break;
                }

                default: {

                    FirebaseAuth.getInstance().signOut();
                }
            }
        }
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
