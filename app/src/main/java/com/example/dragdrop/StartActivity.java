package com.example.dragdrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        hideSystemUI();
        hsv = findViewById(R.id.scroll_horizontal);
        writeInitialLevels();
        assignButtons();



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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        switch (v.getId()) {
            // TODO Might be a bad idea
            case R.id.button_exit:
                this.finishAffinity();
                return;
            case R.id.button_reset:
                resetLevels();
                return;
            case R.id.button_unlock:
                unlockLevels();
                return;
            case R.id.button_tutorial:
                intent = new Intent(this, TutorialActivity.class);
                startActivity(intent);
                return;
            case R.id.button_museum:
                intent = new Intent(this, DinosActivity.class);
                startActivity(intent);
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
            ImageButton iB = findViewById(v.getId());

            intent.putExtra("LEVEL", level);
            startActivity(intent);
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