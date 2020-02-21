package com.example.dragdrop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener,
    View.OnDragListener, View.OnClickListener {

    private int WINNINGNUMBER = 5;
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
    MediaPlayer mp = new MediaPlayer();
    private Animation scale, scaleHalf;
    Animation zoom, flash;
    boolean stillThere, isRunning;
    Handler handleInactivity;
    Runnable runnable;
    ArrayList<ImageView> fragments;
    ArrayList<Integer> colors = new ArrayList<>(
        Arrays.asList(R.color.red, R.color.blue, R.color.yellow, R.color.pink,
            R.color.green, R.color.orange, R.color.purple));
    int counterCorrect, counterWrong;
    static int[] sounds;
    static SoundPool soundPool;
    boolean loaded;
    static int back = 0;
    static int camera = 1;
    static int correct = 2;
    static int wrong = 3;
    static int streamID;
    boolean sound;
    boolean test, backPressed;

    // 30s
    CountDownTimer mCountDown = new CountDownTimer(30000, 30000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            test = true;
            if (stillThere && !backPressed) {
                //mph.start();
                mp.reset();
                mp = MediaPlayer.create(getApplicationContext(), R.raw.are_you_still_there);
                mp.setVolume(0.5f, 0.5f);
                mp.start();
                /*if (loaded) {
                    streamID = soundPool.play(sounds[still], 1f, 1f, 1, 0, 1f);
                }*/
                stillThere = false;
            } else if (!backPressed) {
                mp.reset();
                mp = MediaPlayer.create(getApplicationContext(), getResources()
                    .getIdentifier("instruction_" + level, "raw", getPackageName()));
                mp.setVolume(0.5f, 0.5f);
                mp.start();
                //instr.start();
                //streamID = soundPool.play(sounds[instruction], 1f, 1f, 1, 0, 1f);
                stillThere = true;
            }
            mCountDown.cancel();
            mCountDown.start();
        }
    };

    private final static int LOCKED = 0;
    private final static int UNLOCKED = 1;
    private final static int COMPLETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);
        hideSystemUI();
        sounds = new int[4];

    }


    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        removeColor();
        //mCountDown.start();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        //if(mCountDown)
        /*if (mph.isPlaying()) {
            mph.stop();
        }
        if (instr.isPlaying()) {
            instr.stop();
        }*/
        if (mp.isPlaying() && test) {
            mp.stop();
            test = false;
        }
        soundPool.stop(streamID);
        if (isRunning) {
            mCountDown.cancel();
            mCountDown.start();
        }

    }


    /*@Override
    public void onUserInteraction(){
        stopHandler();
        startHandler();
    }*/

    /*public void stopHandler() {
        if (mph.isPlaying()) {
            mph.stop();
            mph.release();
        }
        handleInactivity.removeCallbacks(runnable);
    }

    public void startHandler() {
        handleInactivity.postDelayed(runnable, 10000); //for 30 seconds
    }*/

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        //noTouchy();
        // Get Level and list of animals
        Intent intent = getIntent();
        level = intent.getCharExtra("LEVEL", 'f');
        Log.d("LEVEL", level.toString());
        globals = (Globals) getApplication();
        animalPool = globals.getAnimalPool();
        init();

        implementEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCountDown.cancel();
        /*if(mp.isPlaying()){
            mp.stop();
            mp.release();
            mp = null;
        }*/
        /*if(prepared) {
            mp.release();
            mp = null;
        }*/
        mp.setVolume(0f, 0f);

    }

    @Override
    protected void onStop() {
        super.onStop();

        mCountDown.cancel();
        mp.release();
        mp = null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCountDown.cancel();
        /*if(mp.isPlaying()){
            mp.stop();
            mp.release();
            mp = null;
        }*/

    }


    void noTouchy() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void touchy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    protected void init() {
        mp.setVolume(0.5f, 0.5f);
        buttonSounds();
/*
        mph = MediaPlayer.create(this, R.raw.are_you_still_there);
*/

        ImageButton letterButton = findViewById(R.id.button_letter);
        ImageButton speaker = findViewById(R.id.button_animal);
        //findViewById(R.id.button_back).setEnabled(false);
        //new Handler().postDelayed(() -> findViewById(R.id.button_back).setEnabled(true), 100);
        /*letterButton.setEnabled(false);
        speaker.setEnabled(false);*/
        int intro = getResources()
            .getIdentifier("instruction_" + level, "raw", this.getPackageName());
        //instr = MediaPlayer.create(this, intro);
        //assignAnimals();
        flash = AnimationUtils.loadAnimation(this, R.anim.flash);
        zoom = AnimationUtils.loadAnimation(this, R.anim.zoom);
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);
        scaleHalf = AnimationUtils.loadAnimation(this, R.anim.button_inactive_anim);
        levels = new LevelCollection(this);
        animal = findViewById(R.id.animal);
        background = findViewById(R.id.level_background);
        background.setImageResource(levels.getLevel(level).getBackgroundID());
        WINNINGNUMBER = levels.getLevel(level).getWinningNumber();
        //background.setBackgroundResource(levels.getLevel(level).getBackgroundID());
        //findViewById(R.id.button_animal).setEnabled(true);

        if (levels.getLevel(level).getIsLeft()) {
            match = findViewById(R.id.left);
            noMatch = findViewById(R.id.right);
        } else {
            match = findViewById(R.id.right);
            noMatch = findViewById(R.id.left);
        }
        middle = findViewById(R.id.middle);
        setLetterButton();

        // Intro
        AssetFileDescriptor afd = getResources().openRawResourceFd(intro);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                afd.getDeclaredLength());
            mp.prepareAsync();

            //mp.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 500));
            mp.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //prepared = true;
                    new Handler().postDelayed(mp::start, 500);

                }
            });
            //new Handler().postDelayed(() -> findViewById(R.id.button_back).setEnabled(true), 500);

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //letterButton.setEnabled(true);
                    //speaker.setEnabled(true);
                    //mp.reset();
                    new Handler().postDelayed(() -> disPlayAnimal(), 500);
                    /*startHandler();*/
                    mCountDown.start();
                    isRunning = true;
                    //touchy();
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

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void buttonSounds() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();

        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();

        soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> loaded = true);
        sounds[back] = soundPool.load(this, R.raw.button, 1);
        sounds[camera] = soundPool.load(this, R.raw.camera, 1);
        sounds[correct] = soundPool.load(this, R.raw.sound_positive, 1);
        sounds[wrong] = soundPool.load(this, R.raw.sound_negative, 1);

    }

    void setLetterButton() {
        /*letterSound = getResources().getIdentifier(level + "_sound", "raw", this.getPackageName());
        loaded = false;
        sounds[currentLetter] = soundPool.load(this, letterSound, 1);*/
        ImageButton iB = findViewById(R.id.button_letter);
        int idImage = getResources()
            .getIdentifier(level + "_letter_outline", "drawable", this.getPackageName());
        iB.setImageResource(idImage);
        iB.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Letter "progress bar"
        idImage = getResources()
            .getIdentifier(level + "_letter_fill", "drawable", this.getPackageName());
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
            frag.setImageBitmap(Bitmap.createBitmap(image, 0, y, image.getWidth(),
                (int) (Math.ceil(image.getHeight() / WINNINGNUMBER))));
            fragments.add(frag);

            // Avoid lines during animation
            y += (int) (image.getHeight() / WINNINGNUMBER);
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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(progress.getWidth(),
            (int) Math.ceil(progress.getHeight() / WINNINGNUMBER));
        params.leftMargin = 0;
        params.topMargin = progress.getHeight() - chunkSize * step;
        progress.addView(fragment, params);

        fragment.setScaleType(ImageView.ScaleType.FIT_CENTER);

    }

    void removeProgress(int step) {
        if (step == 0) {
            return;
        }
        RelativeLayout progress = findViewById(R.id.image_progress);
        ImageView fragment = fragments.get(fragments.size() - step);
        Log.d("BITMAP", fragment.getHeight() + "");
        progress.removeView(fragment);
    }

    public void cameraFlash() {
        // White
        ImageView layover = findViewById(R.id.flash);
        new Handler().postDelayed(() -> layover.setVisibility(View.VISIBLE), 500);
        //mp = MediaPlayer.create(this, R.raw.camera);

        flash.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //mp.start();
                if (loaded) {
                    soundPool.play(sounds[camera], 1f, 1f, 1, 0, 1f);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @RequiresApi(api = VERSION_CODES.JELLY_BEAN)
            @Override
            public void onAnimationEnd(Animation animation) {
                layover.setBackground(null);
            }
        });
        layover.startAnimation(flash);

        //mp.start();

        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        if (availableLevels.getInt(level.toString(), 0) == UNLOCKED) {
            Handler handler = new Handler();
            handler.postDelayed(this::displayDino, 1000);
            return;
        }
        leaveLevel();

    }

    //Set position of animal image and display
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    private void disPlayAnimal() {
        boolean dino = false;

        animal.setOnTouchListener(this);
        // Level done
        if (correctMatches == WINNINGNUMBER) {
            sound = false;
            //noTouchy();
            mCountDown.cancel();
            //handleInactivity.removeCallbacks(runnable);
            //findViewById(R.id.button_back).setEnabled(false);
            /*findViewById(R.id.button_animal).setEnabled(false);
            findViewById(R.id.button_letter).setEnabled(false);*/
            mp.reset();
            mp = MediaPlayer.create(this, R.raw.complete_sound);
            mp.setVolume(0.5f, 0.5f);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //mp.reset();
                    //mp = null;
                    cameraFlash();

                }
            });
            // TODO Replace with listener + release
            /*while (mp.isPlaying()) {
            }

            // White
            ImageView layover = findViewById(R.id.flash);
            new Handler().postDelayed(() -> layover.setVisibility(View.VISIBLE), 500);
            //mp = MediaPlayer.create(this, R.raw.camera);

            flash.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //mp.start();
                    if (loaded) {
                        soundPool.play(sounds[camera], 1f, 1f, 1, 0, 1f);
                    }

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    layover.setBackground(null);
                }
            });
            layover.startAnimation(flash);

            //mp.start();

            availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
            if (availableLevels.getInt(level.toString(), 0) == UNLOCKED) {
                dino = true;
                Handler handler = new Handler();
                handler.postDelayed(this::displayDino, 1000);
                return;
            }
            leaveLevel();*/
            return;
        }
        Log.d("NEW ANIMAL", " METHOD CALLED");

        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);

        // n% chance correct letter
        if (rand.nextInt(100) < levels.getLevel(level).getDifficulty()) {
            animalID = animalPool.getAnimal(level);
            animal.setImageResource(animalID);
            //animal.setScaleType(ImageView.ScaleType.FIT_END);

            // Sound
            String name = getResources().getResourceEntryName(animalID);
            Log.d("Sound name ", name);
            name = name.replace("animal", "");
            Log.d("Sound name ", name);
            animalSound = getResources()
                .getIdentifier(name + "sound", "raw", this.getPackageName());
            findViewById(R.id.button_animal).setEnabled(true);

            /*mp = MediaPlayer.create(this, animalSound);

            mp.start();*/

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
            animalSound = getResources()
                .getIdentifier(name + "sound", "raw", this.getPackageName());
            findViewById(R.id.button_animal).setEnabled(true);
            /*mp = MediaPlayer.create(this, animalSound);
            mp.start();*/
            fit = false;
        }

        mp.reset();
        mp = MediaPlayer.create(this, animalSound);
        if (!backPressed) {
            mp.setVolume(0.5f, 0.5f);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    sound = true;
                /*findViewById(R.id.button_letter).setEnabled(true);
                findViewById(R.id.button_animal).setEnabled(true);*/
                }
            });
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
        } else {
            scalingFactor = vHeight / dHeight;
        }
        //Log.d("Scaling ", scalingFactor + "");
        matrix.postScale(scalingFactor, scalingFactor);

        dWidth *= scalingFactor;
        dHeight *= scalingFactor;
        // Bottom center
        matrix.postTranslate(Math.round((vWidth - dWidth) * 0.5f),
            Math.round((vHeight - dHeight)));
        animal.setImageMatrix(matrix);
        //new Handler().postDelayed(this::touchy, 100);
        //touchy();

    }

    void leaveLevel() {
        //mp.release();
        //mp = null;
        //stopHandler();
        globals.getAnimalPool().reset();
        writePreferences(level, COMPLETE);
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putBoolean("Tutorial", true);
        editor.apply();
        if (level != 't') {
            int index = globals.getLevels().indexOf(level);
            Character nextLevel = globals.getLevels().get(index + 1);
            if (availableLevels.getInt(nextLevel.toString(), 0) == LOCKED) {
                writePreferences(nextLevel, UNLOCKED);
            }
        }

        Intent intent = new Intent(this, StartActivity.class);

        /*if(dino) {
            Handler handler = new Handler();
            handler.postDelayed(() -> startActivity(intent), 5000);
            handler.postDelayed(() -> finish(), 5000);
        }
        else{*/
        Handler handler = new Handler();
        handler.postDelayed(() -> startActivity(intent), 1000);
        handler.postDelayed(() -> finish(), 1000);
        //startActivity(intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void displayDino() {
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        LinearLayout dino = findViewById(R.id.layout_top);
        dino.addView(animal);

        animal.setVisibility(View.VISIBLE);

        animalID = getResources().getIdentifier("dino_" + level, "drawable", getPackageName());
        animal.setImageResource(animalID);

        // Set image position inside ImageView
        Matrix matrix = animal.getImageMatrix();
        matrix.reset();
        Drawable draw = getResources().getDrawable(animalID, getTheme());
        draw.clearColorFilter();
        float vWidth = animal.getWidth();
        float vHeight = animal.getHeight();
        float dWidth = draw.getIntrinsicWidth();
        float dHeight = draw.getIntrinsicHeight();

        float scalingFactor;
        if (dWidth > dHeight) {
            scalingFactor = vWidth / dWidth;
        } else {
            scalingFactor = vHeight / dHeight;
        }
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
        //animal.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom));
        String packageName = this.getPackageName();
        mp.reset();
        mp = MediaPlayer
            .create(this, getResources().getIdentifier("museum_" + level, "raw", packageName));
        mp.setVolume(0.5f, 0.5f);
        zoom.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        leaveLevel();

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });
        animal.startAnimation(zoom);
    }

    //Implement long click and drag listener

    @SuppressLint("ClickableViewAccessibility")
    private void implementEvents() {
        //add or remove any view that you want to be dragged
        animal.setOnTouchListener(this);

        //add or remove any layout view that you want to accept dragged view
        match.setOnDragListener(this);
        middle.setOnDragListener(this);
        noMatch.setOnDragListener(this);

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
        sound = false;
        mp.reset();
        //noTouchy();
        //mp.release();
        //mp = null;
        // Accept view
        ViewGroup owner = (ViewGroup) view.getParent();
        owner.removeView(view);
        container.addView(view);
        // Avoid animal being dragged away again
        view.setOnTouchListener(null);

        // Praise randomly
        if (counterCorrect == 3) {
            //
            //touchy();
            /*findViewById(R.id.button_letter).setEnabled(false);
            findViewById(R.id.button_animal).setEnabled(false);*/
            //findViewById(R.id.button_back).setEnabled(true);
            counterCorrect = 0;
            int nr = rand.nextInt(6) + 1;
            int praise = getResources().getIdentifier("praise" + nr, "raw", this.getPackageName());
            //mp.reset();
            mp = MediaPlayer.create(this, praise);
            mp.setVolume(0.5f, 0.5f);
            new Handler().postDelayed(() -> mp.start(), 500);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //mp.reset();
                    //mp = null;
                    disPlayAnimal();
                    //new Handler().postDelayed(() -> disPlayAnimal(), 0);
                }
            });

            // encourage randomly
        } else if (counterWrong == 3) {
            //touchy();
            /*findViewById(R.id.button_letter).setEnabled(false);
            findViewById(R.id.button_animal).setEnabled(false);*/
            counterWrong = 0;
            int nr = rand.nextInt(4) + 1;
            int praise = getResources()
                .getIdentifier("encourage" + nr, "raw", this.getPackageName());
            //mp.reset();
            mp = MediaPlayer.create(this, praise);
            mp.setVolume(0.5f, 0.5f);
            new Handler().postDelayed(() -> mp.start(), 500);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //mp.reset();
                    //mp = null;
                    disPlayAnimal();
                    //new Handler().postDelayed(() -> disPlayAnimal(), 0);
                }
            });
        } else {
            /*findViewById(R.id.button_letter).setEnabled(false);
            findViewById(R.id.button_animal).setEnabled(false);*/
            //mp.reset();
            //mp = null;
            view.postDelayed(this::disPlayAnimal, 1000);
        }
    }

    void changeColor() {
        if (step >= colors.size()) {
            step %= colors.size();
        }

        // i is red per default
        if (level == 'i' && step == 0) {
            step++;
        }
        for (ImageView fragment : fragments) {
            DrawableCompat.setTint(fragment.getDrawable(),
                ContextCompat.getColor(getApplicationContext(), colors.get(step)));
        }

        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter);
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
        ImageButton iB = findViewById(R.id.button_letter);
        iB.getDrawable().setTintList(null);
        //DrawableCompat.setTint(iB.getDrawable(), ContextCompat.getColor(getApplicationContext(), colors.get((step + 1) % colors.size())));

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
                    // Animation
                    RelativeLayout progress = findViewById(R.id.image_progress);
                    View letter = findViewById(R.id.button_letter);
                    progress.startAnimation(scale);
                    letter.startAnimation(scale);

                    animalPool.getAnimalMapCurrent().get(level).remove(new Integer(animalID));
                    counterCorrect++;
                    correctMatches++;
                    counterWrong = 0;

                    // True positive
                    globals.getStatistics().addToStatistics(animalID, 0);
                    showProgress(correctMatches);

                    /*mp = MediaPlayer.create(this, getResources()
                        .getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();*/
                    if (loaded) {
                        soundPool.play(sounds[correct], 1f, 1f, 1, 0, 1f);
                    }
                    drop(view, container);

                    if (correctMatches == WINNINGNUMBER) {
                        new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    }
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
                    // Animation
                    RelativeLayout progress = findViewById(R.id.image_progress);
                    View letter = findViewById(R.id.button_letter);
                    progress.startAnimation(scale);
                    letter.startAnimation(scale);

                    counterCorrect++;
                    counterWrong = 0;
                    Character lvl = getResources().getResourceEntryName(animalID).charAt(0);
                    animalPool.getAnimalMapCurrent().get(lvl).remove(new Integer(animalID));

                    // True negative
                    globals.getStatistics().addToStatistics(animalID, 1);
                    /*correctMatches++;
                    showProgress(correctMatches);*/
                    /*mp = MediaPlayer.create(this, getResources()
                        .getIdentifier("sound_positive", "raw", this.getPackageName()));
                    mp.start();*/
                    if (loaded) {
                        soundPool.play(sounds[correct], 1f, 1f, 1, 0, 1f);
                    }
                    changeColor();
                    step++;
                    drop(view, container);

                }

                // Wrong
                else if (container.getId() != middle.getId()) {
                    // Animation
                    RelativeLayout progress = findViewById(R.id.image_progress);
                    View letter = findViewById(R.id.button_letter);
                    progress.startAnimation(scale);
                    letter.startAnimation(scale);

                    counterWrong++;
                    counterCorrect = 0;
                    if (correctMatches > 0) {
                        removeProgress(correctMatches);
                        correctMatches--;
                    }

                    // False positive
                    if (!fit && container.getId() == match.getId()) {
                        globals.getStatistics().addToStatistics(animalID, 2);
                    } else
                    // False negative
                    {
                        globals.getStatistics().addToStatistics(animalID, 3);
                    }
                   /* mp = MediaPlayer.create(this, getResources()
                        .getIdentifier("sound_negative", "raw", this.getPackageName()));
                    mp.start();*/
                    if (loaded) {
                        soundPool.play(sounds[wrong], 1f, 1f, 1, 0, 1f);
                    }
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
            RelativeLayout progress = findViewById(R.id.image_progress);


            if (!mp.isPlaying() && sound) {
                letterSound = getResources()
                    .getIdentifier(level + "_sound", "raw", this.getPackageName());
                progress.startAnimation(scale);
                view.startAnimation(scale);
                mp.reset();
                mp = MediaPlayer.create(this, letterSound);
                mp.setVolume(0.5f, 0.5f);
                mp.start();
            } else {
                progress.startAnimation(scaleHalf);
                view.startAnimation(scaleHalf);
            }
            /*if (loaded && !mp.isPlaying() && sound) {
                soundPool.play(sounds[currentLetter], 1f, 1f, 1, 0, 1f);
            }*/
        } else if (view.getId() == findViewById(R.id.button_animal).getId()) {

            if (!mp.isPlaying() && sound) {
                view.startAnimation(scale);
                mp.reset();
                mp = MediaPlayer.create(this, animalSound);
                mp.setVolume(0.5f, 0.5f);
                mp.start();
            } else
            // Button inactive / no sound
            {
                view.startAnimation(scaleHalf);
            }
        }

        // Back button
        else {
            backPressed = true;
            ImageButton backButton = findViewById(R.id.button_back);
            backButton.setEnabled(false);

            if (loaded) {
                soundPool.play(sounds[back], 1f, 1f, 1, 0, 1f);
            }

            //mCountDown.cancel();

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
                    if (mp.isPlaying()) {
                        mp.stop();

                    }
                    if (correctMatches == WINNINGNUMBER) {
                        writePreferences(level, COMPLETE);
                        SharedPreferences.Editor editor = availableLevels.edit();
                        editor.putBoolean("Tutorial", true);
                        editor.apply();
                        if (level != 't') {
                            int index = globals.getLevels().indexOf(level);
                            Character nextLevel = globals.getLevels().get(index + 1);
                            if (availableLevels.getInt(nextLevel.toString(), 0) == LOCKED) {
                                writePreferences(nextLevel, UNLOCKED);
                            }
                        }
                    }

                    /*mp.release();
                    mp = null;*/
                    // reset animal pool because we leave the level
                    animalPool.reset();
                    startActivity(intent);
                    finish();

                }
            });
            view.startAnimation(scale);


        }
    }


}
