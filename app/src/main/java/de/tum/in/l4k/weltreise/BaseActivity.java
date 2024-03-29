package de.tum.in.l4k.weltreise;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import java.util.ArrayList;

/**
 * Class serving as a base for all activities except the MainActivity.
 *
 * @author Josefine Gaßner
 */

public abstract class BaseActivity extends WindowManagementActivity {

    /**
     * Letter of current level.
     */
    Character level;

    /**
     * Mediaplayer for playing longer sequences.
     */
    MediaPlayer mediaPlayer = new MediaPlayer();

    /**
     * Soundpool for playing short sounds.
     */
    SoundPool soundPool;

    /**
     * Contains all levels including their attributes.
     */
    static ArrayList<Character> levels = new ArrayList<>();

    /**
     * Saves the state of the levels and the tutorial.
     */
    static SharedPreferences availableLevels;

    /**
     * Animations indicating that a button is active.
     */
    static Animation scale;

    /**
     * Animation indicating that a button is currently inactive.
     */
    static Animation scaleHalf;

    /**
     * Whether soundPool is loaded or not.
     */
    boolean loaded;

    /**
     * {@inheritDoc} Loads button sounds, animations, shared preferences, and levels on creation.
     *
     * @param savedInstanceState Instance state.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadButtonSounds();
        loadAnimations();
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        levels = new LevelCollection(this).getLevels();
    }

    /**
     * {@inheritDoc} Mutes mediaPlayer on pause.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.setVolume(0f, 0f);
    }

    /**
     * {@inheritDoc} Releases and sets mediaPlayer and soundPool to null when activity is
     * destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
        soundPool.release();
        soundPool = null;
    }

    /**
     * Makes soundPool play a sound.
     *
     * @param resID ID of the soundfile.
     */
    void playSound(int resID) {
        if (loaded) {
            soundPool.play(resID, 1f, 1f, 1, 0, 1f);
        }
    }

    /**
     * Makes MediaPlayer play a soundfile.
     *
     * @param resID ID of the soundfile
     */
    void playInstruction(int resID) {
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(this, resID);

        // Volume at 0.5f fits best with volume of soundPool
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.start();
    }

    /**
     * Base for loading sounds used in soundPool. Indicates with loaded = true when loading was
     * successful.
     */
    void loadButtonSounds() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();

        soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> loaded = true);
    }

    /**
     * Loads animations that all activities use.
     */
    void loadAnimations() {
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);
        scaleHalf = AnimationUtils.loadAnimation(this, R.anim.button_inactive_anim);
    }
}
