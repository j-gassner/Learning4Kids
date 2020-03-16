package de.tum.in.l4k.weltreise;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

/**
 * Class that manages the gameplay for each level.
 *
 * @author Josefine Gaßner
 */

public class GameActivity extends BaseGameActivity implements View.OnTouchListener,
    View.OnDragListener, View.OnClickListener {

    /**
     * Contains all levels and their attriutes.
     */
    private LevelCollection levelCollection;

    /**
     * Random used to decide if relevant or distractor animal is drawn.
     */
    private static Random rand = new Random();

    /**
     * Animal pool animals are drawn from.
     */
    private AnimalPool animalPool;

    /**
     * Counts correct matches, used to detect when to praise.
     */
    private int counterCorrect;

    /**
     * Counts incorrect matches, used to detect when to encourage.
     */
    private int counterWrong;

    /**
     * ID of soundfile "Bist du noch da?".
     */
    private int areYouStillThere;

    /**
     * Decides which soundfile is played in inactivity handler.
     */
    private boolean stillThere;

    /**
     * Is true when back has been pressed. Avoid loading StartActivity twice.
     */
    private boolean backPressed;

    /**
     * Used to detect if a new dino has been unlocked.
     */
    private boolean dinoUnlocked;

    /**
     * {@inheritDoc} Sets layout and inactivity handler.
     *
     * @param savedInstanceState Instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        inactivityHandler();
    }

    /**
     * {@inheritDoc} Gets level and animal pool.
     */
    @Override
    protected void onStart() {
        // Get Level and list of animals
        Intent intent = getIntent();
        level = intent.getCharExtra("LEVEL", 'f');
        super.onStart();
        animalPool = (AnimalPool) getApplication();
    }

    /**
     * Change state of level in shared preferences.
     *
     * @param level Level to be changed.
     * @param mode New state of level.
     */
    void writePreferences(Character level, int mode) {
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putInt(level.toString(), mode);
        editor.apply();
    }

    /**
     * Plays reminder after specified duration of inactivity.
     */
    void inactivityHandler() {
        handleInactivity = new Handler();
        runnable = () -> {
            // Make instruction interruptable
            handler = true;
            if (stillThere) {
                playInstruction(areYouStillThere);
                stillThere = false;
            } else {
                playInstruction(currentInstruction);
                stillThere = true;
            }
            startHandler();
        };
    }

    /**
     * Starts handler waiting for 30s of inactivity.
     */
    public void startHandler() {
        super.startHandler();
        handleInactivity.postDelayed(runnable, 30_000); //for 30 seconds
    }

    /**
     * Initialize elements and play level instruction.
     */
    protected void init() {
        super.init();
        levelCollection = new LevelCollection(this);
        winningNumber = levelCollection.getLevel(level).getWinningNumber();
        areYouStillThere = R.raw.are_you_still_there;
        soundLetter = ResourceManager.getRawIdLevel(this, level);
        currentInstruction = ResourceManager.getRawIdInstruction(this, level);

        if (levelCollection.getLevel(level).getIsLeft()) {
            match = findViewById(R.id.left);
            noMatch = findViewById(R.id.right);
        } else {
            match = findViewById(R.id.right);
            noMatch = findViewById(R.id.left);
        }
        ImageView background = findViewById(R.id.level_background);
        background.setImageResource(levelCollection.getLevel(level).getBackgroundID());
        setLetterButton();
        implementEvents();

        // Intro
        // Prepare async to not intefere with loading of other stuff
        AssetFileDescriptor afd = getResources().openRawResourceFd(currentInstruction);
        try {
            mediaPlayer.reset();
            mediaPlayer.setVolume(0.5f, 0.5f);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                afd.getDeclaredLength());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> new Handler().postDelayed(mp::start, 500));

            mediaPlayer.setOnCompletionListener(mp -> {

                new Handler().postDelayed(this::displayAnimal, 500);

                startHandler();
                isRunning = true;
            });
            afd.close();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.e("EX", "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }
    }

    /**
     * Find level's letterButton and create its progress bar.
     */
    void setLetterButton() {
        buttonLetter = findViewById(R.id.button_letter);
        int idImage = ResourceManager.getDrawableIdLetterOutline(this, level);
        buttonLetter.setImageResource(idImage);
        buttonLetter.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // Letter "progress bar"
        idImage = ResourceManager.getDrawableIdLetterFill(this, level);
        //Define a bitmap with the same size as the view
        Bitmap bm = BitmapFactory.decodeResource(getResources(), idImage);
        splitImage(bm);
    }

    /**
     * Draws animal according to level difficulty, displays them, and plays their name.
     */
    @SuppressLint("ClickableViewAccessibility")
    void displayAnimal() {
        animal.setOnTouchListener(this);

        // Level done
        if (correctMatches == winningNumber) {
            allowedToStartMediaPlayer = false;
            stopHandler();
            playInstruction(R.raw.complete_sound);
            mediaPlayer.setOnCompletionListener(mp -> cameraFlash());
            return;
        }
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        middle.addView(animal);

        // n% chance correct letter
        if (rand.nextInt(100) < levelCollection.getLevel(level).getDifficulty()) {
            animalID = animalPool.getAnimalPool().getAnimal(level);
            relevant = true;
        } else {
            animalID = animalPool.getAnimalPool().getDistractorAnimal(level);
            relevant = false;
        }
        animal.setImageResource(animalID);
        String name = getResources().getResourceEntryName(animalID);
        name = name.replace("animal", "");
        soundAnimal = ResourceManager.getRawIdAnimal(this, name);
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(this, soundAnimal);
        if (!backPressed) {
            mediaPlayer.setVolume(0.5f, 0.5f);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> allowedToStartMediaPlayer = true);
        }
        positionAnimal(false);
    }

    /**
     * Plays praise or encouragement and waits for its completion.
     *
     * @param instruction Praise or encouragement to be played.
     */
    void praiseEncourage(int instruction) {
        playInstruction(instruction);
        mediaPlayer.setOnCompletionListener(mp -> displayAnimal());
    }

    /**
     * Displays dino with an animation and praise.
     */
    void displayDino() {
        ViewGroup owner = (ViewGroup) animal.getParent();
        owner.removeView(animal);
        LinearLayout dino = findViewById(R.id.layout_top);
        dino.addView(animal);
        animal.setVisibility(View.VISIBLE);
        animalID = ResourceManager.getDrawableIdDino(this, level);
        animal.setImageResource(animalID);
        positionAnimal(true);
        animal.setY(animal.getY() - 100);
        playInstruction(ResourceManager.getRawIdMuseum(this, level));
        mediaPlayer.setOnCompletionListener(mp -> leaveLevel());
        animal.startAnimation(zoom);
    }

    /**
     * Updates shared preferences when level is completed.
     */
    void levelCompleted() {
        if (availableLevels.getInt(level.toString(), 0) == LevelState.UNLOCKED.ordinal()) {
            dinoUnlocked = true;
        }
        writePreferences(level, LevelState.COMPLETED.ordinal());
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putBoolean("Tutorial", true);
        editor.apply();
        if (level != 't') {
            int index = levels.indexOf(level);
            Character nextLevel = levels.get(index + 1);
            if (availableLevels.getInt(nextLevel.toString(), 0) == LevelState.LOCKED.ordinal()) {
                writePreferences(nextLevel, LevelState.UNLOCKED.ordinal());
            }
        }
    }

    /**
     * Simulates camera flash with animation and sound.
     */
    public void cameraFlash() {
        // White
        ImageView layover = findViewById(R.id.flash);
        new Handler().postDelayed(() -> layover.setVisibility(View.VISIBLE), 500);

        flash.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                playSound(sounds[shortSounds.CAMERA.ordinal()]);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layover.setBackground(null);
                if (dinoUnlocked && !backPressed) {
                    displayDino();
                    return;
                }
                if (!backPressed) {
                    leaveLevel();
                }
            }
        });
        layover.startAnimation(flash);
    }

    /**
     * Resets animalPool and finishes level.
     */
    void leaveLevel() {
        animalPool.getAnimalPool().reset();
        Intent intent = new Intent(this, StartActivity.class);
        Handler handler = new Handler();
        handler.postDelayed(() -> startActivity(intent), 1000);
        handler.postDelayed(this::finish, 1000);
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
            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_EXITED:
                view.setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DROP:
                LinearLayout container = (LinearLayout) layoutview;
                // Match
                if (relevant && container.getId() == match.getId()) {
                    Objects
                        .requireNonNull(animalPool.getAnimalPool().getAnimalMapCurrent().get(level))
                        .remove(Integer.valueOf(animalID));
                    counterCorrect++;
                    correctMatches++;
                    counterWrong = 0;
                    dragAnimal(view, container, shortSounds.CORRECT.ordinal());
                    addProgress(correctMatches);
                    if (correctMatches == winningNumber) {
                        levelCompleted();
                        new Handler().postDelayed(() -> animal.setVisibility(View.INVISIBLE), 500);
                    }
                }

                // No Match
                else if (!relevant && container.getId() == noMatch.getId()) {
                    counterCorrect++;
                    counterWrong = 0;
                    dragAnimal(view, container, shortSounds.CORRECT.ordinal());
                    Character lvl = getResources().getResourceEntryName(animalID).charAt(0);
                    Objects
                        .requireNonNull(animalPool.getAnimalPool().getAnimalMapCurrent().get(lvl))
                        .remove(Integer.valueOf(animalID));
                    changeColor();
                    colorChange++;
                }

                // Wrong
                else if (container.getId() != middle.getId()) {
                    counterWrong++;
                    counterCorrect = 0;
                    if (correctMatches > 0) {
                        removeProgress(correctMatches);
                        correctMatches--;
                    }
                    dragAnimal(view, container, shortSounds.WRONG.ordinal());
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
     * Called when animal is dropped. Depending on previous performance a random praise or
     * encouragement is triggered.
     *
     * @param view View to be dropped.
     * @param container Container to accept view.
     */
    void dropAnimal(View view, LinearLayout container) {
        super.dropAnimal(view, container);
        // Praise randomly
        if (counterCorrect == 3) {
            counterCorrect = 0;
            int number = rand.nextInt(6) + 1;
            int praise = ResourceManager.getRawIdPraise(this, number);
            // Avoid animalSound being cut off by praise/encouragement
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.setOnCompletionListener(mp -> praiseEncourage(praise));
            } else {
                praiseEncourage(praise);
            }
            // encourage randomly
        } else if (counterWrong == 3) {
            counterWrong = 0;
            int number = rand.nextInt(4) + 1;
            int encourage = ResourceManager.getRawIdEncourage(this, number);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.setOnCompletionListener(mp -> praiseEncourage(encourage));
            } else {
                praiseEncourage(encourage);
            }
        } else {
            view.postDelayed(this::displayAnimal, 1000);
        }
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
            } else {
                progress.startAnimation(scaleHalf);
                view.startAnimation(scaleHalf);
            }
        } else if (view.getId() == buttonSpeaker.getId()) {
            if (!mediaPlayer.isPlaying() && allowedToStartMediaPlayer) {
                view.startAnimation(scale);
                playInstruction(soundAnimal);
            } else
            // Button inactive / no sound
            {
                view.startAnimation(scaleHalf);
            }
        }
        // Back button
        else {
            backPressed = true;
            buttonBack.setEnabled(false);
            buttonSpeaker.setEnabled(false);
            buttonLetter.setEnabled(false);
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
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    // reset animal pool because we leave the level
                    animalPool.getAnimalPool().reset();
                    startActivity(intent);
                    finish();
                }
            });
            view.startAnimation(scale);
        }
    }
}
