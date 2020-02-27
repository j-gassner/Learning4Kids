package com.example.dragdrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private Animation scale, scaleHalf, locked;
    private Animation dino;
    MediaPlayer mp = new MediaPlayer();
    SoundPool soundPool;
    int button;
    boolean loaded;

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinos);
        hideSystemUI();
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);
        scaleHalf = AnimationUtils.loadAnimation(this, R.anim.button_inactive_anim);
        dino = AnimationUtils.loadAnimation(this, R.anim.dino_anim);
        locked = AnimationUtils.loadAnimation(this, R.anim.dino_locked_anim);

        hsv = findViewById(R.id.scroll_horizontal_dinos);
        ImageButton arrowRight = findViewById(R.id.scroll_right_dino);
        ImageButton arrowLeft = findViewById(R.id.scroll_left_dino);
        arrowRight.setVisibility(View.VISIBLE);
        View view = hsv.getChildAt(hsv.getChildCount() - 1);
        int diff = (view.getBottom() - (hsv.getWidth() + hsv.getScrollX()));
        //Log.d("MAXSCROLL", "" + diff);
        hsv.getViewTreeObserver()
            .addOnScrollChangedListener(() -> {
                int diff1 = (view.getRight() - (hsv.getWidth() + hsv.getScrollX()));
                //Log.d("MAXSCROLL", "" + diff1);
                int scrollX = hsv.getScrollX();
                if (scrollX > 0 && diff1 != 0) {
                    arrowRight.setVisibility(View.VISIBLE);
                    arrowLeft.setVisibility(View.VISIBLE);
                } else if (scrollX == 0) {
                    arrowRight.setVisibility(View.VISIBLE);
                    arrowLeft.setVisibility(View.INVISIBLE);
                } else {
                    arrowRight.setVisibility(View.INVISIBLE);
                    arrowLeft.setVisibility(View.VISIBLE);
                }

            });

        buttonSound();

    }

    void snap(boolean left) {
        LinearLayout scroll = findViewById(R.id.layout_scroll_dinos);
        int horizontalWidth = hsv.getMeasuredWidth();
        int horizontalHeight = hsv.getMeasuredHeight();
        int centerX = hsv.getScrollX() + horizontalWidth / 2;
        int centerY = horizontalHeight / 2;
        //Log.d("COORDSMIDDLE", centerX + " " + centerY);
        Rect hitRect = new Rect();
        for (int i = 0; i < scroll.getChildCount(); i++) {
            View child = scroll.getChildAt(i);
            child.getHitRect(hitRect);
            // Correct scrolling to the left
            if (left) {
                hitRect.right += 1;
            }
            //Log.d("COORDS", i + " " + hitRect.left + " " + hitRect.right);
            if (hitRect.contains(centerX, centerY)) {
                if (left) {
                    int x = (child.getRight() - (horizontalWidth / 2));
                    //Log.d("LEFT", "" + child.getRight() + " " + horizontalWidth / 2);

                    hsv.smoothScrollTo(x - (child.getWidth()), 0);
                } else {
                    int x = (child.getLeft() - (horizontalWidth / 2));
                    hsv.smoothScrollTo(x + (child.getWidth()), 0);
                }
                break;
            }
        }
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

    void playDino(int resID) {
        mp.reset();
        mp = MediaPlayer.create(this, resID);
        mp.setVolume(0.5f, 0.5f);
        mp.start();

    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void buttonSound() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();

        soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> loaded = true);
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
                if (loaded) {
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

            case R.id.scroll_right_dino:
                scaleHalf.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        snap(false);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(scaleHalf);

                return;
            case R.id.scroll_left_dino:
                scaleHalf.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        snap(true);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(scaleHalf);

                return;

            // Dino
            default:
                String name = getResources().getResourceEntryName(view.getId());
                String level = "" + name.charAt(5);
                int id = getResources().getIdentifier(name + "_sound", "raw", getPackageName());
                if (availableLevels.getInt(level, 0) == COMPLETE && !mp.isPlaying()) {
                    view.startAnimation(dino);
                    playDino(id);
                } else {
                    view.startAnimation(locked);
                }
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
