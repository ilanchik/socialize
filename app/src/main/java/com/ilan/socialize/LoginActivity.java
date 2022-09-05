package com.ilan.socialize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.ilan.socialize.databinding.ActivityLoginBinding;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setLogoColor(binding.socializeLogo);

        // Animate linear layout
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);
        fadeIn.setFillAfter(true);
        binding.linearLayoutLogin.startAnimation(fadeIn);

        mAuth = FirebaseAuth.getInstance();

        binding.signup.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        binding.login.setOnClickListener(view -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter credentials.", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    /**
     * This method will login user using firebase account
     * @param email user's email
     * @param password user's password
     */
    private void loginUser(String email, String password) {

        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Please update profile!",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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