package com.example.dragdrop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    public static int scrollX = 0;
    public static int scrollY = -1;
    private final static int LOCKED = 0;
    private final static int UNLOCKED = 1;
    private final static int COMPLETE = 2;

    HorizontalScrollView hsv;
    private Character level;
    SharedPreferences availableLevels;
    private Animation scale;

    //ArrayList<Character> buttons = new ArrayList<>(Arrays.asList('f', 'l', 'r', 'm', 'n', 'i', 'e', 'a', 'o', 's', 'b', 't'));

    void writeInitialLevels() {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        if (!availableLevels.contains("f")) {
            editor.putInt("f", UNLOCKED);
            editor.putInt("l", LOCKED);
            editor.putInt("r", LOCKED);
            editor.putInt("m", LOCKED);
            editor.putInt("n", LOCKED);
            editor.putInt("i", LOCKED);
            editor.putInt("e", LOCKED);
            editor.putInt("a", LOCKED);
            editor.putInt("o", LOCKED);
            editor.putInt("s", LOCKED);
            editor.putInt("b", LOCKED);
            editor.putInt("t", LOCKED);
            editor.apply();
        }

        // Start tutorial on first start
        /*if(!availableLevels.contains("Tutorial")) {
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
        }*/
    }

    void resetLevels() {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        editor.putInt("f", UNLOCKED);
        editor.putInt("l", LOCKED);
        editor.putInt("r", LOCKED);
        editor.putInt("m", LOCKED);
        editor.putInt("n", LOCKED);
        editor.putInt("i", LOCKED);
        editor.putInt("e", LOCKED);
        editor.putInt("a", LOCKED);
        editor.putInt("o", LOCKED);
        editor.putInt("s", LOCKED);
        editor.putInt("b", LOCKED);
        editor.putInt("t", LOCKED);
        editor.apply();
        assignButtons();

    }

    void unlockLevels() {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        editor.putInt("f", COMPLETE);
        editor.putInt("l", COMPLETE);
        editor.putInt("r", COMPLETE);
        editor.putInt("m", COMPLETE);
        editor.putInt("n", COMPLETE);
        editor.putInt("i", COMPLETE);
        editor.putInt("e", COMPLETE);
        editor.putInt("a", COMPLETE);
        editor.putInt("o", COMPLETE);
        editor.putInt("s", COMPLETE);
        editor.putInt("b", COMPLETE);
        editor.putInt("t", COMPLETE);
        editor.apply();
        assignButtons();

    }


    void tutorialMuseum() {

        // Disable buttons
        findViewById(R.id.button_reset).setEnabled(false);
        findViewById(R.id.button_tutorial).setEnabled(false);
        findViewById(R.id.button_exit).setEnabled(false);
        findViewById(R.id.button_f).setEnabled(false);
        findViewById(R.id.button_museum).setEnabled(false);

        // 7. Explain museum
        ImageView arrow = findViewById(R.id.button_point_museum);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);

        // TODO Text audio

        findViewById(R.id.button_reset).setEnabled(true);
        findViewById(R.id.button_tutorial).setEnabled(true);
        findViewById(R.id.button_exit).setEnabled(true);
        findViewById(R.id.button_f).setEnabled(true);
        findViewById(R.id.button_museum).setEnabled(true);
        new Handler().postDelayed(() -> arrow.setVisibility(View.INVISIBLE), 1000);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        hideSystemUI();
        hsv = findViewById(R.id.scroll_horizontal);
        writeInitialLevels();
        assignButtons();

        Intent intent = getIntent();
        boolean tutorial = intent.getBooleanExtra("Tutorial", false);
        if (tutorial) {
            tutorialMuseum();
        }

        /*Globals globals =(Globals) getApplication();
        this.animalPool = globals.getAnimalPool();*/

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
        hsv.post(() -> hsv.scrollTo(scrollX, scrollY));
    }


    public void assignButtons() {
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);

        //char[] buttons = {'f', 'l', 'r', 'm', 'n', 'i', 'e', 'a', 'o', 's', 'b', 't'};
        for (char button : Globals.levels) {
            int idImage;
            int idButton = getResources().getIdentifier("button_" + button, "id", this.getPackageName());
            Log.d("IDBUTTON", " " + idButton);
            ImageButton iB = findViewById(idButton);
            if (availableLevels.getInt(button + "", 0) == COMPLETE) {

                idImage = getResources().getIdentifier(button + "_polaroid", "drawable", this.getPackageName());
                Log.d("IDIMAGE", " " + idImage);

            } else if (availableLevels.getInt(button + "", 0) == UNLOCKED) {

                idImage = getResources().getIdentifier(button + "_polaroid_unlocked", "drawable", this.getPackageName());
                Log.d("IDIMAGE", " " + idImage);
            } else {
                idImage = getResources().getIdentifier(button + "_polaroid_locked", "drawable", this.getPackageName());
                Log.d("IDIMAGE", " " + idImage);
            }
            iB.setImageResource(idImage);
            //findViewById(idButton).setBackgroundResource(idImage);

        }
    }

    void alertDialogue(){
        int ui_flags =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Alles zurücksetzen?")
                .setPositiveButton("Ja", (dialog, id) -> resetLevels())
                .setNegativeButton("Nein", (dialog, id) -> {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel();
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().
                setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        // Set full-sreen mode (immersive sticky):
        alertDialog.getWindow().getDecorView().setSystemUiVisibility(ui_flags);
        // Show the alertDialog:
        alertDialog.show();
        // Set dialog focusable so we can avoid touching outside:
        alertDialog.getWindow().
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // TODO Might be a bad idea
            case R.id.button_exit:
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        finishAffinity();
                    }
                });
                view.startAnimation(scale);
                return;
            case R.id.button_reset:
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        alertDialogue();
                    }
                });
                view.startAnimation(scale);
                return;

            case R.id.button_unlock:
                unlockLevels();
                return;
            case R.id.button_tutorial:
                Intent intentTut = new Intent(this, TutorialActivity.class);
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startActivity(intentTut);
                    }
                });
                view.startAnimation(scale);
                return;

            case R.id.button_museum:
                Intent intentDino = new Intent(this, DinosActivity.class);
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startActivity(intentDino);
                    }
                });
                view.startAnimation(scale);
                return;

            case R.id.button_a:
                level = 'a';
                break;
            case R.id.button_b:
                level = 'b';
                break;
            case R.id.button_e:
                level = 'e';
                break;
            case R.id.button_f:
                level = 'f';
                break;
            case R.id.button_i:
                level = 'i';
                break;
            case R.id.button_l:
                level = 'l';
                break;
            case R.id.button_m:
                level = 'm';
                break;
            case R.id.button_n:
                level = 'n';
                break;
            case R.id.button_o:
                level = 'o';
                break;
            case R.id.button_r:
                level = 'r';
                break;
            case R.id.button_s:
                level = 's';
                break;
            case R.id.button_t:
                level = 't';
                break;
            default:
                break;

        }
        //Log.d("LEVEL ", level.toString());
        /*extras.putChar("LEVEL", level);
        extras.putSerializable("animalPool", animalPool);
        intent.putExtras(extras);*/


        // Only playable levels
        if (availableLevels.getInt(level.toString(), 0) != LOCKED) {
            ImageButton iB = findViewById(view.getId());
            iB.startAnimation(scale);
            Intent intent = new Intent(this, GameActivity.class);

            intent.putExtra("LEVEL", level);

            scale.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    startActivity(intent);
                }
            });
            view.startAnimation(scale);


        }
        //intent.putExtra("animalPool", animalPool);*/

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