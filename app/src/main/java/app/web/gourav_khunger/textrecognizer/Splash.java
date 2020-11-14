package app.web.gourav_khunger.textrecognizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Splash extends AppCompatActivity {

    ImageView image;
    TextView title, slogan;
    Animation fade, zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        image = findViewById(R.id.image_splash);
        title = findViewById(R.id.appTitle);
        slogan = findViewById(R.id.appSlogan);
        fade = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        zoom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);

        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    Thread.sleep(1000);
                    startActivity(new Intent(Splash.this, Home.class));
                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        image.startAnimation(fade);
        title.startAnimation(zoom);
        slogan.startAnimation(zoom);
    }
}