package com.example.dragdrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class DinosActivity extends AppCompatActivity {

    public static int scrollX = 0;
    public static int scrollY = -1;
    HorizontalScrollView hsv;
    SharedPreferences availableLevels;
    private final static int LOCKED = 0;
    private final static int UNLOCKED = 1;
    private final static int COMPLETE = 2;
    private Animation scale;
    private Animation dino;
    MediaPlayer mp = new MediaPlayer();
    SoundPool soundPool;
    int button;

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinos);
        hideSystemUI();
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);
        dino = AnimationUtils.loadAnimation(this, R.anim.dino_anim);

        hsv = findViewById(R.id.scroll_horizontal_dinos);
        ImageView arrowRight = findViewById(R.id.scroll_right_dino);
        ImageView arrowLeft = findViewById(R.id.scroll_left_dino);
        arrowRight.setVisibility(View.VISIBLE);
        View view = hsv.getChildAt(hsv.getChildCount() - 1);
        int diff = (view.getBottom() - (hsv.getWidth() + hsv.getScrollX()));
        //Log.d("MAXSCROLL", "" + diff);
        hsv.getViewTreeObserver()
            .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    int diff = (view.getRight() - (hsv.getWidth() + hsv.getScrollX()));
                    Log.d("MAXSCROLL", "" + diff);
                    int scrollX = hsv.getScrollX();
                    if (scrollX > 0 && diff != 0) {
                        arrowRight.setVisibility(View.VISIBLE);
                        arrowLeft.setVisibility(View.VISIBLE);
                    } else if (scrollX == 0) {
                        arrowRight.setVisibility(View.VISIBLE);
                        arrowLeft.setVisibility(View.INVISIBLE);
                    } else {
                        arrowRight.setVisibility(View.INVISIBLE);
                        arrowLeft.setVisibility(View.VISIBLE);
                    }

                }
            });

        buttonSound();

    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        assignDinos();
        //buttonSound();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.setVolume(0f, 0f);
        scrollX = hsv.getScrollX();
        scrollY = hsv.getScrollY();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mp.release();
        mp = null;
        soundPool.release();
        soundPool = null;

    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        removeTint();
        hsv.post(() -> hsv.scrollTo(scrollX, scrollY));
        buttonSound();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    void removeTint() {
        for (char level : Globals.levels) {
            int idImage = getResources()
                .getIdentifier("dino_" + level, "drawable", this.getPackageName());
            Drawable draw = getResources().getDrawable(idImage);
            if (availableLevels.getInt(level + "", 0) == COMPLETE) {
                draw.clearColorFilter();
            }
        }
    }

    public void assignDinos() {
        for (char level : Globals.levels) {
            int idImage;
            int idButton = getResources()
                .getIdentifier("dino_" + level, "id", this.getPackageName());
            ImageButton iB = findViewById(idButton);
            idImage = getResources()
                .getIdentifier("dino_" + level, "drawable", this.getPackageName());
            Drawable draw = getResources().getDrawable(idImage);
            if (availableLevels.getInt(level + "", 0) != COMPLETE) {
                draw.setColorFilter(ContextCompat.getColor(this, R.color.dark),
                    PorterDuff.Mode.SRC_ATOP);
            }
            iB.setImageResource(idImage);
        }
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void buttonSound() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();

        soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> Log.d("SOUNDPOOL", "COMPLETE " + button));
        button = soundPool.load(this, R.raw.button, 1);

    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    public void onClick(View view) {
        Intent intent = new Intent(this, StartActivity.class);
        switch (view.getId()) {
            case R.id.button_back:
                if (mp.isPlaying()) {
                    mp.stop();
                }

                // Sound loaded
                if (button != 0) {
                    soundPool.play(button, 1f, 1f, 1, 0, 1f);
                    //soundPool.release();
                }
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //new Handler().postDelayed(() -> startActivity(intent), 500);
                        startActivity(intent);
                    }
                });
                view.startAnimation(scale);
                break;

            case R.id.dino_a:
                if (availableLevels.getInt("a", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_a);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_b:
                if (availableLevels.getInt("b", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_b);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_e:
                if (availableLevels.getInt("e", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_e);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_f:
                if (availableLevels.getInt("f", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_f);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }

                }
                break;
            case R.id.dino_i:
                if (availableLevels.getInt("i", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_i);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_l:
                if (availableLevels.getInt("l", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_l);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_m:
                if (availableLevels.getInt("m", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_m);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_n:
                if (availableLevels.getInt("n", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_n);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_o:
                if (availableLevels.getInt("o", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_o);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_r:
                if (availableLevels.getInt("r", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_r);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_s:
                if (availableLevels.getInt("s", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_s);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
                }
                break;
            case R.id.dino_t:
                if (availableLevels.getInt("t", 0) == COMPLETE) {
                    view.startAnimation(dino);
                    if (!mp.isPlaying()) {
                        mp.reset();
                        mp = MediaPlayer.create(this, R.raw.dino_sound_t);
                        mp.setVolume(0.5f, 0.5f);
                        mp.start();
                    }
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
