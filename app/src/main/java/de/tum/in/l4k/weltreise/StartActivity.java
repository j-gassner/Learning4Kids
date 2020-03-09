package de.tum.in.l4k.weltreise;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

public class StartActivity extends ScrollActivity implements View.OnClickListener {

    boolean tutorialRunning;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
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
            tutorialRunning = true;
            SharedPreferences.Editor editor = availableLevels.edit();
            editor.putBoolean("Tutorial", true);
            editor.apply();
            tutorialMuseum();
        }
    }

    void loadAnimations() {
        super.loadAnimations();
        locked = AnimationUtils.loadAnimation(this, R.anim.locked);

    }
    void writeInitialLevels(boolean reset) {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        if (!availableLevels.contains("f") || reset) {
            for (char level : levels) {
                editor.putInt(Character.toString(level), levelState.LOCKED.ordinal());
            }
            editor.putInt("f", levelState.UNLOCKED.ordinal());
            editor.apply();
        }
    }

    void resetLevels() {
        writeInitialLevels(true);
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putBoolean("Tutorial", false);
        editor.apply();
        assignScrollElements();

    }
    // For testing only

    void unlockLevels() {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        for (char level : levels) {
            editor.putInt(Character.toString(level), levelState.COMPLETED.ordinal());
        }
        //editor.putInt("f", levelState.UNLOCKED.ordinal());
        editor.apply();
        assignScrollElements();

    }

    void tutorialMuseum() {
        // Explain museum
        ImageView arrow = findViewById(R.id.button_point_museum);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);

        playInstruction(R.raw.instruction_museum);
        mp.setOnCompletionListener(mp -> {
            tutorialReset();
            arrow.setVisibility(View.INVISIBLE);
        });
    }

    void tutorialReset() {
        ImageView arrow = findViewById(R.id.button_point_reset);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 0);
        playInstruction(R.raw.instruction_reset);
        mp.setOnCompletionListener(mp -> {
            arrow.setVisibility(View.INVISIBLE);
            tutorialTutorial();
        });

    }

    void tutorialTutorial() {
        ImageView arrow = findViewById(R.id.button_point_tutorial);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 0);
        playInstruction(R.raw.instruction_tutorial);
        mp.setOnCompletionListener(mp -> {
            arrow.setVisibility(View.INVISIBLE);
            tutorialNowYou();
        });
    }

    void tutorialNowYou() {
        ImageView arrow = findViewById(R.id.button_point_f);
        arrow.setVisibility(View.VISIBLE);
        playInstruction(R.raw.instruction_now_you);
        mp.setOnCompletionListener(mp -> {
            arrow.setVisibility(View.INVISIBLE);
            tutorialRunning = false;
        });


    }

    void snap(boolean left) {
        LinearLayout scroll = findViewById(R.id.layout_scroll);
        int horizontalWidth = hsv.getMeasuredWidth();
        int horizontalHeight = hsv.getMeasuredHeight();
        int centerX = hsv.getScrollX() + horizontalWidth / 2;
        int centerY = horizontalHeight / 2;
        int distancePolaroids = scroll.getChildAt(1).getLeft() - scroll.getChildAt(0).getRight();
        Rect hitRect = new Rect();
        for (int i = 0; i < scroll.getChildCount(); i++) {
            View child = scroll.getChildAt(i);
            child.getHitRect(hitRect);
            // 45 coord distance between polaroids
            // Cover entire space
            hitRect.right += 22;
            hitRect.left -= 23;

            if (hitRect.contains(centerX, centerY)) {
                if (left) {
                    int x = (child.getLeft() - (horizontalWidth / 2)) + (child.getWidth() / 2);
                    hsv.smoothScrollTo(x - (child.getWidth() + distancePolaroids), 0);
                } else {
                    int x = (child.getLeft() - (horizontalWidth / 2)) + (child.getWidth() / 2);
                    hsv.smoothScrollTo(x + (child.getWidth() + distancePolaroids), 0);
                }
                break;
            }
        }
    }

    public void assignScrollElements() {
        //findViewById(R.id.button_unlock).setEnabled(false);
        for (char button : levels) {
            int idImage;
            int idButton = getResources()
                .getIdentifier("button_" + button, "id", this.getPackageName());
            ImageButton iB = findViewById(idButton);
            if (availableLevels.getInt(button + "", 0) == levelState.COMPLETED.ordinal()) {

                idImage = getResources()
                    .getIdentifier(button + "_polaroid", "drawable", this.getPackageName());

            } else if (availableLevels.getInt(button + "", 0) == levelState.UNLOCKED.ordinal()) {

                idImage = getResources()
                    .getIdentifier(button + "_polaroid_unlocked", "drawable",
                        this.getPackageName());
            } else {
                idImage = getResources()
                    .getIdentifier(button + "_polaroid_locked", "drawable", this.getPackageName());
            }
            iB.setImageResource(idImage);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                if (loaded) {
                    soundPool.play(button, 1f, 1f, 1, 0, 1f);
                }
                if (mp.isPlaying()) {
                    mp.stop();
                }
                resetLevels();
                touchy();
            })
            .setNegativeButton("Nein", (dialog, id) -> {
                // if this button is clicked, just close
                // the dialog_shape box and do nothing
                if (loaded) {
                    soundPool.play(button, 1f, 1f, 1, 0, 1f);
                }
                if (mp.isPlaying()) {
                    mp.stop();
                }
                dialog.cancel();
                touchy();
            }).setOnCancelListener(dialog -> {
            if (mp.isPlaying()) {
                mp.stop();
            }
            touchy();
        });

        // create alert dialog_shape
        AlertDialog alertDialog = alertDialogBuilder.create();
        assert (alertDialog != null);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.chalk);
        alertDialog.getWindow().
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
        buttonPositive.setTextColor(getResources().getColor(R.color.green));
        buttonNegative.setTypeface(typeface, Typeface.BOLD);
        buttonNegative.setTextColor(getResources().getColor(R.color.red));

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        buttonPositive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        buttonNegative.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);

        // Set dialog_shape focusable so we can avoid touching outside:
        alertDialog.getWindow().
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        findViewById(R.id.button_reset).setEnabled(true);

    }

    void noTouchy() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    void touchy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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
                            if (loaded) {
                                soundPool.play(button, 1f, 1f, 1, 0, 1f);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @RequiresApi(api = VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            finish();
                            finishAffinity();
                        }
                    });
                    view.startAnimation(scale);
                } else {
                    view.startAnimation(scaleHalf);
                }
                return;
            case R.id.button_reset:
                if (!tutorialRunning) {

                    scale.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                            if (loaded) {
                                soundPool.play(button, 1f, 1f, 1, 0, 1f);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                            if (loaded) {
                                soundPool.play(button, 1f, 1f, 1, 0, 1f);
                            }
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
                            if (loaded) {
                                soundPool.play(button, 1f, 1f, 1, 0, 1f);
                            }
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
                    public void onAnimationEnd(Animation animation) {
                        snap(true);
                        touchy();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

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
                    public void onAnimationEnd(Animation animation) {
                        snap(false);
                        touchy();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(scaleHalf);

                return;

            default:
                String name = getResources().getResourceEntryName(view.getId());
                level = name.charAt(7);
        }

        // Only playable levels

        if (availableLevels.getInt(level.toString(), 0) != levelState.LOCKED.ordinal()
            && !tutorialRunning) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("LEVEL", level);
            scaleHalf.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    if (loaded) {
                        soundPool.play(button, 1f, 1f, 1, 0, 1f);
                    }
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