package de.tum.in.l4k.weltreise;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
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
import java.util.ArrayList;
import java.util.Arrays;

public abstract class BaseGameActivity extends BaseActivity implements View.OnTouchListener,
    View.OnDragListener, View.OnClickListener {

    int winningNumber, soundAnimal, soundLetter, animalID, colorChange, correctMatches, currentInstruction;
    boolean sound, fit, isRunning, handler;
    ImageView animal;
    LinearLayout match, middle, noMatch;
    Handler handleInactivity;
    Runnable runnable;
    ArrayList<ImageView> fragments;
    ArrayList<Integer> colors = new ArrayList<>(
        Arrays.asList(R.color.red, R.color.blue, R.color.yellow, R.color.pink,
            R.color.green, R.color.orange, R.color.purple));
    ImageButton buttonLetter, buttonSpeaker, buttonBack;
    Animation zoom, flash;
    static int[] sounds = new int[4];
    enum shortSounds {BUTTON, CORRECT, WRONG, CAMERA}

    abstract void inactivityHandler();
    abstract void displayAnimal();

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        removeColor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHandler();
    }

    /**
     * Called when user touches the screen. Stops handler.
     */
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

    /**
     * Called when user touches animal with intention to drag it.
     *
     * @param view View that is touched.
     * @param motionEvent Kind of motion used.
     */
    @RequiresApi(api = VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(null, shadowBuilder, view, View.DRAG_FLAG_OPAQUE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Starts handler handling user inactivity.
     */
    public void startHandler() {
        isRunning = true;
    }

    /**
     * Stops handler handling user inactivity.
     */
    public void stopHandler() {
        isRunning = false;
        handleInactivity.removeCallbacks(runnable);
    }

    /**
     * Sets onTouch- and onDragListeners
     */
    @SuppressLint("ClickableViewAccessibility")
    void implementEvents() {
        animal.setOnTouchListener(this);
        middle.setOnDragListener(this);
        match.setOnDragListener(this);
        noMatch.setOnDragListener(this);
    }

    /**
     * Prepares buttons, animations, sounds, and some views.
     */
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    protected void init() {
        loadButtons();
        loadAnimations();
        buttonSounds();

        // Match and noMatch need to be set later as they can be either left or right
        animal = findViewById(R.id.animal);
        middle = findViewById(R.id.middle);
    }

    /**
     * Finds buttons used by all extending activities.
     */
    void loadButtons() {
        buttonLetter = findViewById(R.id.button_letter);
        buttonSpeaker = findViewById(R.id.button_animal);
        buttonBack = findViewById(R.id.button_back);
    }

    /**
     * Finds animations used by all extending activities.
     */
    void loadAnimations() {
        super.loadAnimations();
        zoom = AnimationUtils.loadAnimation(this, R.anim.zoom);
        flash = AnimationUtils.loadAnimation(this, R.anim.flash);
    }

    /**
     * Adds all necessary sounds to soundPool.
     */
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void buttonSounds() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();
        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();
        soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> loaded = true);
        sounds[shortSounds.BUTTON.ordinal()] = soundPool.load(this, R.raw.button, 1);
        sounds[shortSounds.CORRECT.ordinal()] = soundPool.load(this, R.raw.sound_positive, 1);
        sounds[shortSounds.WRONG.ordinal()] = soundPool.load(this, R.raw.sound_negative, 1);
        sounds[shortSounds.CAMERA.ordinal()] = soundPool.load(this, R.raw.camera, 1);
    }

    /**
     * Splits an image into fragments horizontally depending on winningNumber.
     *
     * @param image Image to be split.
     */
    void splitImage(Bitmap image) {
        fragments = new ArrayList<>(winningNumber);
        int y = 0;
        for (int i = 0; i < winningNumber; i++) {
            ImageView frag = new ImageView(getApplicationContext());
            frag.setImageBitmap(Bitmap.createBitmap(image, 0, y, image.getWidth(),
                image.getHeight() / winningNumber));
            fragments.add(frag);
            y += (image.getHeight() / winningNumber);
        }
    }

    /**
     * Adds new fragment to letter to indicate progress when relevant animals was dragged correctly.
     *
     * @param step Which fragment will be added.
     */
    void showProgress(int step) {
        RelativeLayout progress = findViewById(R.id.image_progress);
        int chunkSize = progress.getHeight() / winningNumber;
        ImageView fragment = fragments.get(fragments.size() - step);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(progress.getWidth(),
            chunkSize);
        params.leftMargin = 0;

        // +1 to avoid lines in animation
        params.topMargin = progress.getHeight() - chunkSize * step + 1;
        progress.addView(fragment, params);
        fragment.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    /**
     * Removes fragment when mistake was made.
     *
     * @param step Which fragment to be removed.
     */
    void removeProgress(int step) {
        if (step == 0) {
            return;
        }
        RelativeLayout progress = findViewById(R.id.image_progress);
        ImageView fragment = fragments.get(fragments.size() - step);
        progress.removeView(fragment);
    }

    /**
     * Changes color of letterButton and filling when irrelevant animals was dragged correctly.
     * Works by coloring each fragment.
     */
    void changeColor() {
        if (colorChange >= colors.size()) {
            colorChange %= colors.size();
        }

        // i is red per default
        if (level == 'i' && colorChange == 0) {
            colorChange++;
        }
        for (ImageView fragment : fragments) {
            DrawableCompat.setTint(fragment.getDrawable(),
                ContextCompat.getColor(getApplicationContext(), colors.get(colorChange)));
        }
        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter);
        DrawableCompat.setTint(iB.getDrawable(), ContextCompat
            .getColor(getApplicationContext(), colors.get((colorChange + 1) % colors.size())));
    }

    /**
     * Resets letterButton to initial appearance.
     */
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void removeColor() {
        for (ImageView fragment : fragments) {
            fragment.getDrawable().setTintList(null);
        }
        // Change color of letter
        ImageButton iB = findViewById(R.id.button_letter);
        iB.getDrawable().setTintList(null);

    }

    /**
     * Scales image's longer side to fit 300dp and other side accordingly.
     * Positions it to bottom center of screen.
     *
     * @param dino Indicates if dino or animal is to be positioned.
     */
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void positionAnimal(boolean dino) {
        if (!dino) {
            // Center ImageView
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) animal.getLayoutParams();
            params.gravity = Gravity.CENTER_HORIZONTAL;
            animal.setLayoutParams(params);
        }

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


    /**
     * Called when animal is dragged to another view that accepts it.
     * Shows animation in letterButton.
     * A sound indicates whether it was right or wrong.
     *
     * @param view View to be dragged.
     * @param container Where view is dragged to.
     * @param sound Correct or wrong sound.
     */
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

    /**
     * Called when view is dropped into container.
     * View is removed from old and added to new container.
     *
     * @param view View to be dropped.
     * @param container Container to accept view.
     */
    @RequiresApi(api = VERSION_CODES.N)
    void dropAnimal(View view, LinearLayout container) {
        sound = false;
        // Accept view
        ViewGroup owner = (ViewGroup) view.getParent();
        owner.removeView(view);
        container.addView(view);
        // Avoid animal being dragged away again
        view.setOnTouchListener(null);
    }
}





