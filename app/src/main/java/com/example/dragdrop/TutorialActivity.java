package com.example.dragdrop;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TutorialActivity extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {
    private final static int WINNINGNUMBER = 5;
    private ImageView animal, arrow;
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
    ArrayList<ImageView> fragments;
    ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(R.color.red, R.color.orange, R.color.yellow, R.color.green,
            R.color.blue, R.color.purple, R.color.pink));
    boolean letterClicked, animalClicked, dragCorrect, dragWrong, dragCorrectRIght, back;
    ImageButton buttonBack, buttonLetter, buttonAnimal, buttonSkip;
    private Animation scale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tutorial);
        hideSystemUI();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        // Get Level and list of animals
        init();
        /*Handler handler = new Handler();
        handler.postDelayed(this::disPlayAnimal, 500);*/
        //implementEvents();
        tutorialLetter();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        tutorialLetter();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void tutorialLetter() {
        arrow = findViewById(R.id.button_point_letter);
        buttonSkip = findViewById(R.id.button_skip);
        buttonBack = findViewById(R.id.button_back_tutorial);
        buttonLetter = findViewById(R.id.button_letter_tutorial);
        buttonAnimal = findViewById(R.id.button_animal_tutorial);
        Intent intent = new Intent(this, StartActivity.class);
        buttonSkip.setOnClickListener(v -> {
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
                    finish();
                }
            });
            buttonSkip.startAnimation(scale);
        });


        buttonBack.setEnabled(false);
        buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);

        buttonBack.setAlpha(0.3f);
        buttonAnimal.setAlpha(0.3f);
        arrow.setVisibility(View.VISIBLE);

        // 1. Introduce letter
        // TODO Play sound
        buttonLetter.setEnabled(true);

        buttonLetter.setOnClickListener(v -> {
            buttonLetter.startAnimation(scale);
            letterClicked = true;
            MediaPlayer mp = MediaPlayer.create(this, letterSound);
            mp.start();
            if (!buttonAnimal.isEnabled() && !back)
                tutorialAnimal();
        });


       /* while (!letterClicked) {
            // TODO Repeat every 10s
        }*/

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialAnimal() {
        // 2. First animal
        arrow.setVisibility(View.INVISIBLE);
        // TODO Play sound
        new Handler().postDelayed(() -> disPlayAnimal(0), 500);
        // 3. Animal sound
        // TODO Play sound
        buttonAnimal.setEnabled(true);
        new Handler().postDelayed(() -> buttonAnimal.setAlpha(1.0f), 500);
        arrow = findViewById(R.id.button_point_speaker);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        buttonAnimal.setOnClickListener(v -> {
            buttonAnimal.startAnimation(scale);
            animalClicked = true;
            MediaPlayer mp = MediaPlayer.create(this, animalSound);
            mp.start();
            /*while (mp.isPlaying()) {

            }*/
            tutorialDragCorrect();
            //TODO Play sound
        });

        // TODO Play sound incl. progress
        /*while(!animalClicked){
            // TODO Repeat every 10s
        }*/


    }

    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragCorrect() {
        // 4. Drag to submarine
        arrow.setVisibility(View.INVISIBLE);
        arrow = findViewById(R.id.button_point_submarine);
        arrow.setVisibility(View.VISIBLE);
        // TODO Play sound
        animal.setOnTouchListener(this);
        match.setOnDragListener(this);

        /*while (!dragCorrect) {
            // TODO Repeat every 10s
        }*/

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragCorrectRight() {
        // 5. Drag animal right
        arrow.setVisibility(View.INVISIBLE);

        dragCorrectRIght = true;
        new Handler().postDelayed(() -> disPlayAnimal(1), 500);
        arrow = findViewById(R.id.button_point_sign);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        // TODO Play sound
        animal.setOnTouchListener(this);
        noMatch.setOnDragListener(this);
        match.setOnDragListener(null);

        /*while (!dragCorrect) {
            // TODO Repeat every 10s
        }*/

    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialDragFalse() {
        // 6. Drag wrong
        arrow.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(() -> disPlayAnimal(1), 500);
        arrow = findViewById(R.id.button_point_submarine);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        // TODO Play sound
        animal.setOnTouchListener(this);
        match.setOnDragListener(this);
        noMatch.setOnDragListener(null);
        /*while(!dragWrong){
            // TODO Repeat every 10s
        }*/

    }

    void tutorialBack() {
        arrow.setVisibility(View.INVISIBLE);
        back = true;
        buttonAnimal.setEnabled(false);

        //new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500); // Change the time for as little as long it works

        // 7. Back button
        // TODO Play sound
        new Handler().postDelayed(() -> buttonBack.setAlpha(1.0f), 500);
        buttonBack.setEnabled(true);
        arrow = findViewById(R.id.button_point_back);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("Tutorial", true);
        buttonBack.setOnClickListener(v -> {
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
                    finish();
                }
            });
            buttonBack.startAnimation(scale);
        });


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
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);
        animal = findViewById(R.id.animal);
        match = findViewById(R.id.left);
        noMatch = findViewById(R.id.right);
        middle = findViewById(R.id.middle);

        letterSound = getResources().getIdentifier("f_sound", "raw", this.getPackageName());
        // Letter "progress bar"
        int idImage = getResources().getIdentifier("f_letter", "drawable", this.getPackageName());
        Log.d("IDIMAGENULL ", idImage + "");

        //Define a bitmap with the same size as the view

        Bitmap bm = BitmapFactory.decodeResource(getResources(), idImage);
        splitImage(bm);
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
        RelativeLayout progress = findViewById(R.id.tutorial_progress);
        int chunkSize = (int) Math.ceil(progress.getHeight() / WINNINGNUMBER);
        ImageView fragment = fragments.get(fragments.size() - step);
        /*int color = step - 1;
        if (color >= colors.size())
            color %= colors.size();*/
        //DrawableCompat.setTint(fragment.getDrawable(), ContextCompat.getColor(getApplicationContext(), colors.get(color)));

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
        RelativeLayout progress = findViewById(R.id.tutorial_progress);
        ImageView fragment = fragments.get(fragments.size() - step);
        Log.d("BITMAP", fragment.getHeight() + "");
        progress.removeView(fragment);
    }

    void changeColor(int step) {
        if (step >= colors.size())
            step %= colors.size();
        for (ImageView fragment : fragments) {
            DrawableCompat.setTint(fragment.getDrawable(), ContextCompat.getColor(getApplicationContext(), colors.get(step)));
        }
    }

    //Set position of animal image and display
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    private void disPlayAnimal(int step) {
        MediaPlayer mp;
        //animal.setOnTouchListener(this);

        Log.d("NEW ANIMAL", " METHOD CALLED");
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);

        // Correct animal
        if (step == 0) {
            animal.setImageResource(R.drawable.flughoernchen_animal);
            animalID = getResources().getIdentifier("flughoernchen_animal", "drawable", this.getPackageName());
            animalSound = R.raw.flughoernchen_sound;
            animal.setVisibility(View.VISIBLE);

            fit = true;

        } else if (step == 1) {
            animal.setImageResource(R.drawable.loewe_animal);
            animalID = getResources().getIdentifier("loewe_animal", "drawable", this.getPackageName());
            animalSound = R.raw.loewe_sound;
            animal.setVisibility(View.VISIBLE);
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
        if (dWidth > dHeight) {
            scalingFactor = vWidth / dWidth;
        } else
            scalingFactor = vHeight / dHeight;
        //Log.d("Scaling ", scalingFactor + "");
        matrix.postScale(scalingFactor, scalingFactor);

        dWidth *= scalingFactor;
        dHeight *= scalingFactor;
        // Bottom center
        matrix.postTranslate(Math.round((vWidth - dWidth) * 0.5f),
                Math.round((vHeight - dHeight)));
        animal.setImageMatrix(matrix);
        mp = MediaPlayer.create(this, animalSound);
        mp.start();
        /*animal.getLayoutParams().height = 300;

        //TODO: Remove hard coded coordinates
        // Bottom middle
        animal.setX(0);
        animal.setY(750);*/

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
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

    void drop(View view, LinearLayout container) {
        ViewGroup owner = (ViewGroup) view.getParent();
        owner.removeView(view);
        container.addView(view);

        // Avoid animal being dragged away again
        view.setOnTouchListener(null);
        //owner.removeView(animal);
        //view.setVisibility(View.INVISIBLE);
        //view.postDelayed(this::disPlayAnimal, 1000);
    }

    // This is the method that the system calls when it dispatches a drag event to the
    // listener.
    @RequiresApi(api = Build.VERSION_CODES.N)
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
                LinearLayout container = (LinearLayout) layoutview;

                // Match
                if (fit && container.getId() == match.getId() && !dragCorrectRIght) {
                    dragCorrect = true;

                    correctMatches++;
                    showProgress(correctMatches);

                    MediaPlayer mp = MediaPlayer.create(this, getResources().getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();
                    drop(view, container);
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialDragCorrectRight, 500);
                }

                // No match
                else if (!fit && container.getId() == noMatch.getId() && dragCorrectRIght) {
                    MediaPlayer mp = MediaPlayer.create(this, getResources().getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();
                    drop(view, container);
                    changeColor(0);
                    dragCorrectRIght = false;
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialDragFalse, 500);

                }
                // Wrong
                else if (container.getId() != middle.getId()) {
                    if (correctMatches > 0) {
                        removeProgress(correctMatches);
                        correctMatches--;

                    }
                    dragWrong = true;
                    MediaPlayer mp = MediaPlayer.create(this, getResources().getIdentifier("sound_negative", "raw", this.getPackageName()));
                    mp.start();
                    drop(view, container);
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialBack, 500);
                    //tutorialBack();
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


}
