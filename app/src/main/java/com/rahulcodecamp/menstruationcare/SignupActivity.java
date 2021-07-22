package com.rahulcodecamp.menstruationcare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;


public class SignupActivity extends AppCompatActivity {

    LottieAnimationView signupBackground;

    Button signupButton;
    TextView signInTextView;
    EditText nameEditText, addressEditText, phoneEditText, emailEditText, passwordEditText;
    CountryCodePicker ccp;

    LinearLayout signUpWithPhoneLayout;

    CircularImageView profileImageView;
    private static final int PERMISSION_FILE = 23;  // for taking image
    private static final int ACCESS_FILE = 43;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    String imageURI; // To store default image uri/firebase image token i.e.,default profile_image

    ProgressDialog progressDialog;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;


    Uri imageUri;   // used to pic image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupBackground = findViewById(R.id.splashAnimation);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        signupButton = findViewById(R.id.signupButton);
        signInTextView = findViewById(R.id.signInTextView);
        profileImageView = findViewById(R.id.profileImageView);

        nameEditText = findViewById(R.id.nameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        ccp = findViewById(R.id.ccp);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        signUpWithPhoneLayout = findViewById(R.id.signUpWithPhoneLayout);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                String name = nameEditText.getText().toString();
                String address = addressEditText.getText().toString();
                String phoneNo = ccp.getSelectedCountryCodeWithPlus()+phoneEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if(TextUtils.isEmpty(name)){
                    progressDialog.dismiss();

                    nameEditText.setError("please enter Your full name");
                    Toast.makeText(SignupActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(address)){
                    progressDialog.dismiss();

                    addressEditText.setError("please enter Your Address");
                    Toast.makeText(SignupActivity.this, "please enter Your Address", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(phoneNo)){
                    progressDialog.dismiss();

                    phoneEditText.setError("please enter phone number");
                    Toast.makeText(SignupActivity.this, "please enter phone number", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password)){
                    progressDialog.dismiss();

                    passwordEditText.setError("please enter password");
                    Toast.makeText(SignupActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                }
                else if (!email.matches(emailPattern)){
                    progressDialog.dismiss();

                    emailEditText.setError("invalid email");
                    Toast.makeText(SignupActivity.this, "invalid email", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(email)){
                    progressDialog.dismiss();

                    emailEditText.setError("please enter email");
                    Toast.makeText(SignupActivity.this, "Please enter valid data", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.show();
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        Toast.makeText(SignupActivity.this, "Verification link has been sent to your registered email id\nPlease verify that...", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(SignupActivity.this, "something went wrong"+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                    // storage
                                    DatabaseReference databaseReference = database.getReference().child("user").child(auth.getUid());
                                    StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

                                    if (imageUri !=null){  // if we choose/pic any profile pic from our phone storage
                                        storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            imageURI = uri.toString();
                                                            Users users = new Users(auth.getUid(),name, phoneNo, address, email, imageURI);
                                                            databaseReference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(SignupActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                                    }
                                                                    else {
                                                                        Toast.makeText(SignupActivity.this, "Error in creating a new user", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                    else {            // if user doesn't chose any profile pic then this access token url will be the profile pic
                                        imageURI = "https://firebasestorage.googleapis.com/v0/b/menstruationcare-52744.appspot.com/o/profile_image.png?alt=media&token=b19905f7-83dc-481e-a049-19d4716aab23";
                                        Users users = new Users(auth.getUid(), name, phoneNo, address, email, imageURI);
                                        databaseReference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SignupActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                }
                                                else {
                                                    Toast.makeText(SignupActivity.this, "Error in creating a new user", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(SignupActivity.this, "something went wrong " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));

            }
        });

        signUpWithPhoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, PhoneSignupActivity.class));
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // image picker
                if(ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SignupActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_FILE);
                }
                else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), ACCESS_FILE);


                }
            }

        });
    }
    // belows code is a part of profileImageView.clickListener

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // for cropped image picking
        if (requestCode == ACCESS_FILE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setActivityTitle("Crop Image")
                    .setFixAspectRatio(true)
                    .setCropMenuCropButtonTitle("Done")
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                profileImageView.setImageURI(imageUri); // setting image
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        finishActivity(0);
    }
}