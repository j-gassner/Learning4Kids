package com.example.dragdrop;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener, View.OnClickListener {
    private final static int WINNINGNUMBER = 5;
    private ImageView animal;
    private int animalSound;
    private int letterSound;
    private ImageView background;
    private LinearLayout match, middle, noMatch;
    private Animals animalPool = new Animals();
    private Character level;
    private LevelCollection levels;
    private boolean fit;
    private int correctMatches;
    private static Random rand = new Random();
    private SharedPreferences availableLevels;
    private Globals globals;
    private int animalID;
    private int step;
    ArrayList<ImageView> fragments;
    ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(R.color.red, R.color.orange, R.color.yellow, R.color.green,
            R.color.blue, R.color.purple, R.color.pink));

    private final static int LOCKED = 0;
    private final static int UNLOCKED = 1;
    private final static int COMPLETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);
        hideSystemUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get Level and list of animals
        Intent intent = getIntent();
        level = intent.getCharExtra("LEVEL", 'f');
        Log.d("LEVEL", level.toString());
        globals = (Globals) getApplication();
        animalPool = globals.getAnimalPool();
        init();
        Handler handler = new Handler();
        handler.postDelayed(this::disPlayAnimal, 500);
        implementEvents();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
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

    protected void init() {
        //assignAnimals();
        levels = new LevelCollection(this);
        animal = findViewById(R.id.animal);
        background = findViewById(R.id.level_background);
        background.setImageResource(levels.getLevel(level).getBackgroundID());
        //background.setBackgroundResource(levels.getLevel(level).getBackgroundID());
        findViewById(R.id.button_animal).setEnabled(true);

        if (levels.getLevel(level).getIsLeft()) {
            match = findViewById(R.id.left);
            noMatch = findViewById(R.id.right);
        } else {
            match = findViewById(R.id.right);
            noMatch = findViewById(R.id.left);
        }
        middle = findViewById(R.id.middle);
        setLetterButton();
    }

    void setLetterButton() {
        letterSound = getResources().getIdentifier(level + "_sound", "raw", this.getPackageName());
        ImageButton iB = findViewById(R.id.button_letter);
        int idImage = getResources().getIdentifier(level + "_letter_outline", "drawable", this.getPackageName());
        iB.setImageResource(idImage);
        iB.setScaleType(ImageView.ScaleType.FIT_CENTER);


        // Letter "progress bar"
        idImage = getResources().getIdentifier(level + "_letter", "drawable", this.getPackageName());
        Log.d("IDIMAGENULL ", idImage + "");

        //Define a bitmap with the same size as the view

        Bitmap bm = BitmapFactory.decodeResource(getResources(), idImage);
        splitImage(bm);


    }

    void writePreferences(Character level, int mode) {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putInt(level.toString(), mode);
        editor.apply();

    }

    // Cut image horizontally
    void splitImage(Bitmap image) {
        fragments = new ArrayList<>(WINNINGNUMBER);
        int y = 0;
        Log.d("HEIGHTORIG", image.getHeight() + "");
        for (int i = 0; i < WINNINGNUMBER; i++) {
            ImageView frag = new ImageView(getApplicationContext());
            frag.setImageBitmap(Bitmap.createBitmap(image, 0, y, image.getWidth(), (int) Math.ceil(image.getHeight() / WINNINGNUMBER)));
            fragments.add(frag);
            y += (int) Math.ceil(image.getHeight() / WINNINGNUMBER);
        }

    }


    void showProgress(int step) {
        RelativeLayout progress = findViewById(R.id.image_progress);
        int chunkSize = (int) Math.ceil(progress.getHeight() / WINNINGNUMBER);
        ImageView fragment = fragments.get(fragments.size() - step);
        /*int color = step - 1;
        if(color >= colors.size())
            color %= colors.size();
        DrawableCompat.setTint(fragment.getDrawable(), ContextCompat.getColor(getApplicationContext(), colors.get(color)));
*/
        Log.d("BITMAP", fragment.getHeight() + "");
        //ImageView frag = new ImageView(getApplicationContext());
        //frag.setImageBitmap(fragment);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(progress.getWidth(), (int) Math.ceil(progress.getHeight() / WINNINGNUMBER));
        params.leftMargin = 0;
        params.topMargin = progress.getHeight() - chunkSize * step;
        progress.addView(fragment, params);

        fragment.setScaleType(ImageView.ScaleType.FIT_CENTER);

    }

    void removeProgress(int step) {
        if (step == 0)
            return;
        RelativeLayout progress = findViewById(R.id.image_progress);
        ImageView fragment = fragments.get(fragments.size() - step);
        Log.d("BITMAP", fragment.getHeight() + "");
        progress.removeView(fragment);
    }

    //Set position of animal image and display
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    private void disPlayAnimal() {
        MediaPlayer mp;
        animal.setOnTouchListener(this);

        // Level done
        if (correctMatches == WINNINGNUMBER) {

            findViewById(R.id.button_animal).setEnabled(false);
            //animal.setAlpha(0.0f);
            mp = MediaPlayer.create(this, R.raw.complete_sound);
            mp.start();

            // TODO Ugly
            while (mp.isPlaying()) {
            }
            availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);;
            if(availableLevels.getInt(level.toString(), 0) == UNLOCKED)
                displayDino();
            globals.getAnimalPool().reset();
            writePreferences(level, COMPLETE);
            if (level != 't') {
                int index = globals.getLevels().indexOf(level);
                Character nextLevel = globals.getLevels().get(index + 1);
                if (availableLevels.getInt(nextLevel.toString(), 0) == LOCKED)
                    writePreferences(nextLevel, UNLOCKED);
            }


            Intent intent = new Intent(this, StartActivity.class);
            Handler handler = new Handler();
            handler.postDelayed(() -> startActivity(intent), 5000);   //5 seconds

            return;
        }
        Log.d("NEW ANIMAL", " METHOD CALLED");
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);


        // n% chance correct letter
        if (rand.nextInt(100) < levels.getLevel(level).getDifficulty() ) {
            animalID = animalPool.getAnimal(level);
            animal.setImageResource(animalID);
            //animal.setScaleType(ImageView.ScaleType.FIT_END);

            // Sound
            String name = getResources().getResourceEntryName(animalID);
            Log.d("Sound name ", name);
            name = name.replace("animal", "");
            Log.d("Sound name ", name);
            animalSound = getResources().getIdentifier(name + "sound", "raw", this.getPackageName());
            mp = MediaPlayer.create(this, animalSound);
            mp.start();

            fit = true;
        } else {
            animalID = animalPool.getDistractorAnimal(level);
            animal.setImageResource(animalID);
            //animal.setScaleType(ImageView.ScaleType.FIT_END);

            // Sound
            String name = getResources().getResourceEntryName(animalID);
            Log.d("Sound name ", name);
            name = name.replace("animal", "");
            Log.d("Sound name ", name);
            animalSound = getResources().getIdentifier(name + "sound", "raw", this.getPackageName());
            mp = MediaPlayer.create(this, animalSound);
            mp.start();
            fit = false;
        }

        // Center ImageView
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) animal.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        animal.setLayoutParams(params);

        // Set image position inside ImageView
        Matrix matrix = animal.getImageMatrix();
        matrix.reset();
        Drawable draw = getResources().getDrawable(animalID, getTheme());
        float vWidth = animal.getWidth();
        float vHeight = animal.getHeight();
        float dWidth = draw.getIntrinsicWidth();
        float dHeight = draw.getIntrinsicHeight();

        float scalingFactor;
        if(dWidth > dHeight){
            scalingFactor = vWidth / dWidth;
        }
        else
            scalingFactor = vHeight / dHeight;
        //Log.d("Scaling ", scalingFactor + "");
        matrix.postScale(scalingFactor, scalingFactor);

        dWidth *= scalingFactor;
        dHeight *= scalingFactor;
        // Bottom center
        matrix.postTranslate(Math.round((vWidth - dWidth) * 0.5f),
                Math.round((vHeight - dHeight)));
        animal.setImageMatrix(matrix);


        //TODO: Remove hard coded coordinates
        // Bottom middle
        /*animal.setX(0);
        animal.setY(100);*/

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void displayDino(){
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        //middle.addView(animal);
        LinearLayout dino = findViewById(R.id.layout_top);
        dino.addView(animal);
        animal.setVisibility(View.VISIBLE);

        animalID = getResources().getIdentifier("dino_" + level, "drawable", getPackageName());
        animal.setImageResource(animalID);

        // Set image position inside ImageView
        Matrix matrix = animal.getImageMatrix();
        matrix.reset();
        Drawable draw = getResources().getDrawable(animalID, getTheme());
        float vWidth = animal.getWidth();
        float vHeight = animal.getHeight();
        float dWidth = draw.getIntrinsicWidth();
        float dHeight = draw.getIntrinsicHeight();

        float scalingFactor;
        if(dWidth > dHeight){
            scalingFactor = vWidth / dWidth;
        }
        else
            scalingFactor = vHeight / dHeight;
        //Log.d("Scaling ", scalingFactor + "");
        matrix.postScale(scalingFactor, scalingFactor);

        dWidth *= scalingFactor;
        dHeight *= scalingFactor;
        // Bottom center
        matrix.postTranslate(Math.round((vWidth - dWidth) * 0.5f),
                Math.round((vHeight - dHeight)));
        animal.setImageMatrix(matrix);

        //TODO: Remove hard coded coordinates
        // Bottom middle
        /*animal.setX(0);
        animal.setY(0);*/

        animal.setY(animal.getY() - 100);
        animal.setAnimation(AnimationUtils.loadAnimation(this,R.anim.zoom));
    }

    //Implement long click and drag listener

    @SuppressLint("ClickableViewAccessibility")
    private void implementEvents() {
        //add or remove any view that you don't want to be dragged
        animal.setOnTouchListener(this);

        //add or remove any layout view that you don't want to accept dragged view
        match.setOnDragListener(this);
        middle.setOnDragListener(this);
        noMatch.setOnDragListener(this);

    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                view.startDragAndDrop(null, shadowBuilder, view, View.DRAG_FLAG_OPAQUE);
            else
                view.startDrag(null, shadowBuilder, view, View.DRAG_FLAG_OPAQUE);
            view.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }

    }

    void drop(View view, LinearLayout container){
        ViewGroup owner = (ViewGroup) view.getParent();
        owner.removeView(view);
        container.addView(view);

        // Avoid animal being dragged away again
        view.setOnTouchListener(null);
        view.postDelayed(this::disPlayAnimal, 1000);
    }

    void changeColor(){
        if(step >= colors.size())
            step %= colors.size();
        for(ImageView fragment : fragments){
            DrawableCompat.setTint(fragment.getDrawable(), ContextCompat.getColor(getApplicationContext(), colors.get(step)));
        }
    }

    // This is the method that the system calls when it dispatches a drag event to the
    // listener.
    @Override
    public boolean onDrag(View layoutview, DragEvent dragevent) {
        //ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        int action = dragevent.getAction();
        View view = (View) dragevent.getLocalState();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                Log.d("drag", "Drag event started");
                //view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Log.d("drag", "Drag event entered into " + layoutview.toString());
                //view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                Log.d("drag", "Drag event exited from " + layoutview.toString());
                //view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DROP:
                Log.d("drag", "Dropped");
                /*ViewGroup owner = (ViewGroup) view.getParent();
                owner.removeView(view);
                LinearLayout container = (LinearLayout) layoutview;

                container.addView(view);
                view.setVisibility(View.VISIBLE);
                break;*/

                LinearLayout container = (LinearLayout) layoutview;

                // Match
                if (fit && container.getId() == match.getId()) {
                    animalPool.getAnimalMapCurrent().get(level).remove(new Integer(animalID));

                    correctMatches++;

                    // True positive
                    globals.getStatistics().addToStatistics(animalID, 0);
                    showProgress(correctMatches);

                    MediaPlayer mp = MediaPlayer.create(this, getResources().getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();
                    drop(view, container);
                    /*ViewGroup owner = (ViewGroup) view.getParent();
                    owner.removeView(view);
                    container.addView(view);
                    //view.setVisibility(View.VISIBLE);

                    // Avoid animal being dragged away again
                    view.setOnTouchListener(null);

                    *//*FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) cover.getLayoutParams();
                    params.height -= 24;
                    cover.setLayoutParams(params);*//*
                    view.postDelayed(this::disPlayAnimal, 1000);*/

                }

                // No Match
                else if (!fit && container.getId() == noMatch.getId()) {
                    step++;
                    Character lvl = getResources().getResourceEntryName(animalID).charAt(0);
                    animalPool.getAnimalMapCurrent().get(lvl).remove(new Integer(animalID));

                    // True negative
                    globals.getStatistics().addToStatistics(animalID, 1);
                    /*correctMatches++;
                    showProgress(correctMatches);*/
                    MediaPlayer mp = MediaPlayer.create(this, getResources().getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();
                    changeColor();
                    drop(view, container);

                }

                // Wrong
                else if (container.getId() != middle.getId()){
                    if (correctMatches > 0) {
                        removeProgress(correctMatches);
                        correctMatches--;
                    }

                    // False positive
                    if(!fit && container.getId() == match.getId())
                        globals.getStatistics().addToStatistics(animalID, 2);
                    else
                        // False negative
                        globals.getStatistics().addToStatistics(animalID, 3);
                    MediaPlayer mp = MediaPlayer.create(this, getResources().getIdentifier("sound_negative", "raw", this.getPackageName()));
                    mp.start();
                    drop(view, container);
                }

                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d("drag", "Drag ended");
                view.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }

        return true;
    }


    public void onClick(View view) {
        // Letter button
        if (view.getId() == findViewById(R.id.button_letter).getId()) {
            //letterSound =  getResources().getIdentifier(level + "_sound", "raw", this.getPackageName());
            MediaPlayer mp;
            mp = MediaPlayer.create(this, letterSound);
            mp.start();
        } else if (view.getId() == findViewById(R.id.button_animal).getId()) {
            MediaPlayer mp;
            mp = MediaPlayer.create(this, animalSound);
            mp.start();
        }

        // Back button
        else {
            // reset anmial pool because we leave the level
            animalPool.reset();
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
    }


}
