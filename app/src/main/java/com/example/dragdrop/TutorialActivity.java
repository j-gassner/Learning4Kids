package com.example.dragdrop;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TutorialActivity extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {
    private final static int WINNINGNUMBER = 5;
    private ImageView animal, arrow;
    private int animalSound;
    private int letterSound;
    private LinearLayout match, middle, noMatch;
    private boolean fit;
    private int correctMatches;
    private int animalID;
    ArrayList<ImageView> fragments;
    ArrayList<Integer> colors = new ArrayList<>(
        Arrays.asList(R.color.red, R.color.blue, R.color.yellow, R.color.pink,
            R.color.green, R.color.orange, R.color.purple));
    boolean letterClicked, animalClicked, dragCorrect, dragWrong, dragCorrectRight, back, skip, sound;
    ImageButton buttonBack, buttonLetter, buttonAnimal, buttonSkip;
    private Animation scale, scaleHalf;
    MediaPlayer mp = new MediaPlayer();
    Handler handleInactivity;
    Runnable runnable;
    int currentInstruction;
    static int button = 0;
    static int correct = 1;
    static int wrong = 2;
    static int[] sounds;
    boolean loaded;
    SoundPool soundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sounds = new int[3];
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tutorial);
        hideSystemUI();
        handleInactivity = new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                /*buttonLetter.setEnabled(false);*/
                /*buttonAnimal.setEnabled(false);*/

                mp.reset();
                mp = MediaPlayer.create(getApplicationContext(), currentInstruction);
                mp.setVolume(0.5f, 0.5f);
                mp.start();
                //startHandler();
                /*mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {*/
                /*    @Override*/
                /*    public void onCompletion(MediaPlayer mp) {*/
                /*        //startHandler();*/
                /*    }*/
                /*});*/
            }
        };
    }

    public void stopHandler() {
        handleInactivity.removeCallbacks(runnable);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        /*if (mp.isPlaying()) {
            mp.stop();
        }*/
        stopHandler();
        startHandler();

    }

    public void startHandler() {
        handleInactivity.postDelayed(runnable, 10000); //for 10 seconds
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        // Get Level and list of animals
        init();
        buttonSounds();
        hideSystemUI();
        /*Handler handler = new Handler();
        handler.postDelayed(this::disPlayAnimal, 500);*/
        //implementEvents();

        //new Handler().postDelayed(this::tutorialLetter, 5000);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        removeColor();
        hideSystemUI();
        tutorialIntro();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.setVolume(0f, 0f);
        /*if (mp.isPlaying()) {
            mp.stop();
        }*/
        stopHandler();
        //mp.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mp.release();
        mp = null;
        //stopHandler();
        //mp.stop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* if (mp.isPlaying()) {
            mp.stop();
        }*/
        //mp.stop();
        //stopHandler();
    }




    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void buttonSounds() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();

        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();

        soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> loaded = true);
        sounds[button] = soundPool.load(this, R.raw.button, 1);
        sounds[correct] = soundPool.load(this, R.raw.sound_positive, 1);
        sounds[wrong] = soundPool.load(this, R.raw.sound_negative, 1);
    }

    void playInstructions(int resID) {
        AssetFileDescriptor afd = getResources().openRawResourceFd(resID);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.prepareAsync();

            mp.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 500));

            //mp.setOnCompletionListener(mp -> performOnEnd());

            mp.setVolume(0.5f, 0.5f);
            mp.start();
            afd.close();

        } catch (IllegalArgumentException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void tutorialIntro() {
        arrow = findViewById(R.id.button_point_letter);
        buttonSkip = findViewById(R.id.button_skip);
        buttonBack = findViewById(R.id.button_back_tutorial);
        buttonLetter = findViewById(R.id.button_letter_tutorial);
        buttonAnimal = findViewById(R.id.button_animal_tutorial);
        Intent intent = new Intent(this, StartActivity.class);
        buttonSkip.setOnClickListener(v -> {
            buttonSkip.setEnabled(false);
            buttonLetter.setEnabled(false);
            buttonAnimal.setEnabled(false);
            buttonBack.setEnabled(false);
            skip = true;
            stopHandler();

            /*MediaPlayer bloop;
            bloop = MediaPlayer.create(this, R.raw.button);
            bloop.start();*/
            if (loaded) {
                soundPool.play(sounds[button], 1f, 1f, 1, 0, 1f);
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

                    startActivity(intent);
                    finish();
                }
            });
            buttonSkip.startAnimation(scale);
        });


        buttonBack.setEnabled(false);
        buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);

        buttonLetter.setAlpha(0.3f);
        buttonAnimal.setAlpha(0.3f);
        buttonBack.setAlpha(0.3f);

        // 1. Introduce letter

        // Intro
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.tutorial_intro);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.prepareAsync();

            mp.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 500));

            mp.setOnCompletionListener(mp -> tutorialLetter());

            afd.close();

        } catch (IllegalArgumentException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialLetter() {

        arrow.setVisibility(View.VISIBLE);
        buttonLetter.setAlpha(1.0f);
        // Letter
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.tutorial_letter);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.prepareAsync();

            mp.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 500));
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    sound = true;
                    currentInstruction = R.raw.instruction_letter;
                    buttonLetter.setEnabled(true);
                    startHandler();
                }
            });

            afd.close();

        } catch (IllegalArgumentException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialSubmarine() {
        sound = false;
        /*buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);*/
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.tutorial_submarine);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.prepareAsync();

            mp.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 1000));

            mp.setOnCompletionListener(mp -> tutorialAnimal());

            afd.close();

        } catch (IllegalArgumentException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }
    }

    void tutorialSpeaker() {
        sound = false;
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.tutorial_speaker);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.prepareAsync();

            mp.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 500));

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    sound = true;
                    buttonAnimal.setEnabled(true);
                    buttonLetter.setEnabled(true);
                    currentInstruction = R.raw.instruction_speaker;
                    startHandler();
                }
            });

            afd.close();

        } catch (IllegalArgumentException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialAnimal() {
        // 2. First animal

        new Handler().postDelayed(() -> disPlayAnimal(0), 500);
        // 3. Animal sound

        //new Handler().postDelayed(() -> tutorialSpeaker(), 500);
        //buttonAnimal.setEnabled(true);
        new Handler().postDelayed(() -> buttonAnimal.setAlpha(1.0f), 2500);
        arrow = findViewById(R.id.button_point_speaker);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 2500);
        /*buttonAnimal.setOnClickListener(v -> {
            buttonAnimal.startAnimation(scale);
            if (!animalClicked)
                stopHandler();

            if (!mp.isPlaying()) {
                mp = MediaPlayer.create(this, animalSound);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (!animalClicked) {
                            //stopHandler();
                            animalClicked = true;
                            arrow.setVisibility(View.INVISIBLE);
                            tutorialProgress();
                        }
                    }
                });
            }
        });*/

        // TODO Play sound incl. progress
        /*while(!animalClicked){
            // TODO Repeat every 10s
        }*/


    }

    void tutorialProgress() {
        /*buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);*/
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.tutorial_progress);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                tutorialFlug();
            }
        });

    }

    void tutorialFlug() {
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.tutorial_flug);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                sound = true;
                currentInstruction = R.raw.instruction_flug;
                startHandler();
                tutorialDragCorrect();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragCorrect() {
        // 4. Drag to submarine

        buttonLetter.setEnabled(true);
        buttonAnimal.setEnabled(true);
        arrow.setVisibility(View.INVISIBLE);
        arrow = findViewById(R.id.button_point_submarine);
        arrow.setVisibility(View.VISIBLE);
        // TODO Play sound
        animal.setOnTouchListener(this);
        match.setOnDragListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialColorChange() {
        /*buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);*/
        arrow.setVisibility(View.INVISIBLE);
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.tutorial_colorchange);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(mp -> disPlayAnimal(1));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialLionRight() {
        /*buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);*/
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.tutorial_loewe_correct);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                sound = true;
                currentInstruction = R.raw.instruction_loewe_correct;
                startHandler();
                tutorialDragCorrectRight();

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragCorrectRight() {
        // 5. Drag animal right

        buttonLetter.setEnabled(true);
        buttonAnimal.setEnabled(true);
        dragCorrectRight = true;
        //new Handler().postDelayed(() -> disPlayAnimal(1), 500);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialProgressLost() {
        /*buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);*/
        arrow.setVisibility(View.INVISIBLE);
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.tutorial_progress_lost);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(mp -> disPlayAnimal(1));
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialDragFalse() {
        // 6. Drag wrong


        View.OnTouchListener ot = this;
        View.OnDragListener od = this;
        //new Handler().postDelayed(() -> disPlayAnimal(1), 500);
        arrow = findViewById(R.id.button_point_submarine);
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.instruction_loewe_wrong);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                sound = true;
                currentInstruction = R.raw.instruction_loewe_wrong;
                startHandler();
                buttonLetter.setEnabled(true);
                buttonAnimal.setEnabled(true);
                arrow.setVisibility(View.VISIBLE);
                animal.setOnTouchListener(ot);
                match.setOnDragListener(od);
                noMatch.setOnDragListener(null);
            }
        });

    }


    void tutorialBack() {
        back = true;
        /*buttonAnimal.setEnabled(false);
        buttonLetter.setEnabled(false);*/

        //new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500); // Change the time for as little as long it works

        // 7. Back button
        arrow.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(() -> buttonBack.setAlpha(1.0f), 500);
        arrow = findViewById(R.id.button_point_back);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.tutorial_back);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currentInstruction = R.raw.instruction_back;
                startHandler();
                buttonBack.setEnabled(true);
                buttonLetter.setEnabled(true);
            }
        });
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("Tutorial", true);
        buttonBack.setOnClickListener(v -> {

            /*MediaPlayer bloop;
            bloop = MediaPlayer.create(this, R.raw.button);
            bloop.start();*/
            if (loaded) {
                soundPool.play(sounds[button], 1f, 1f, 1, 0, 1f);
            }

            buttonBack.setEnabled(false);
            buttonSkip.setEnabled(false);
            buttonLetter.setEnabled(false);
            buttonAnimal.setEnabled(false);
            stopHandler();
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
        scaleHalf = AnimationUtils.loadAnimation(this, R.anim.button_inactive_anim);
        animal = findViewById(R.id.animal);
        match = findViewById(R.id.left);
        noMatch = findViewById(R.id.right);
        middle = findViewById(R.id.middle);

        // Write to preferences that tutorial was started
        /*availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putBoolean("Tutorial", true);
        editor.apply();*/

        letterSound = getResources().getIdentifier("f_sound", "raw", this.getPackageName());
        animalSound = getResources().getIdentifier("flughoernchen_sound", "raw", this.getPackageName());
        // Letter "progress bar"
        int idImage = getResources().getIdentifier("f_letter_fill", "drawable", this.getPackageName());

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
        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter_tutorial);
        DrawableCompat.setTint(iB.getDrawable(), ContextCompat
            .getColor(getApplicationContext(), colors.get((step + 1) % colors.size())));
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void removeColor() {
        for (ImageView fragment : fragments) {
            //DrawableCompat.setTint(fragment.getDrawable(), ContextCompat.getColor(getApplicationContext(), colors.get(step)));
            fragment.getDrawable().setTintList(null);
        }
        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter_tutorial);
        iB.getDrawable().setTintList(null);
        //DrawableCompat.setTint(iB.getDrawable(), ContextCompat.getColor(getApplicationContext(), colors.get((step + 1) % colors.size())));
    }

    //Set position of animal image and display
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    private void disPlayAnimal(int step) {
        sound = false;
        if (step != 0 && !dragCorrectRight) {
            buttonAnimal.setEnabled(true);
            buttonLetter.setEnabled(true);
        }

        // Avoid sound being played after leaving

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


        AssetFileDescriptor afd = getResources().openRawResourceFd(animalSound);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.prepareAsync();
            mp.setVolume(0.5f, 0.5f);
            mp.setOnPreparedListener(MediaPlayer -> mp.start());

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    sound = true;
                    if (step == 0)
                        tutorialSpeaker();
                    else if (step == 1 && !dragCorrectRight)
                        tutorialLionRight();
                    else
                        tutorialDragFalse();
                }
            });

            afd.close();

        } catch (IllegalArgumentException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }



        /*animal.getLayoutParams().height = 300;

        //TODO: Remove hard coded coordinates
        // Bottom middle
        animal.setX(0);
        animal.setY(750);*/

    }


    @RequiresApi(api = VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(null, shadowBuilder, view, View.DRAG_FLAG_OPAQUE);
            //view.setVisibility(View.INVISIBLE);
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
                view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Log.d("drag", "Drag event entered into " + layoutview.toString());
                view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                Log.d("drag", "Drag event exited from " + layoutview.toString());
                view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DROP:
                //buttonAnimal.setEnabled(false);
                Log.d("drag", "Dropped");
                LinearLayout container = (LinearLayout) layoutview;

                // Match
                if (fit && container.getId() == match.getId() && !dragCorrectRight) {
                    // Animation
                    RelativeLayout progress = findViewById(R.id.tutorial_progress);
                    View letter = findViewById(R.id.button_letter_tutorial);
                    progress.startAnimation(scale);
                    letter.startAnimation(scale);

                    stopHandler();
                    dragCorrect = true;

                    correctMatches++;
                    showProgress(correctMatches);

                    /*mp = MediaPlayer.create(this, getResources().getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();*/
                    if (loaded) {
                        soundPool.play(sounds[correct], 1f, 1f, 1, 0, 1f);
                    }
                    drop(view, container);
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialColorChange, 500);
                }

                // No match
                else if (!fit && container.getId() == noMatch.getId() && dragCorrectRight) {
                    // Animation
                    RelativeLayout progress = findViewById(R.id.tutorial_progress);
                    View letter = findViewById(R.id.button_letter_tutorial);
                    progress.startAnimation(scale);
                    letter.startAnimation(scale);

                    stopHandler();
                    /*mp = MediaPlayer.create(this, getResources().getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();*/
                    if (loaded) {
                        soundPool.play(sounds[correct], 1f, 1f, 1, 0, 1f);
                    }
                    drop(view, container);
                    changeColor(0);
                    dragCorrectRight = true;
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialProgressLost, 500);

                }
                // Wrong
                else if (container.getId() != middle.getId()) {
                    // Animation
                    RelativeLayout progress = findViewById(R.id.tutorial_progress);
                    View letter = findViewById(R.id.button_letter_tutorial);
                    progress.startAnimation(scale);
                    letter.startAnimation(scale);

                    stopHandler();
                    if (correctMatches > 0) {
                        removeProgress(correctMatches);
                        correctMatches--;

                    }
                    dragWrong = true;
                    /*mp = MediaPlayer.create(this, getResources().getIdentifier("sound_negative", "raw", this.getPackageName()));
                    mp.start();*/
                    if (loaded) {
                        soundPool.play(sounds[wrong], 1f, 1f, 1, 0, 1f);
                    }
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

    public void onClick(View view) {
        // Letter button
        if (view.getId() == findViewById(R.id.button_letter_tutorial).getId()) {
            /*if (!letterClicked) {
                buttonLetter.setEnabled(false);
                stopHandler();
            }*/
            RelativeLayout progress = findViewById(R.id.tutorial_progress);

            if (!mp.isPlaying() && sound) {
                progress.startAnimation(scale);
                view.startAnimation(scale);
                sound = false;
                mp.reset();
                mp = MediaPlayer.create(this, letterSound);
                mp.setVolume(0.5f, 0.5f);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (!letterClicked) {
                            //buttonLetter.setEnabled(false);
                            //stopHandler();
                            arrow.setVisibility(View.INVISIBLE);
                            letterClicked = true;
                            tutorialSubmarine();
                        } else {
                            sound = true;
                        }

                    }
                });} else {
                progress.startAnimation(scaleHalf);
                view.startAnimation(scaleHalf);
            }

        } else if (view.getId() == findViewById(R.id.button_animal_tutorial).getId()) {

            /*if (!animalClicked) {
                buttonAnimal.setEnabled(false);
                stopHandler();
            }*/

            if (!mp.isPlaying() && sound) {
                view.startAnimation(scale);
                //buttonAnimal.startAnimation(scale);
                sound = false;
                mp.reset();
                mp = MediaPlayer.create(this, animalSound);
                mp.setVolume(0.5f, 0.5f);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (!animalClicked) {
                            //buttonAnimal.setEnabled(false);
                            //stopHandler();
                            animalClicked = true;
                            arrow.setVisibility(View.INVISIBLE);
                            tutorialProgress();
                        } else {
                            sound = true;
                        }
                    }
                });} else {
                view.startAnimation(scaleHalf);
                //buttonAnimal.startAnimation(scaleHalf);

            }


            /*if(!mp.isPlaying()) {
                mp = MediaPlayer.create(this, animalSound);
                mp.start();
            }*/
        }

        // Back button
        /*else {
            // reset animal pool because we leave the level
            Intent intent = new Intent(this, StartActivity.class);
            scale.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animalPool.reset();
                    startActivity(intent);
                    finish();
                }
            });
            view.startAnimation(scale);

        }*/
    }


}
