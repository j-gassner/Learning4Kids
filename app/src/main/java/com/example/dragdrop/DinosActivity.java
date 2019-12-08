package com.example.dragdrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class DinosActivity extends AppCompatActivity {
    public static int scrollX = 0;
    public static int scrollY = -1;
    HorizontalScrollView hsv;
    SharedPreferences availableLevels;
    private final static int LOCKED = 0;
    private final static int UNLOCKED = 1;
    private final static int COMPLETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinos);
        hideSystemUI();
        hsv = findViewById(R.id.scroll_horizontal_dinos);
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        assignDinos();

    }

    @Override
    protected void onPause() {
        super.onPause();
        scrollX = hsv.getScrollX();
        scrollY = hsv.getScrollY();
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeTint();
        hsv.post(() -> hsv.scrollTo(scrollX, scrollY));

    }

    void removeTint() {
        for (char level : Globals.levels) {
            int idImage = getResources().getIdentifier("dino_" + level, "drawable", this.getPackageName());
            Drawable draw = getResources().getDrawable(idImage);
            if (availableLevels.getInt(level + "", 0) == COMPLETE) {
                draw.clearColorFilter();
            }
        }
    }

    public void assignDinos() {
        for (char level : Globals.levels) {
            int idImage;
            int idButton = getResources().getIdentifier("dino_" + level, "id", this.getPackageName());
            ImageButton iB = findViewById(idButton);
            idImage = getResources().getIdentifier("dino_" + level, "drawable", this.getPackageName());
            Drawable draw = getResources().getDrawable(idImage);
            if (availableLevels.getInt(level + "", 0) != COMPLETE) {
                draw.setColorFilter(ContextCompat.getColor(this, R.color.dark), PorterDuff.Mode.SRC_ATOP);
            }
            iB.setImageResource(idImage);
        }
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, StartActivity.class);
        MediaPlayer mp;
        switch (v.getId()) {
            case R.id.button_back:
                startActivity(intent);
                return;
            case R.id.dino_a:
                if (availableLevels.getInt("a", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_a);
                    mp.start();
                }
                break;
            case R.id.dino_b:
                if (availableLevels.getInt("b", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_b);
                    mp.start();
                }
                break;
            case R.id.dino_e:
                if (availableLevels.getInt("e", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_e);
                    mp.start();
                }
                break;
            case R.id.dino_f:
                if (availableLevels.getInt("f", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_f);
                    mp.start();
                }
                break;
            case R.id.dino_i:
                if (availableLevels.getInt("i", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_i);
                    mp.start();
                }
                break;
            case R.id.dino_l:
                if (availableLevels.getInt("l", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_l);
                    mp.start();
                }
                break;
            case R.id.dino_m:
                if (availableLevels.getInt("m", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_m);
                    mp.start();
                }
                break;
            case R.id.dino_n:
                if (availableLevels.getInt("n", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_n);
                    mp.start();
                }
                break;
            case R.id.dino_o:
                if (availableLevels.getInt("o", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_o);
                    mp.start();
                }
                break;
            case R.id.dino_r:
                if (availableLevels.getInt("r", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_r);
                    mp.start();
                }
                break;
            case R.id.dino_s:
                if (availableLevels.getInt("s", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_s);
                    mp.start();
                }
                break;
            case R.id.dino_t:
                if (availableLevels.getInt("t", 0) == COMPLETE) {
                    mp = MediaPlayer.create(this, R.raw.dino_sound_t);
                    mp.start();
                }
                break;
            default:
                break;

        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}
