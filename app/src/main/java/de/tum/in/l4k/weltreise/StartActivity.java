package de.tum.in.l4k.weltreise;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import java.util.Objects;

/**
 * Class representing the main menu. All functions are started from here.
 *
 * @author Josefine Gaßner
 */

public class StartActivity extends ScrollActivity implements View.OnClickListener {

    /**
     * Detects if tutorial is currently running.
     */
    private boolean tutorialRunning;

    /**
     * {@inheritDoc} Prepares layout and starts second half of tutorial if necessary.
     *
     * @param savedInstanceState Instance state.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        writeInitialLevels(false);
        assignScrollElements();
        Intent intent = getIntent();
        boolean tutorial = intent.getBooleanExtra("Tutorial", false);

        // Activity comes from tutorial
        if (tutorial) {
            // Disable scrolling
            final HorizontalScrollView horizontalScrollView = findViewById(R.id.scroll_horizontal);
            horizontalScrollView.setOnTouchListener((view, event) -> true);
            tutorialRunning = true;
            SharedPreferences.Editor editor = availableLevels.edit();
            editor.putBoolean("Tutorial", true);
            editor.apply();
            tutorialMuseum();
        }
    }

    /**
     * Ignores touch of user.
     */
    void noTouchy() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    /**
     * Makes window touchable.
     */
    void touchy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    /**
     * Snaps scrollview when buttons are used for scrolling.
     *
     * @param left Whether left button has been pressed.
     */
    void snap(boolean left) {
        LinearLayout scroll = findViewById(R.id.layout_scroll);
        int horizontalWidth = horizontalScrollView.getMeasuredWidth();
        int horizontalHeight = horizontalScrollView.getMeasuredHeight();
        int centerX = horizontalScrollView.getScrollX() + horizontalWidth / 2;
        int centerY = horizontalHeight / 2;
        int distancePolaroids = scroll.getChildAt(1).getLeft() - scroll.getChildAt(0).getRight();
        Rect hitRect = new Rect();
        for (int i = 0; i < scroll.getChildCount(); i++) {
            View child = scroll.getChildAt(i);
            child.getHitRect(hitRect);
            // 45 coord distance between polaroids
            // Cover entire space
            hitRect.right += distancePolaroids / 2;
            hitRect.left -= (distancePolaroids / 2 + 1);

            if (hitRect.contains(centerX, centerY)) {
                if (left) {
                    int x = (child.getLeft() - (horizontalWidth / 2)) + (child.getWidth() / 2);
                    horizontalScrollView
                        .smoothScrollTo(x - (child.getWidth() + distancePolaroids), 0);
                } else {
                    int x = (child.getLeft() - (horizontalWidth / 2)) + (child.getWidth() / 2);
                    horizontalScrollView
                        .smoothScrollTo(x + (child.getWidth() + distancePolaroids), 0);
                }
                break;
            }
        }
    }

    /**
     * Called when reset is pressed asking if the user really wants to reset.
     */
    void alertDialogue() {
        playInstruction(R.raw.question_reset);
        int ui_flags =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
            .setMessage("Wirklich alles zurücksetzen?")
            .setPositiveButton("Ja", (dialog, id) -> {
                playSound(button);
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                resetLevels();
                touchy();
            })
            .setNegativeButton("Nein", (dialog, id) -> {
                playSound(button);
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                dialog.cancel();
                touchy();
            }).setOnCancelListener(dialog -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            touchy();
        });

        // Create alertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        assert (alertDialog != null);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.chalk);
        Objects.requireNonNull(alertDialog.getWindow()).
            setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        // Set full-sreen mode (immersive sticky):
        alertDialog.getWindow().getDecorView().setSystemUiVisibility(ui_flags);
        // Show the alertDialog:
        alertDialog.show();
        TextView textView = alertDialog.findViewById(android.R.id.message);
        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button buttonNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        textView.setTypeface(typeface, Typeface.BOLD);
        buttonPositive.setTypeface(typeface, Typeface.BOLD);
        buttonPositive.setTextColor(ContextCompat.getColor(this, R.color.green));
        buttonNegative.setTypeface(typeface, Typeface.BOLD);
        buttonNegative.setTextColor(ContextCompat.getColor(this, R.color.red));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        buttonPositive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        buttonNegative.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        alertDialog.getWindow().
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        findViewById(R.id.button_reset).setEnabled(true);
    }

    /**
     * Finds remaining animation needed for this activity.
     */
    void loadAnimations() {
        super.loadAnimations();
        locked = AnimationUtils.loadAnimation(this, R.anim.locked);
    }

    /**
     * Checks state of level and sets image of image buttons accordingly.
     */
    public void assignScrollElements() {
        findViewById(R.id.button_unlock).setEnabled(false);
        for (Character button : levels) {
            int idImage;
            int idButton = ResourceManager.getIdButton(this, button);
            ImageButton imageButton = findViewById(idButton);
            if (availableLevels.getInt(button.toString(), 0) == LevelState.COMPLETED.ordinal()) {
                idImage = ResourceManager.getDrawableIdPolaroid(this, button);
            } else if (availableLevels.getInt(button.toString(), 0) == LevelState.UNLOCKED
                .ordinal()) {
                idImage = ResourceManager.getDrawableIdPolaroidUnlocked(this, button);
            } else {
                idImage = ResourceManager.getDrawableIdPolaroidLocked(this, button);
            }
            imageButton.setImageResource(idImage);
        }
    }

    /**
     * Sets levels to inital state with only f being unlocked.
     *
     * @param reset Whether reset was pressed.
     */
    void writeInitialLevels(boolean reset) {
        SharedPreferences.Editor editor = availableLevels.edit();
        // Create SharedPreference
        if (!availableLevels.contains("f") || reset) {
            for (char level : levels) {
                editor.putInt(Character.toString(level), LevelState.LOCKED.ordinal());
            }
            editor.putInt("f", LevelState.UNLOCKED.ordinal());
            editor.apply();
        }
    }

    /**
     * Reset levels to intial state.
     */
    void resetLevels() {
        writeInitialLevels(true);
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putBoolean("Tutorial", false);
        editor.apply();
        assignScrollElements();
    }

    /**
     * Unlocks all levels via invisible button is lower left corner. For testing only.
     */
    void unlockLevels() {
        SharedPreferences.Editor editor = availableLevels.edit();
        for (char level : levels) {
            editor.putInt(Character.toString(level), LevelState.COMPLETED.ordinal());
        }
        editor.apply();
        assignScrollElements();
    }

    /**
     * Starts explanation of museum.
     */
    void tutorialMuseum() {
        // Explain museum
        ImageView arrow = findViewById(R.id.button_point_museum);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);

        playInstruction(R.raw.instruction_museum);
        mediaPlayer.setOnCompletionListener(mp -> {
            tutorialReset();
            arrow.setVisibility(View.INVISIBLE);
        });
    }

    /**
     * Starts explanation of reset button.
     */
    void tutorialReset() {
        ImageView arrow = findViewById(R.id.button_point_reset);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 0);
        playInstruction(R.raw.instruction_reset);
        mediaPlayer.setOnCompletionListener(mp -> {
            arrow.setVisibility(View.INVISIBLE);
            tutorialTutorial();
        });
    }

    /**
     * Starts explanation of tutorialButton.
     */
    void tutorialTutorial() {
        ImageView arrow = findViewById(R.id.button_point_tutorial);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 0);
        playInstruction(R.raw.instruction_tutorial);
        mediaPlayer.setOnCompletionListener(mp -> {
            arrow.setVisibility(View.INVISIBLE);
            tutorialNowYou();
        });
    }

    /**
     * Points to first level.
     */
    void tutorialNowYou() {
        ImageView arrow = findViewById(R.id.button_point_f);
        arrow.setVisibility(View.VISIBLE);
        playInstruction(R.raw.instruction_now_you);
        mediaPlayer.setOnCompletionListener(mp -> {
            arrow.setVisibility(View.INVISIBLE);
            tutorialRunning = false;
            findViewById(R.id.scroll_horizontal).setOnTouchListener(null);
        });
    }

    /**
     * {@inheritDoc} Manages events depending on which view is clicked.
     *
     * @param view View that is clicked.
     */
    @Override
    public void onClick(View view) {
        // Avoid double clicks
        noTouchy();
        switch (view.getId()) {
            case R.id.button_exit:
                if (!tutorialRunning) {
                    scale.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            playSound(button);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            finish();
                            finishAffinity();
                        }
                    });
                    view.startAnimation(scale);
                } else {
                    view.startAnimation(scaleHalf);
                    touchy();
                }
                return;
            case R.id.button_reset:
                if (!tutorialRunning) {
                    scale.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            playSound(button);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            alertDialogue();
                        }
                    });
                    view.startAnimation(scale);
                } else {
                    view.startAnimation(scaleHalf);
                    touchy();
                }
                return;
            // Testing only
            case R.id.button_unlock:
                unlockLevels();
                touchy();
                return;
            case R.id.button_tutorial:
                if (!tutorialRunning) {
                    Intent intentTut = new Intent(this, TutorialActivity.class);
                    scale.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            playSound(button);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            startActivity(intentTut);
                        }
                    });
                    view.startAnimation(scale);
                } else {
                    view.startAnimation(scaleHalf);
                    touchy();
                }
                return;
            case R.id.button_museum:
                if (!tutorialRunning) {
                    Intent intentDino = new Intent(this, DinosActivity.class);
                    scale.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            playSound(button);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            startActivity(intentDino);
                        }
                    });
                    view.startAnimation(scale);
                } else {
                    view.startAnimation(scaleHalf);
                    touchy();
                }
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
                        if (!tutorialRunning) {
                            snap(true);
                        }
                        touchy();
                    }
                });
                view.startAnimation(scaleHalf);
                return;
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
                        if (!tutorialRunning) {
                            snap(false);
                        }
                        touchy();
                    }
                });
                view.startAnimation(scaleHalf);
                return;
            default:
                String name = getResources().getResourceEntryName(view.getId());
                level = name.charAt(7);
        }
        // Only playable levels
        if (availableLevels.getInt(level.toString(), 0) != LevelState.LOCKED.ordinal()
            && !tutorialRunning) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("LEVEL", level);
            scaleHalf.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    playSound(button);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    startActivity(intent);
                }
            });
            view.startAnimation(scaleHalf);
        } else {
            view.startAnimation(locked);
            touchy();
        }
    }
}