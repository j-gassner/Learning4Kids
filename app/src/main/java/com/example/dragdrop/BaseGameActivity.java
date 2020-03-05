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
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public abstract class BaseGameActivity extends BaseActivity implements View.OnTouchListener,
    View.OnDragListener, View.OnClickListener {

    int WINNINGNUMBER;
    ImageView animal;
    int animalSound;
    int letterSound;
    LinearLayout match, middle, noMatch;
    boolean fit;
    int animalID;
    int step;
    Handler handleInactivity;
    Runnable runnable;
    ArrayList<ImageView> fragments;
    ArrayList<Integer> colors = new ArrayList<>(
        Arrays.asList(R.color.red, R.color.blue, R.color.yellow, R.color.pink,
            R.color.green, R.color.orange, R.color.purple));
    ImageButton letterButton, speakerButton, backButton;
    int correctMatches;
    boolean isRunning, handler;
    int currentInstruction;

    Animals animalPool = new Animals();
    private LevelCollection levelCollection;
    private static Random rand = new Random();
    private Globals globals;
    Animation zoom, flash;
    boolean stillThere;
    int counterCorrect, counterWrong;
    static int[] sounds;
    boolean loaded;
    static int back = 0;
    static int camera = 1;
    static int correct = 2;
    static int wrong = 3;
    boolean sound;
    boolean backPressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);
        hideSystemUI();
        sounds = new int[4];

        handleInactivity = new Handler();
        runnable = () -> {
            if (stillThere) {
                // Make instruction interruptable
                handler = true;
                playInstruction(R.raw.are_you_still_there);
                stillThere = false;
            } else {
                playInstruction(getResources()
                    .getIdentifier("instruction_" + level, "raw", getPackageName()));
                handler = true;
                stillThere = true;

                // If user does nothing
            }
            startHandler();
        };

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

    public void startHandler() {
        isRunning = true;
        handleInactivity.postDelayed(runnable, 30000); //for 30 seconds
    }

    public void stopHandler() {
        isRunning = false;
        handleInactivity.removeCallbacks(runnable);
    }


    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        removeColor();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();

        if (mp.isPlaying() && handler) {
            mp.stop();
            handler = false;
        }

        if (isRunning) {
            stopHandler();
            startHandler();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHandler();

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

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void positionAnimal() {
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
        matrix.postScale(scalingFactor, scalingFactor);

        dWidth *= scalingFactor;
        dHeight *= scalingFactor;
        // Bottom center
        matrix.postTranslate(Math.round((vWidth - dWidth) * 0.5f),
            Math.round((vHeight - dHeight)));
        animal.setImageMatrix(matrix);
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
            fragment.getDrawable().setTintList(null);
        }

        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter);
        iB.getDrawable().setTintList(null);

    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

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


    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    protected void init() {
        buttonSounds();

        letterButton = findViewById(R.id.button_letter);
        speakerButton = findViewById(R.id.button_animal);
        backButton = findViewById(R.id.button_back);
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
        levelCollection = new LevelCollection(this);
        animal = findViewById(R.id.animal);
        ImageView background = findViewById(R.id.level_background);
        background.setImageResource(levelCollection.getLevel(level).getBackgroundID());
        WINNINGNUMBER = levelCollection.getLevel(level).getWinningNumber();
        //background.setBackgroundResource(levels.getLevel(level).getBackgroundID());
        //findViewById(R.id.button_animal).setEnabled(true);

        if (levelCollection.getLevel(level).getIsLeft()) {
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
                    startHandler();
                    //mCountDown.start();
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

    void writePreferences(Character level, int mode) {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putInt(level.toString(), mode);
        editor.apply();

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
        if (availableLevels.getInt(level.toString(), 0) == levelState.UNLOCKED.ordinal()) {
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

        animal.setOnTouchListener(this);
        // Level done
        if (correctMatches == WINNINGNUMBER) {
            sound = false;
            //mCountDown.cancel();
            stopHandler();

            playInstruction(R.raw.complete_sound);
            mp.setOnCompletionListener(mp -> cameraFlash());

            return;
        }
        //Log.d("NEW ANIMAL", " METHOD CALLED");

        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);

        // n% chance correct letter
        if (rand.nextInt(100) < levelCollection.getLevel(level).getDifficulty()) {
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
            fit = false;
        }

        mp.reset();
        mp = MediaPlayer.create(this, animalSound);
        if (!backPressed) {
            mp.setVolume(0.5f, 0.5f);
            mp.start();
            mp.setOnCompletionListener(mp -> {
                sound = true;
            });
        }
        positionAnimal();

    }

    void levelCompleted() {
        writePreferences(level, levelState.COMPLETED.ordinal());
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putBoolean("Tutorial", true);
        editor.apply();
        if (level != 't') {
            int index = globals.getLevels().indexOf(level);
            Character nextLevel = globals.getLevels().get(index + 1);
            if (availableLevels.getInt(nextLevel.toString(), 0) == levelState.LOCKED.ordinal()) {
                writePreferences(nextLevel, levelState.UNLOCKED.ordinal());
            }
        }
    }

    void leaveLevel() {
        globals.getAnimalPool().reset();
        levelCompleted();

        Intent intent = new Intent(this, StartActivity.class);

        Handler handler = new Handler();
        handler.postDelayed(() -> startActivity(intent), 1000);
        handler.postDelayed(this::finish, 1000);

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

        animal.setY(animal.getY() - 100);
        String packageName = this.getPackageName();

        playInstruction(getResources().getIdentifier("museum_" + level, "raw", packageName));
        mp.setOnCompletionListener(mp -> leaveLevel());
        animal.startAnimation(zoom);
    }

    @RequiresApi(api = VERSION_CODES.N)
    void drop(View view, LinearLayout container) {
        sound = false;
        mp.reset();

        // Accept view
        ViewGroup owner = (ViewGroup) view.getParent();
        owner.removeView(view);
        container.addView(view);
        // Avoid animal being dragged away again
        view.setOnTouchListener(null);

        // Praise randomly
        if (counterCorrect == 3) {
            counterCorrect = 0;
            int nr = rand.nextInt(6) + 1;
            int praise = getResources().getIdentifier("praise" + nr, "raw", this.getPackageName());
            //mp.reset();
            mp = MediaPlayer.create(this, praise);
            mp.setVolume(0.5f, 0.5f);
            new Handler().postDelayed(() -> mp.start(), 500);
            mp.setOnCompletionListener(mp -> disPlayAnimal());

            // encourage randomly
        } else if (counterWrong == 3) {
            counterWrong = 0;
            int nr = rand.nextInt(4) + 1;
            int praise = getResources()
                .getIdentifier("encourage" + nr, "raw", this.getPackageName());
            //mp.reset();
            mp = MediaPlayer.create(this, praise);
            mp.setVolume(0.5f, 0.5f);
            new Handler().postDelayed(() -> mp.start(), 500);
            mp.setOnCompletionListener(mp -> {
                disPlayAnimal();
            });
        } else {
            view.postDelayed(this::disPlayAnimal, 1000);
        }
    }

    void playInstruction(int resID) {
        //sound = false;
        mp.reset();
        mp = MediaPlayer.create(this, resID);
        mp.setVolume(0.5f, 0.5f);
        mp.start();

    }

    // This is the method that the system calls when it dispatches a drag event to the
    // listener.
    @RequiresApi(api = VERSION_CODES.N)
    @Override
    public boolean onDrag(View layoutview, DragEvent dragevent) {
        int action = dragevent.getAction();
        View view = (View) dragevent.getLocalState();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_EXITED:
                view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DROP:

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

                    if (loaded) {
                        soundPool.play(sounds[correct], 1f, 1f, 1, 0, 1f);
                    }
                    drop(view, container);

                    if (correctMatches == WINNINGNUMBER) {
                        new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    }

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

                    // valueOf does not work here
                    animalPool.getAnimalMapCurrent().get(lvl).remove(new Integer(animalID));

                    // True negative
                    globals.getStatistics().addToStatistics(animalID, 1);

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
        if (view.getId() == letterButton.getId()) {
            RelativeLayout progress = findViewById(R.id.image_progress);

            if (!mp.isPlaying() && sound) {
                letterSound = getResources()
                    .getIdentifier(level + "_sound", "raw", this.getPackageName());
                progress.startAnimation(scale);
                view.startAnimation(scale);
                playInstruction(letterSound);

            } else {
                progress.startAnimation(scaleHalf);
                view.startAnimation(scaleHalf);
            }

        } else if (view.getId() == speakerButton.getId()) {

            if (!mp.isPlaying() && sound) {
                view.startAnimation(scale);
                playInstruction(animalSound);

            } else
            // Button inactive / no sound
            {
                view.startAnimation(scaleHalf);
            }
        }

        // Back button
        else {
            backPressed = true;
            backButton.setEnabled(false);
            speakerButton.setEnabled(false);
            letterButton.setEnabled(false);

            if (loaded) {
                soundPool.play(sounds[back], 1f, 1f, 1, 0, 1f);
            }

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
                        levelCompleted();
                    }
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
