package com.ilan.socialize;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ilan.socialize.databinding.ActivityStartBinding;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    private ActivityStartBinding binding;
    private Button login;
    private Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setLogoColor(binding.socializeLogo);
        animation();

        login = binding.login;
        signup = binding.signup;

        signup.setOnClickListener(view -> {
            startActivity(new Intent(StartActivity.this, RegisterActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        login.setOnClickListener(view -> {
            startActivity(new Intent(StartActivity.this, LoginActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

    }

    /**
     * This method is used to set the logo color
     * @param tv TextView object to change color
     */
    public void setLogoColor(TextView tv) {
        Shader shader = new LinearGradient(0, 0, 0, tv.getLineHeight(),
                Color.parseColor("#E91E63"), Color.parseColor("#FFC107"),
                Shader.TileMode.REPEAT);

        tv.getPaint().setShader(shader);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is already logged in, if so direct to MainActivity dashboard.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
        }
    }

    /**
     * This method is used to animate the opening activity, including logo animation
     * fade in, fade out
     */
    private void animation() {
        binding.linearLayout.animate().alpha(0f).setDuration(1);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);
        fadeIn.setFillAfter(true);

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(2000);
        fadeOut.setFillAfter(true);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.goSomewhere.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.goSomewhere.clearAnimation();
                binding.goSomewhere.setVisibility(View.INVISIBLE);
                binding.linearLayout.animate().alpha(1f).setDuration(3000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        binding.goSomewhere.startAnimation(fadeIn);
    }
}