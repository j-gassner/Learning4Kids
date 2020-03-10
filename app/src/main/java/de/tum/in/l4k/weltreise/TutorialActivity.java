package de.tum.in.l4k.weltreise;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.RequiresApi;
import java.io.IOException;


public class TutorialActivity extends BaseGameActivity implements View.OnTouchListener,
    View.OnDragListener {

    private ImageView arrow;
    private boolean fit;
    int step;

    boolean letterClicked, speakerClicked, dragCorrectRight, lastDrag;
    ImageButton buttonSkip;

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        inactivityHandler();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        tutorialIntro();
    }

    void inactivityHandler() {
        handleInactivity = new Handler();
        runnable = () -> {
            if (!mp.isPlaying()) {
                // Make instruction interruptable
                playInstruction(currentInstruction);
                handler = true;

                // If user does nothing
                startHandler();
            }
        };

    }

    public void startHandler() {
        super.startHandler();
        handleInactivity.postDelayed(runnable, 10000); //for 10 seconds
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    protected void init() {
        super.init();

        winningNumber = 5;
        level = 'f';

        // Get other components
        match = findViewById(R.id.left);
        noMatch = findViewById(R.id.right);

        // Sounds
        soundLetter = getResources().getIdentifier("f_sound", "raw", this.getPackageName());
        soundAnimal = getResources()
            .getIdentifier("flughoernchen_sound", "raw", this.getPackageName());

        // Letter "progress bar"
        int idImage = getResources()
            .getIdentifier("f_letter_fill", "drawable", this.getPackageName());
        Bitmap bm = BitmapFactory.decodeResource(getResources(), idImage);
        splitImage(bm);
    }


    void loadButtons() {
        super.loadButtons();
        arrow = findViewById(R.id.button_point_letter);
        buttonSkip = findViewById(R.id.button_skip);
        buttonBack.setEnabled(false);
        buttonLetter.setEnabled(false);
        buttonSpeaker.setEnabled(false);

        // Indicate buttons are disabled
        buttonLetter.setAlpha(0.3f);
        buttonSpeaker.setAlpha(0.3f);
        buttonBack.setAlpha(0.3f);

    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void tutorialIntro() {
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
        sound = false;
        playInstruction(R.raw.tutorial_submarine);
        mp.setOnCompletionListener(mp -> tutorialAnimal());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialAnimal() {
        // First animal
        new Handler().postDelayed(this::displayAnimal, 500);
        new Handler().postDelayed(() -> buttonSpeaker.setAlpha(1.0f), 2500);
        arrow = findViewById(R.id.button_point_speaker);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 2500);

    }

    void tutorialSpeaker() {
        playInstruction(R.raw.tutorial_speaker);
        mp.setOnCompletionListener(mp -> {
            sound = true;
            buttonSpeaker.setEnabled(true);
            currentInstruction = R.raw.instruction_speaker;
            startHandler();
        });
    }

    void tutorialProgress() {
        sound = false;
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
        sound = false;
        playInstruction(R.raw.tutorial_colorchange);
        mp.setOnCompletionListener(mp -> displayAnimal());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialLionRight() {
        sound = false;
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
        sound = false;
        playInstruction(R.raw.tutorial_progress_lost);
        mp.setOnCompletionListener(mp -> displayAnimal());
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    void tutorialDragFalse() {
        // Drag wrong
        arrow = findViewById(R.id.button_point_submarine);
        sound = false;
        playInstruction(R.raw.instruction_loewe_wrong);
        mp.setOnCompletionListener(mp -> {
            sound = true;
            currentInstruction = R.raw.instruction_loewe_wrong;
            startHandler();
            arrow.setVisibility(View.VISIBLE);
            animal.setOnTouchListener(this);
            match.setOnDragListener(this);
            noMatch.setOnDragListener(null);
        });
    }

    void tutorialBack() {
        // Back button
        arrow.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(() -> buttonBack.setAlpha(1.0f), 500);
        arrow = findViewById(R.id.button_point_back);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        sound = false;
        playInstruction(R.raw.tutorial_back);
        mp.setOnCompletionListener(mp -> {
            currentInstruction = R.raw.instruction_back;
            startHandler();
            buttonBack.setEnabled(true);
            sound = true;
        });
    }

    //Set position of animal image and display
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    void displayAnimal() {
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);

        // Correct animal - Flughoernchen
        if (step == 0) {
            animalID = getResources()
                .getIdentifier("flughoernchen_animal", "drawable", this.getPackageName());
            animal.setImageResource(R.drawable.flughoernchen_animal);
            soundAnimal = R.raw.flughoernchen_sound;
            animal.setVisibility(View.VISIBLE);

            fit = true;

        } else {
            animal.setImageResource(R.drawable.loewe_animal);
            animalID = getResources()
                .getIdentifier("loewe_animal", "drawable", this.getPackageName());
            soundAnimal = R.raw.loewe_sound;
            animal.setVisibility(View.VISIBLE);
            fit = false;

        }
        positionAnimal(false);

        // Animal sound
        playInstruction(soundAnimal);

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
    void dragAnimal(View view, LinearLayout container, int sound) {
        // Animation
        RelativeLayout progress = findViewById(R.id.image_progress);
        View letter = findViewById(R.id.button_letter);
        progress.startAnimation(scale);
        letter.startAnimation(scale);

        if (loaded) {
            soundPool.play(sounds[sound], 1f, 1f, 1, 0, 1f);
        }
        dropAnimal(view, container);

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
                    dragAnimal(view, container, shortSounds.CORRECT.ordinal());
                    correctMatches++;
                    step++;
                    showProgress(correctMatches);
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialColorChange, 500);
                }

                // No match
                else if (!fit && container.getId() == noMatch.getId() && dragCorrectRight) {
                    dragAnimal(view, container, shortSounds.CORRECT.ordinal());
                    changeColor();
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialProgressLost, 500);

                }
                // Wrong
                else if (container.getId() != middle.getId()) {
                    lastDrag = true;
                    dragAnimal(view, container, shortSounds.WRONG.ordinal());

                    removeProgress(correctMatches);
                    // Doesn't matter at this point
                    //correctMatches--;

                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialBack, 500);
                    sound = true;
                }

                break;
            case DragEvent.ACTION_DRAG_ENDED:
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
            RelativeLayout progress = findViewById(R.id.image_progress);

            if (!mp.isPlaying() && sound) {
                progress.startAnimation(scale);
                view.startAnimation(scale);
                playInstruction(soundLetter);
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
        } else if (view.getId() == buttonSpeaker.getId()) {
            if (!mp.isPlaying() && sound && !lastDrag) {
                view.startAnimation(scale);
                playInstruction(soundAnimal);
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
            buttonSpeaker.setEnabled(false);
            buttonBack.setEnabled(false);

            if (loaded) {
                soundPool.play(sounds[shortSounds.BUTTON.ordinal()], 1f, 1f, 1, 0, 1f);
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
                soundPool.play(sounds[shortSounds.BUTTON.ordinal()], 1f, 1f, 1, 0, 1f);
            }

            buttonBack.setEnabled(false);
            buttonSkip.setEnabled(false);
            buttonLetter.setEnabled(false);
            buttonSpeaker.setEnabled(false);

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