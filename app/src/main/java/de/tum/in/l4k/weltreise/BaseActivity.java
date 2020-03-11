package de.tum.in.l4k.weltreise;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class serving as a base for all activities except the MainActivity.
 *
 * @author Josefine Gaßner
 */

public abstract class BaseActivity extends WindowManagement {

    static ArrayList<Character> levels = new ArrayList<>(
        Arrays.asList('f', 'l', 'r', 'm', 'n', 'i', 'e', 'a', 'o', 's', 'b', 't'));
    static Character level;
    MediaPlayer mediaPlayer = new MediaPlayer();
    SoundPool soundPool;
    SharedPreferences availableLevels;
    Animation scale, scaleHalf;

    /**
     * Wheter soundPool is loaded or not.
     */
    boolean loaded;
    enum levelState {LOCKED, UNLOCKED, COMPLETED}

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadButtonSounds();
        loadAnimations();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.setVolume(0f, 0f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
        soundPool.release();
        soundPool = null;
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
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
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
