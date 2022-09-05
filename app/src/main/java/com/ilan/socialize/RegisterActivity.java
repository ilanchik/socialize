package com.ilan.socialize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ilan.socialize.databinding.ActivityRegisterBinding;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private EditText username, name, email, password;
    private Button signup;
    private TextView login;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setLogoColor(binding.socializeLogo);

        // Animate linear layout
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);
        fadeIn.setFillAfter(true);
        binding.linearLayoutRegister.startAnimation(fadeIn);

        // Link variables with XML
        username = binding.username;
        name = binding.name;
        email = binding.email;
        password = binding.password;
        signup = binding.signup;
        login = binding.login;

        // Initialize Firebase database and authentication
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        signup.setOnClickListener(view -> {
            String txtUsername = username.getText().toString();
            String txtName = name.getText().toString();
            String txtEmail = email.getText().toString();
            String txtPassword = password.getText().toString();

            if (txtUsername.isEmpty() || txtName.isEmpty() || txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Empty Credentials",
                        Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8) {
                Toast.makeText(RegisterActivity.this, "Password length too short.",
                        Toast.LENGTH_SHORT).show();
            } else {
                registerUser(txtUsername, txtName, txtEmail, txtPassword);
            }
        });


    }

    /**
     * This method will register (sign up) user in Firebase authentication
     * as well as create a data entry in Firebase database with user information.
     * @param username username
     * @param name full name
     * @param email email
     * @param password password
     */
    private void registerUser(String username, String name, String email, String password) {

        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                // Create hashMap of attributes
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("name", name);
                hashMap.put("email", email);
                hashMap.put("username", username);
                hashMap.put("id", mAuth.getCurrentUser().getUid());
                hashMap.put("bio", "");
                hashMap.put("imageurl", "default");

                // Add map to firebase database
                mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Please update profile!",
                                            Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Used to set the logo gradient color
     * @param tv TextView object to colorize
     */
    public void setLogoColor(TextView tv) {
        Shader shader = new LinearGradient(0, 0, 0, tv.getLineHeight(),
                Color.parseColor("#E91E63"), Color.parseColor("#FFC107"),
                Shader.TileMode.REPEAT);

        tv.getPaint().setShader(shader);
    }
}