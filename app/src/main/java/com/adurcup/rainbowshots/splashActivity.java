package com.adurcup.rainbowshots;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by kshivang on 15/11/16.
 */

public class SplashActivity extends AppCompatActivity{

    boolean isAnimationFinished = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        Animation bounceAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        findViewById(R.id.game_name).setAnimation(bounceAnim);

        bounceAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimationFinished = true;
                startActivity(new Intent(SplashActivity.this, GameActivity.class));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isAnimationFinished) {
            finish();
        }
    }
}
