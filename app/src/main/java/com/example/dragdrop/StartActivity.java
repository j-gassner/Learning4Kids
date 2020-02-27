package com.example.dragdrop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    public static int scrollX = 0;
    public static int scrollY = -1;
    private final static int LOCKED = 0;
    private final static int UNLOCKED = 1;
    private final static int COMPLETE = 2;

    HorizontalScrollView hsv;
    private Character level;
    SharedPreferences availableLevels;
    private Animation scale, scaleHalf, locked;
    MediaPlayer mp = new MediaPlayer();
    SoundPool soundPool;
    boolean loaded, tutorialRunning;
    int button;

    void writeInitialLevels(boolean reset) {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        if (!availableLevels.contains("f") || reset) {
            editor.putInt("f", UNLOCKED);
            editor.putInt("l", LOCKED);
            editor.putInt("r", LOCKED);
            editor.putInt("m", LOCKED);
            editor.putInt("n", LOCKED);
            editor.putInt("i", LOCKED);
            editor.putInt("e", LOCKED);
            editor.putInt("a", LOCKED);
            editor.putInt("o", LOCKED);
            editor.putInt("s", LOCKED);
            editor.putInt("b", LOCKED);
            editor.putInt("t", LOCKED);
            editor.apply();
        }
    }

    void resetLevels() {
        writeInitialLevels(true);
        SharedPreferences.Editor editor = availableLevels.edit();
        editor.putBoolean("Tutorial", false);
        editor.apply();
        assignButtons();

    }

    // For testing only
    void unlockLevels() {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        editor.putInt("f", COMPLETE);
        editor.putInt("l", COMPLETE);
        editor.putInt("r", COMPLETE);
        editor.putInt("m", COMPLETE);
        editor.putInt("n", COMPLETE);
        editor.putInt("i", COMPLETE);
        editor.putInt("e", COMPLETE);
        editor.putInt("a", COMPLETE);
        editor.putInt("o", COMPLETE);
        editor.putInt("s", COMPLETE);
        editor.putInt("b", COMPLETE);
        editor.putInt("t", COMPLETE);
        editor.apply();
        assignButtons();

    }


    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void buttonSound() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();

        soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> loaded = true);
        button = soundPool.load(this, R.raw.button, 1);

    }

    void playInstruction(int resID) {
        mp.reset();
        mp = MediaPlayer.create(this, resID);
        mp.setVolume(0.5f, 0.5f);
        mp.start();

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
            //touchy();
            tutorialRunning = false;
        });


    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        hideSystemUI();
        hsv = findViewById(R.id.scroll_horizontal);
        ImageButton arrowRight = findViewById(R.id.scroll_right);
        ImageButton arrowLeft = findViewById(R.id.scroll_left);
        arrowRight.setVisibility(View.VISIBLE);
        View view = hsv.getChildAt(hsv.getChildCount() - 1);

        hsv.getViewTreeObserver()
            .addOnScrollChangedListener(() -> {
                int diff = (view.getRight() - (hsv.getWidth() + hsv.getScrollX()));
                int scrollX = hsv.getScrollX();
                if (scrollX > 0 && diff != 0) {
                    arrowRight.setVisibility(View.VISIBLE);
                    arrowLeft.setVisibility(View.VISIBLE);
                } else if (scrollX == 0) {
                    arrowRight.setVisibility(View.VISIBLE);
                    arrowLeft.setVisibility(View.INVISIBLE);
                } else {
                    arrowRight.setVisibility(View.INVISIBLE);
                    arrowLeft.setVisibility(View.VISIBLE);
                }

            });

        // Works but doesn't really feel good when scrolling
        /*hsv.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                snapCenter();
                return true;
            } else {
                return false;
            }
        });*/

        writeInitialLevels(false);
        assignButtons();
        buttonSound();
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

    void snapCenter(boolean left) {
        LinearLayout scroll = findViewById(R.id.layout_scroll);
        int horizontalWidth = hsv.getMeasuredWidth();
        int horizontalHeight = hsv.getMeasuredHeight();
        int centerX = hsv.getScrollX() + horizontalWidth / 2;
        int centerY = horizontalHeight / 2;
        int distancePolaroids = scroll.getChildAt(1).getLeft() - scroll.getChildAt(0).getRight();
        //Log.d("COORDSMIDDLE", centerX + " " + centerY);
        Rect hitRect = new Rect();
        for (int i = 0; i < scroll.getChildCount(); i++) {
            View child = scroll.getChildAt(i);
            child.getHitRect(hitRect);
            // 45 coord distance between polaroids
            //Log.d("COORDS", i + " " + hitRect.left +" " + hitRect.right);
            // Cover entire space

            hitRect.right += 22;
            hitRect.left -= 23;

            //Log.d("COORDS", " " + (scroll.getChildAt(1).getLeft() - scroll.getChildAt(0).getRight()));
            //Log.d("WIDTH", "" + child.getWidth());
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

    @Override
    protected void onPause() {
        super.onPause();
        scrollX = hsv.getScrollX();
        scrollY = hsv.getScrollY();
        mp.setVolume(0f, 0f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hsv.post(() -> hsv.scrollTo(scrollX, scrollY));
        //touchy();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mp.release();
        mp = null;
        soundPool.release();
        soundPool = null;

    }


    public void assignButtons() {
        //findViewById(R.id.button_unlock).setEnabled(false);
        scale = AnimationUtils.loadAnimation(this, R.anim.button_anim);
        scaleHalf = AnimationUtils.loadAnimation(this, R.anim.button_inactive_anim);
        locked = AnimationUtils.loadAnimation(this, R.anim.locked);


        for (char button : Globals.levels) {
            int idImage;
            int idButton = getResources()
                .getIdentifier("button_" + button, "id", this.getPackageName());
            ImageButton iB = findViewById(idButton);
            if (availableLevels.getInt(button + "", 0) == COMPLETE) {

                idImage = getResources()
                    .getIdentifier(button + "_polaroid", "drawable", this.getPackageName());

            } else if (availableLevels.getInt(button + "", 0) == UNLOCKED) {

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


    // DialogFragment is deprecated now
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

        // Icons
        /*Drawable yes = getResources().getDrawable(R.drawable.button_yes, getTheme());
        Drawable no = getResources().getDrawable(R.drawable.button_exit, getTheme());*/

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        buttonPositive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);
        buttonNegative.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 46);

        /*buttonPositive.setCompoundDrawablesWithIntrinsicBounds(yes, null, null, null);
        buttonNegative.setCompoundDrawablesWithIntrinsicBounds(no, null, null, null);*/

        // Set dialog_shape focusable so we can avoid touching outside:
        alertDialog.getWindow().
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        findViewById(R.id.button_reset).setEnabled(true);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
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
                        snapCenter(true);
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
                        snapCenter(false);
                        touchy();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(scaleHalf);

                return;

            case R.id.button_a:
                level = 'a';
                break;
            case R.id.button_b:
                level = 'b';
                break;
            case R.id.button_e:
                level = 'e';
                break;
            case R.id.button_f:
                level = 'f';
                break;
            case R.id.button_i:
                level = 'i';
                break;
            case R.id.button_l:
                level = 'l';
                break;
            case R.id.button_m:
                level = 'm';
                break;
            case R.id.button_n:
                level = 'n';
                break;
            case R.id.button_o:
                level = 'o';
                break;
            case R.id.button_r:
                level = 'r';
                break;
            case R.id.button_s:
                level = 's';
                break;
            case R.id.button_t:
                level = 't';
                break;
            default:
                break;

        }

        // Only playable levels

        if (availableLevels.getInt(level.toString(), 0) != LOCKED && !tutorialRunning) {
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

}