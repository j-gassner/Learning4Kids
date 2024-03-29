package de.tum.in.l4k.weltreise;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.IOException;

/**
 * Class explaining the game principle. Is started on initial start of the app.
 *
 * @author Josefine Gaßner
 */

public class TutorialActivity extends BaseGameActivity implements View.OnTouchListener,
    View.OnDragListener {

    /**
     * Points to the element currently explained.
     */
    private ImageView arrow;

    /**
     * Indicates which animal is currently shown.
     */
    private int step;

    /**
     * Indicates if letter has been clicked when it is supposed to.
     */
    private boolean letterClicked;

    /**
     * Indicates if speaker has been clicked when it is supposed to.
     */
    private boolean speakerClicked;

    /**
     * Indicates if loewe has been dragged to the right.
     */
    private boolean dragCorrectRight;

    /**
     * Indicates if the last drag of the tutorial has been done.
     */
    private boolean lastDrag;

    /**
     * Button to skip the tutorial.
     */
    private ImageButton buttonSkip;

    /**
     * {@inheritDoc} Sets layout and initialzes inactivity handler.
     *
     * @param savedInstanceState Instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        inactivityHandler();
    }

    /**
     * {@inheritDoc} Restarts tutorial on resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        tutorialIntro();
    }

    /**
     * Repeats current instruction after specified duration of inactivity.
     */
    void inactivityHandler() {
        handleInactivity = new Handler();
        runnable = () -> {
            if (!mediaPlayer.isPlaying()) {
                // Make instruction interruptable
                playInstruction(currentInstruction);
                handler = true;

                // If user does nothing
                startHandler();
            }
        };
    }

    /**
     * Starts handler waiting for 10s of inactivity.
     */
    public void startHandler() {
        super.startHandler();
        handleInactivity.postDelayed(runnable, 10_000); //for 10 seconds
    }

    /**
     * Initialize elements.
     */
    protected void init() {
        super.init();
        winningNumber = 5;
        level = 'f';

        // Get other components
        match = findViewById(R.id.left);
        noMatch = findViewById(R.id.right);

        // Sounds
        soundLetter = ResourceManager.getRawIdLevel(this, level);
        soundAnimal = ResourceManager.getRawIdAnimal(this, "flughoernchen");

        // Letter "progress bar"
        int idImage = ResourceManager.getDrawableIdLetterFill(this, level);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), idImage);
        splitImage(bm);
    }

    /**
     * Find and disable buttons that should be locked at the start.
     */
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

    /**
     * Play intro.
     */
    @SuppressLint("ClickableViewAccessibility")
    public void tutorialIntro() {
        // Intro
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.tutorial_intro);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                afd.getDeclaredLength());
            mediaPlayer.prepareAsync();

            mediaPlayer.setVolume(0.5f, 0.5f);

            mediaPlayer.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 500));

            mediaPlayer.setOnCompletionListener(mp -> tutorialLetter());

            afd.close();

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }
    }

    /**
     * Explain letter button and enable it to be clicked.
     */
    void tutorialLetter() {
        arrow.setVisibility(View.VISIBLE);
        buttonLetter.setAlpha(1.0f);

        // Letter
        playInstruction(R.raw.tutorial_letter);
        mediaPlayer.setOnCompletionListener(mp -> {
            allowedToStartMediaPlayer = true;
            currentInstruction = R.raw.instruction_letter;
            buttonLetter.setEnabled(true);
            startHandler();
        });
    }

    /**
     * Explain which animals should be dragged to submarine.
     */
    void tutorialSubmarine() {
        allowedToStartMediaPlayer = false;
        playInstruction(R.raw.tutorial_submarine);
        mediaPlayer.setOnCompletionListener(mp -> tutorialAnimal());
    }

    /**
     * Display first animal and set speakerButton visible.
     */
    void tutorialAnimal() {
        // First animal
        new Handler().postDelayed(this::displayAnimal, 500);
        new Handler().postDelayed(() -> buttonSpeaker.setAlpha(1.0f), 2500);
        arrow = findViewById(R.id.button_point_speaker);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 2500);
    }

    /**
     * Explain speaker button and enable it to be clicked.
     */
    void tutorialSpeaker() {
        playInstruction(R.raw.tutorial_speaker);
        mediaPlayer.setOnCompletionListener(mp -> {
            allowedToStartMediaPlayer = true;
            buttonSpeaker.setEnabled(true);
            currentInstruction = R.raw.instruction_speaker;
            startHandler();
        });
    }

    /**
     * Explain how progress works.
     */
    void tutorialProgress() {
        allowedToStartMediaPlayer = false;
        playInstruction(R.raw.tutorial_progress);
        mediaPlayer.setOnCompletionListener(mp -> tutorialFlug());
    }

    /**
     * Explain where Flughoernchen wants to go.
     */
    void tutorialFlug() {
        playInstruction(R.raw.tutorial_flug);
        mediaPlayer.setOnCompletionListener(mp -> {
            allowedToStartMediaPlayer = true;
            currentInstruction = R.raw.instruction_flug;
            startHandler();
            tutorialDragCorrect();
        });
    }

    /**
     * Enable Flughoernchen being dragged to submarine.
     */
    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragCorrect() {
        // Drag to submarine
        arrow.setVisibility(View.INVISIBLE);
        arrow = findViewById(R.id.button_point_submarine);
        arrow.setVisibility(View.VISIBLE);
        animal.setOnTouchListener(this);
        match.setOnDragListener(this);
    }

    /**
     * Explain what happens when irrelevant animal is dragged correctly. Display Loewen.
     */
    void tutorialColorChange() {
        arrow.setVisibility(View.INVISIBLE);
        allowedToStartMediaPlayer = false;
        playInstruction(R.raw.tutorial_colorchange);
        mediaPlayer.setOnCompletionListener(mp -> displayAnimal());
    }

    /**
     * Explain where Loewe wants to go.
     */
    void tutorialLionRight() {
        allowedToStartMediaPlayer = false;
        playInstruction(R.raw.tutorial_loewe_correct);
        mediaPlayer.setOnCompletionListener(mp -> {
            allowedToStartMediaPlayer = true;
            currentInstruction = R.raw.instruction_loewe_correct;
            startHandler();
            tutorialDragCorrectRight();
        });
    }

    /**
     * Enable Loewen to be dragged to sign.
     */
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

    /**
     * Explain what happens on mistake.
     */
    void tutorialProgressLost() {
        arrow.setVisibility(View.INVISIBLE);
        allowedToStartMediaPlayer = false;
        playInstruction(R.raw.tutorial_progress_lost);
        mediaPlayer.setOnCompletionListener(mp -> displayAnimal());
    }

    /**
     * Enable Loewen to be dragged incorrectly.
     */
    @SuppressLint("ClickableViewAccessibility")
    void tutorialDragFalse() {
        // Drag wrong
        arrow = findViewById(R.id.button_point_submarine);
        allowedToStartMediaPlayer = false;
        playInstruction(R.raw.instruction_loewe_wrong);
        mediaPlayer.setOnCompletionListener(mp -> {
            allowedToStartMediaPlayer = true;
            currentInstruction = R.raw.instruction_loewe_wrong;
            startHandler();
            arrow.setVisibility(View.VISIBLE);
            animal.setOnTouchListener(this);
            match.setOnDragListener(this);
            noMatch.setOnDragListener(null);
        });
    }

    /**
     * Explain back button and enable it.
     */
    void tutorialBack() {
        // Back button
        arrow.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(() -> buttonBack.setAlpha(1.0f), 500);
        arrow = findViewById(R.id.button_point_back);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);
        allowedToStartMediaPlayer = false;
        playInstruction(R.raw.tutorial_back);
        mediaPlayer.setOnCompletionListener(mp -> {
            currentInstruction = R.raw.instruction_back;
            startHandler();
            buttonBack.setEnabled(true);
            allowedToStartMediaPlayer = true;
        });
    }

    /**
     * Displays animals and plays their name.
     */
    @SuppressLint("ClickableViewAccessibility")
    void displayAnimal() {
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);

        // Correct animal - Flughoernchen
        if (step == 0) {
            animalID = ResourceManager.getDrawableIdResource(this, "flughoernchen_animal");
            animal.setImageResource(R.drawable.flughoernchen_animal);
            soundAnimal = R.raw.flughoernchen_sound;
            animal.setVisibility(View.VISIBLE);
            relevant = true;
        } else {
            animal.setImageResource(R.drawable.loewe_animal);
            animalID = ResourceManager.getDrawableIdResource(this, "loewe_animal");
            soundAnimal = R.raw.loewe_sound;
            animal.setVisibility(View.VISIBLE);
            relevant = false;
        }
        positionAnimal(false);

        // Animal sound
        playInstruction(soundAnimal);
        mediaPlayer.setOnCompletionListener(mp -> {
            allowedToStartMediaPlayer = true;
            if (step == 0) {
                tutorialSpeaker();
            } else if (step == 1 && !dragCorrectRight) {
                tutorialLionRight();
            } else {
                tutorialDragFalse();
            }
        });
    }

    /**
     * Called when animal is dragged to another view that accepts it. Shows animation in
     * letterButton. A sound indicates whether it was right or wrong.
     *
     * @param view View to be dragged.
     * @param container Where view is dragged to.
     * @param sound Correct or wrong sound.
     */
    void dragAnimal(View view, LinearLayout container, int sound) {
        // Animation
        RelativeLayout progress = findViewById(R.id.image_progress);
        View letter = findViewById(R.id.button_letter);
        progress.startAnimation(scale);
        letter.startAnimation(scale);
        playSound(sounds[sound]);
        dropAnimal(view, container);
    }

    /**
     * {@inheritDoc} Manages drag events of animal.
     *
     * @param layoutview View to be dragged, i.e. the animal.
     * @param dragevent Drag event.
     */
    @Override
    public boolean onDrag(View layoutview, DragEvent dragevent) {
        int action = dragevent.getAction();
        View view = (View) dragevent.getLocalState();
        if (view == null) {
            return false;
        }
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
            case DragEvent.ACTION_DRAG_EXITED:
            case DragEvent.ACTION_DRAG_ENTERED:
                view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DROP:
                LinearLayout container = (LinearLayout) layoutview;
                // Match
                if (relevant && container.getId() == match.getId() && !dragCorrectRight) {
                    stopHandler();
                    dragAnimal(view, container, shortSounds.CORRECT.ordinal());
                    correctMatches++;
                    step++;
                    addProgress(correctMatches);
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialColorChange, 500);
                }
                // No match
                else if (!relevant && container.getId() == noMatch.getId() && dragCorrectRight) {
                    stopHandler();
                    dragAnimal(view, container, shortSounds.CORRECT.ordinal());
                    changeColor();
                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialProgressLost, 500);
                }
                // Wrong
                else if (container.getId() != middle.getId()) {
                    stopHandler();
                    lastDrag = true;
                    dragAnimal(view, container, shortSounds.WRONG.ordinal());

                    removeProgress(correctMatches);
                    // Doesn't matter at this point
                    //correctMatches--;

                    new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    new Handler().postDelayed(this::tutorialBack, 500);
                    allowedToStartMediaPlayer = true;
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

    /**
     * {@inheritDoc} Manages events depending on which view is clicked.
     *
     * @param view View that is clicked.
     */
    public void onClick(View view) {
        // Letter button
        if (view.getId() == buttonLetter.getId()) {
            RelativeLayout progress = findViewById(R.id.image_progress);
            if (!mediaPlayer.isPlaying() && allowedToStartMediaPlayer) {
                progress.startAnimation(scale);
                view.startAnimation(scale);
                playInstruction(soundLetter);
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (!letterClicked) {
                        stopHandler();
                        arrow.setVisibility(View.INVISIBLE);
                        letterClicked = true;
                        tutorialSubmarine();
                    } else {
                        allowedToStartMediaPlayer = true;
                    }
                });
            } else {
                progress.startAnimation(scaleHalf);
                view.startAnimation(scaleHalf);
            }
            // Speaker button
        } else if (view.getId() == buttonSpeaker.getId()) {
            if (!mediaPlayer.isPlaying() && allowedToStartMediaPlayer && !lastDrag) {
                view.startAnimation(scale);
                playInstruction(soundAnimal);
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (!speakerClicked) {
                        stopHandler();
                        speakerClicked = true;
                        arrow.setVisibility(View.INVISIBLE);
                        tutorialProgress();
                    } else {
                        allowedToStartMediaPlayer = true;
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
            playSound(sounds[shortSounds.BUTTON.ordinal()]);
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
            playSound(sounds[shortSounds.BUTTON.ordinal()]);
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