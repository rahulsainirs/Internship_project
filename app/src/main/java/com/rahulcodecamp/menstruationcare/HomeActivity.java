package com.rahulcodecamp.menstruationcare;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;


    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;

    ProgressDialog progressDialog;
    public ArrayList<String> profileItems;
    LottieAnimationView headerBackground;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();


        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolBar);
        navigationView = findViewById(R.id.navigationView);

        headerBackground = findViewById(R.id.headerBackground);  // animated header background


        setSupportActionBar(toolbar);   // below three lines are to create toggle icon with function
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                drawerLayout.closeDrawer(GravityCompat.START);
                switch (item.getItemId()){
                    case (R.id.logoutBtn):
                        Dialog dialog = new Dialog(HomeActivity.this,R.style.Dialog);
                        dialog.setContentView(R.layout.logout_dialog_layout);
                        Button yesButton, noButton;
                        yesButton = dialog.findViewById(R.id.yesButton);
                        noButton = dialog.findViewById(R.id.noButton);

                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                FirebaseAuth.getInstance().signOut();
                                auth.signOut();
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                            }
                        });
                        noButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;
                    case (R.id.chatsBtn):
                        Toast.makeText(HomeActivity.this, "chats clicked", Toast.LENGTH_SHORT).show();
                        break;

                    }
                    return true;
                    // we will add further cases according to data and uses
            }
        });

//        View view = navigationView.getHeaderView(0);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(false);

//        progressDialog.show();    will use it later


        if (currentUser == null){
            Toast.makeText(this, "Current user == null", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        }

        else if (!currentUser.isEmailVerified() && currentUser.getPhoneNumber().isEmpty()){

            Toast.makeText(this, "your email is not verified", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();

        }
        else if (currentUser.isEmailVerified() && currentUser.getPhoneNumber().isEmpty()){
            Toast.makeText(this, "Email is verified", Toast.LENGTH_SHORT).show();

            DatabaseReference databaseReference = firebaseDatabase.getReference().child("user");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren() ){
                        if (dataSnapshot.getKey().equals(currentUser.getUid())){
//                            Toast.makeText(HomeActivity.this, "uid matched", Toast.LENGTH_SHORT).show();
                            profileItems = new ArrayList<>();
                            profileItems.add(dataSnapshot.child("name").getValue().toString());
                            profileItems.add(dataSnapshot.child("email").getValue().toString());
                            profileItems.add(dataSnapshot.child("imageUri").getValue().toString());

                            TextView nameTextView = findViewById(R.id.nameTextView);
                            TextView emailOrPhoneTextView = findViewById(R.id.emailOrPhoneTextView);
                            CircularImageView profileImageView = findViewById(R.id.profileImageView);

                            nameTextView.setText(profileItems.get(0));
                            emailOrPhoneTextView.setText(profileItems.get(1));
                            Picasso.get().load(profileItems.get(2)).into(profileImageView);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
        else if (currentUser.getPhoneNumber() != null){
            Toast.makeText(this, "PhoneAuth user", Toast.LENGTH_SHORT).show();

            DatabaseReference databaseReference = firebaseDatabase.getReference().child("user");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren() ){
                        if (dataSnapshot.getKey().equals(currentUser.getUid())){
//                            Toast.makeText(HomeActivity.this, "uid matched", Toast.LENGTH_SHORT).show();
                            profileItems = new ArrayList<>();
                            profileItems.add(dataSnapshot.child("name").getValue().toString());
                            profileItems.add(dataSnapshot.child("phoneNo").getValue().toString());
                            profileItems.add(dataSnapshot.child("imageUri").getValue().toString());

                            TextView nameTextView = findViewById(R.id.nameTextView);
                            TextView emailOrPhoneTextView = findViewById(R.id.emailOrPhoneTextView);
                            CircularImageView profileImageView = findViewById(R.id.profileImageView);

                            nameTextView.setText(profileItems.get(0));
                            emailOrPhoneTextView.setText(profileItems.get(1));
                            Picasso.get().load(profileItems.get(2)).into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
        else {
            Toast.makeText(this, "Verify your email", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }
    }


    @Override    // to close the drawer when we touch/press back button
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            onDestroy();
            super.onBackPressed();
        }
    }
}