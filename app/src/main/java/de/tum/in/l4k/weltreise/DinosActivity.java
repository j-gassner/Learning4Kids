package de.tum.in.l4k.weltreise;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import java.util.Objects;

/**
 * Class containing a dinosaur for each level as a reward.
 *
 * @author Josefine Gaßner
 */

public class DinosActivity extends ScrollActivity {

    /**
     * Animation for when unlocked dino is clicked.
     */
    private static Animation dino;

    /**
     * {@inheritDoc} Sets layout.
     *
     * @param savedInstanceState Instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinos);
    }

    /**
     * {@inheritDoc} Assigns dino images depending on level state.
     */
    @Override
    protected void onStart() {
        super.onStart();
        assignScrollElements();
    }

    /**
     * {@inheritDoc} Removes dark tint from dino if necessary.
     */
    @Override
    protected void onResume() {
        super.onResume();
        removeTint();
    }

    /**
     * Finds animations only used in this activity.
     */
    void loadAnimations() {
        super.loadAnimations();
        dino = AnimationUtils.loadAnimation(this, R.anim.dino_anim);
        locked = AnimationUtils.loadAnimation(this, R.anim.dino_locked_anim);
    }

    /**
     * Checks whether a level is completed and sets dino images accordingly.
     */
    public void assignScrollElements() {
        for (Character level : levels) {
            int idButton = ResourceManager.getIdDino(this, level);
            ImageButton imageButton = findViewById(idButton);
            int idImage = ResourceManager.getDrawableIdDino(this, level);
            Drawable draw = ContextCompat.getDrawable(this, idImage);
            if (availableLevels.getInt(level.toString(), 0) != LevelState.COMPLETED.ordinal()) {
                Objects.requireNonNull(draw)
                    .setColorFilter(ContextCompat.getColor(this, R.color.dark),
                    PorterDuff.Mode.SRC_ATOP);
            }
            imageButton.setImageResource(idImage);
        }
    }

    /**
     * Snaps scrollview when buttons are used for scrolling.
     *
     * @param left Indicates whether left button was clicked.
     */
    void snap(boolean left) {
        LinearLayout scroll = findViewById(R.id.layout_scroll);
        int horizontalWidth = horizontalScrollView.getMeasuredWidth();
        int horizontalHeight = horizontalScrollView.getMeasuredHeight();
        int centerX = horizontalScrollView.getScrollX() + horizontalWidth / 2;
        int centerY = horizontalHeight / 2;
        Rect hitRect = new Rect();
        for (int i = 0; i < scroll.getChildCount(); i++) {
            View child = scroll.getChildAt(i);
            child.getHitRect(hitRect);
            // Correct scrolling to the left
            if (left) {
                hitRect.right += 1;
            }
            if (hitRect.contains(centerX, centerY)) {
                if (left) {
                    int x = (child.getRight() - (horizontalWidth / 2));
                    horizontalScrollView.smoothScrollTo(x - (child.getWidth()), 0);
                } else {
                    int x = (child.getLeft() - (horizontalWidth / 2));
                    horizontalScrollView.smoothScrollTo(x + (child.getWidth()), 0);
                }
                break;
            }
        }
    }

    /**
     * Removes dark tint from dino when level is completed.
     */
    void removeTint() {
        for (Character level : levels) {
            int idImage = ResourceManager.getDrawableIdDino(this, level);
            Drawable draw = ContextCompat.getDrawable(this, idImage);
            if (availableLevels.getInt(level.toString(), 0) == LevelState.COMPLETED.ordinal()) {
                Objects.requireNonNull(draw).clearColorFilter();
            }
        }
    }

    /**
     * {@inheritDoc} Manages events depending on which view is clicked.
     *
     * @param view View that is clicked.
     */
    public void onClick(View view) {
        Intent intent = new Intent(this, StartActivity.class);
        switch (view.getId()) {
            case R.id.button_back:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                playSound(button);
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
                    }
                });
                view.startAnimation(scale);
                break;
            case R.id.scroll_right:
                scaleHalf.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        snap(false);
                    }
                });
                view.startAnimation(scaleHalf);
                return;
            case R.id.scroll_left:
                scaleHalf.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        snap(true);
                    }
                });
                view.startAnimation(scaleHalf);
                return;
            // Dino
            default:
                String name = getResources().getResourceEntryName(view.getId());
                String level = "" + name.charAt(5);
                int id = ResourceManager.getRawIdDino(this, name);
                if (availableLevels.getInt(level, 0) == LevelState.COMPLETED.ordinal()
                    && !mediaPlayer
                    .isPlaying()) {
                    view.startAnimation(dino);
                    playInstruction(id);
                } else {
                    view.startAnimation(locked);
                }
                break;
        }
    }
}
