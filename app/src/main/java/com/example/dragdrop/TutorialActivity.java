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

public class TutorialActivity extends AppCompatActivity implements View.OnTouchListener,
    View.OnDragListener {

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
    boolean letterClicked, speakerClicked, dragCorrectRight, sound, isRunning, handler, lastDrag;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tutorial);
        hideSystemUI();
        handleInactivity = new Handler();
        runnable = () -> {
            if (!mp.isPlaying()) {
                // Make instruction interruptable
                handler = true;
                mp.reset();
                mp = MediaPlayer.create(getApplicationContext(), currentInstruction);
                mp.setVolume(0.5f, 0.5f);
                mp.start();

                // If user does nothing
                startHandler();
            }
        };
    }

    public void stopHandler() {
        isRunning = false;
        handleInactivity.removeCallbacks(runnable);
    }

    public void startHandler() {
        isRunning = true;
        handleInactivity.postDelayed(runnable, 10000); //for 10 seconds
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        init();
        buttonSounds();
        hideSystemUI();

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
        stopHandler();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mp.release();
        mp = null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        sounds = new int[3];
        sounds[button] = soundPool.load(this, R.raw.button, 1);
        sounds[correct] = soundPool.load(this, R.raw.sound_positive, 1);
        sounds[wrong] = soundPool.load(this, R.raw.sound_negative, 1);
    }

    void playInstruction(int resID) {
        sound = false;
        mp.reset();
        mp = MediaPlayer.create(this, resID);
        mp.setVolume(0.5f, 0.5f);
        mp.start();

    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void tutorialIntro() {
        // Find buttons
        arrow = findViewById(R.id.button_point_letter);
        buttonSkip = findViewById(R.id.button_skip);
        buttonBack = findViewById(R.id.button_back_tutorial);
        buttonLetter = findViewById(R.id.button_letter_tutorial);
        buttonAnimal = findViewById(R.id.button_animal_tutorial);

        // Disable buttons
        buttonBack.setEnabled(false);
        buttonLetter.setEnabled(false);
        buttonAnimal.setEnabled(false);

        // Indicate buttons are disabled
        buttonLetter.setAlpha(0.3f);
        buttonAnimal.setAlpha(0.3f);
        buttonBack.setAlpha(0.3f);

        // Intro
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.tutorial_intro);
        try {
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                afd.getDeclaredLength());
            mp.prepareAsync();

            mp.setVolume(0.5f, 0.5f);

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
        playInstruction(R.raw.tutorial_letter);
        mp.setOnCompletionListener(mp -> {
            sound = true;
            currentInstruction = R.raw.instruction_letter;
            buttonLetter.setEnabled(true);
            startHandler();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialSubmarine() {
        playInstruction(R.raw.tutorial_submarine);
        mp.setOnCompletionListener(mp -> tutorialAnimal());
    }

    void tutorialSpeaker() {
        playInstruction(R.raw.tutorial_speaker);
        mp.setOnCompletionListener(mp -> {
            sound = true;
            buttonAnimal.setEnabled(true);
            currentInstruction = R.raw.instruction_speaker;
            startHandler();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialAnimal() {
        // First animal
        new Handler().postDelayed(() -> disPlayAnimal(0), 500);
        new Handler().postDelayed(() -> buttonAnimal.setAlpha(1.0f), 2500);
        arrow = findViewById(R.id.button_point_speaker);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 2500);

    }

    void tutorialProgress() {
        playInstruction(R.raw.tutorial_progress);
        mp.setOnCompletionListener(mp -> tutorialFlug());

    }

    void tutorialFlug() {
        playInstruction(R.raw.tutorial_flug);
        mp.setOnCompletionListener(mp -> {
            sound = true;
            currentInstruction = R.raw.instruction_flug;
            startHandler();
            tutorialDragCorrect();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragCorrect() {
        // Drag to submarine
        arrow.setVisibility(View.INVISIBLE);
        arrow = findViewById(R.id.button_point_submarine);
        arrow.setVisibility(View.VISIBLE);
        animal.setOnTouchListener(this);
        match.setOnDragListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialColorChange() {
        arrow.setVisibility(View.INVISIBLE);
        playInstruction(R.raw.tutorial_colorchange);
        mp.setOnCompletionListener(mp -> disPlayAnimal(1));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialLionRight() {
        playInstruction(R.raw.tutorial_loewe_correct);
        mp.setOnCompletionListener(mp -> {
            sound = true;
            currentInstruction = R.raw.instruction_loewe_correct;
            startHandler();
            tutorialDragCorrectRight();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragCorrectRight() {
        // Drag animal to the right
        dragCorrectRight = true;
        arrow = findViewById(R.id.button_point_sign);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        animal.setOnTouchListener(this);
        noMatch.setOnDragListener(this);
        match.setOnDragListener(null);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialProgressLost() {
        arrow.setVisibility(View.INVISIBLE);
        playInstruction(R.raw.tutorial_progress_lost);
        mp.setOnCompletionListener(mp -> disPlayAnimal(1));
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialDragFalse() {
        // Drag wrong

        View.OnTouchListener ot = this;
        View.OnDragListener od = this;
        arrow = findViewById(R.id.button_point_submarine);
        playInstruction(R.raw.instruction_loewe_wrong);
        mp.setOnCompletionListener(mp -> {
            sound = true;
            currentInstruction = R.raw.instruction_loewe_wrong;
            startHandler();
            arrow.setVisibility(View.VISIBLE);
            animal.setOnTouchListener(ot);
            match.setOnDragListener(od);
            noMatch.setOnDragListener(null);
        });
    }


    void tutorialBack() {
        // Back button
        arrow.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(() -> buttonBack.setAlpha(1.0f), 500);
        arrow = findViewById(R.id.button_point_back);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        playInstruction(R.raw.tutorial_back);
        mp.setOnCompletionListener(mp -> {
            currentInstruction = R.raw.instruction_back;
            startHandler();
            buttonBack.setEnabled(true);
            sound = true;
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
        // Animations for buttons
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);
        scaleHalf = AnimationUtils.loadAnimation(this, R.anim.button_inactive_anim);

        // Get other components
        animal = findViewById(R.id.animal);
        match = findViewById(R.id.left);
        noMatch = findViewById(R.id.right);
        middle = findViewById(R.id.middle);

        // Sounds
        letterSound = getResources().getIdentifier("f_sound", "raw", this.getPackageName());
        animalSound = getResources()
            .getIdentifier("flughoernchen_sound", "raw", this.getPackageName());

        // Letter "progress bar"
        int idImage = getResources()
            .getIdentifier("f_letter_fill", "drawable", this.getPackageName());
        Bitmap bm = BitmapFactory.decodeResource(getResources(), idImage);
        splitImage(bm);
    }


    // Cut image horizontally
    void splitImage(Bitmap image) {
        fragments = new ArrayList<>(WINNINGNUMBER);
        int y = 0;
        for (int i = 0; i < WINNINGNUMBER; i++) {
            ImageView frag = new ImageView(getApplicationContext());
            frag.setImageBitmap(Bitmap.createBitmap(image, 0, y, image.getWidth(),
                (int) Math.ceil(image.getHeight() / WINNINGNUMBER)));
            fragments.add(frag);
            y += (int) Math.ceil(image.getHeight() / WINNINGNUMBER);
        }

    }


    void showProgress(int step) {
        RelativeLayout progress = findViewById(R.id.tutorial_progress);
        int chunkSize = (int) Math.ceil(progress.getHeight() / WINNINGNUMBER);
        ImageView fragment = fragments.get(fragments.size() - step);

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
        RelativeLayout progress = findViewById(R.id.tutorial_progress);
        ImageView fragment = fragments.get(fragments.size() - step);
        progress.removeView(fragment);
    }

    // More compact as step will always be 0 when called
    void changeColor() {
        for (ImageView fragment : fragments) {
            DrawableCompat.setTint(fragment.getDrawable(),
                ContextCompat.getColor(getApplicationContext(), colors.get(0)));
        }

        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter_tutorial);
        DrawableCompat.setTint(iB.getDrawable(), ContextCompat
            .getColor(getApplicationContext(), colors.get(1)));
    }


    // Set letter back to initial look
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void removeColor() {
        for (ImageView fragment : fragments) {
            fragment.getDrawable().setTintList(null);
        }
        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter_tutorial);
        iB.getDrawable().setTintList(null);
    }

    //Set position of animal image and display
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    private void disPlayAnimal(int step) {
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);

        // Correct animal
        if (step == 0) {
            animal.setImageResource(R.drawable.flughoernchen_animal);
            animalID = getResources()
                .getIdentifier("flughoernchen_animal", "drawable", this.getPackageName());
            animalSound = R.raw.flughoernchen_sound;
            animal.setVisibility(View.VISIBLE);

            fit = true;

        } else if (step == 1) {
            animal.setImageResource(R.drawable.loewe_animal);
            animalID = getResources()
                .getIdentifier("loewe_animal", "drawable", this.getPackageName());
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
        playInstruction(animalSound);

        mp.setOnCompletionListener(mp -> {
            sound = true;
            if (step == 0) {
                tutorialSpeaker();
            } else if (step == 1 && !dragCorrectRight) {
                tutorialLionRight();
            } else {
                tutorialDragFalse();
            }
        });

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
        ViewGroup owner = (ViewGroup) view.getParent();
        owner.removeView(view);
        container.addView(view);

        // Avoid animal being dragged away again
        view.setOnTouchListener(null);

    }

    void dragAnimal(View view, LinearLayout container, int sound) {
        // Animation
        RelativeLayout progress = findViewById(R.id.tutorial_progress);
        View letter = findViewById(R.id.button_letter_tutorial);
        progress.startAnimation(scale);
        letter.startAnimation(scale);

        if (loaded) {
            soundPool.play(sounds[sound], 1f, 1f, 1, 0, 1f);
        }
        drop(view, container);

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
            case DragEvent.ACTION_DRAG_EXITED:
            case DragEvent.ACTION_DRAG_ENTERED:
                view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DROP:
                //Log.d("drag", "Dropped");
                LinearLayout container = (LinearLayout) layoutview;

                // Match
                if (fit && container.getId() == match.getId() && !dragCorrectRight) {
                    //dragCorrect = true;
                    dragAnimal(view, container, correct);
                    correctMatches++;
                    showProgress(correctMatches);
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialColorChange, 500);
                }

                // No match
                else if (!fit && container.getId() == noMatch.getId() && dragCorrectRight) {
                    dragAnimal(view, container, correct);
                    changeColor();
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialProgressLost, 500);

                }
                // Wrong
                else if (container.getId() != middle.getId()) {
                    lastDrag = true;
                    dragAnimal(view, container, wrong);

                    removeProgress(correctMatches);
                    // Doesn't matter at this point
                    //correctMatches--;

                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialBack, 500);
                    sound = true;
                }

                break;
            case DragEvent.ACTION_DRAG_ENDED:
                //Log.d("drag", "Drag ended");
                view.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }

        return true;
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onClick(View view) {
        // Letter button
        if (view.getId() == buttonLetter.getId()) {
            RelativeLayout progress = findViewById(R.id.tutorial_progress);

            if (!mp.isPlaying() && sound) {
                progress.startAnimation(scale);
                view.startAnimation(scale);
                playInstruction(letterSound);
                mp.setOnCompletionListener(mp -> {
                    if (!letterClicked) {
                        arrow.setVisibility(View.INVISIBLE);
                        letterClicked = true;
                        tutorialSubmarine();
                    } else {
                        sound = true;
                    }

                });
            } else {
                progress.startAnimation(scaleHalf);
                view.startAnimation(scaleHalf);
            }

            // Speaker button
        } else if (view.getId() == buttonAnimal.getId()) {
            if (!mp.isPlaying() && sound && !lastDrag) {
                view.startAnimation(scale);
                playInstruction(animalSound);
                mp.setOnCompletionListener(mp -> {
                    if (!speakerClicked) {
                        speakerClicked = true;
                        arrow.setVisibility(View.INVISIBLE);
                        tutorialProgress();
                    } else {
                        sound = true;
                    }
                });
            } else {
                view.startAnimation(scaleHalf);
            }
        }

        // Skip
        else if (view.getId() == buttonSkip.getId()) {
            stopHandler();
            buttonSkip.setEnabled(false);
            buttonLetter.setEnabled(false);
            buttonAnimal.setEnabled(false);
            buttonBack.setEnabled(false);

            if (loaded) {
                soundPool.play(sounds[button], 1f, 1f, 1, 0, 1f);
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
                    startActivity(intent);
                    finish();
                }
            });
            buttonSkip.startAnimation(scale);
        }
        // Back
        else {
            stopHandler();
            if (loaded) {
                soundPool.play(sounds[button], 1f, 1f, 1, 0, 1f);
            }

            buttonBack.setEnabled(false);
            buttonSkip.setEnabled(false);
            buttonLetter.setEnabled(false);
            buttonAnimal.setEnabled(false);

            Intent intent = new Intent(this, StartActivity.class);
            intent.putExtra("Tutorial", true);
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

        }
    }
}
