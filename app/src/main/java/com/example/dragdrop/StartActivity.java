package com.example.dragdrop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private Animation scale;
    MediaPlayer mp = new MediaPlayer();
    SoundPool soundPool;
    ;
    int button;
    //ArrayList<Character> buttons = new ArrayList<>(Arrays.asList('f', 'l', 'r', 'm', 'n', 'i', 'e', 'a', 'o', 's', 'b', 't'));

    void writeInitialLevels() {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
        if (!availableLevels.contains("f")) {
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

        /*TextView name = findViewById(R.id.name_child);
        name.setText(availableLevels.getString("Name", ""));*/

        // Start tutorial on first start
        /*if(!availableLevels.getBoolean("Tutorial", false)) {
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
        }*/
    }

    void resetLevels() {
        availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        SharedPreferences.Editor editor = availableLevels.edit();

        // Create SharedPreference
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
        editor.putBoolean("Tutorial", false);
        //editor.putString("Name", "");
        editor.apply();
        assignButtons();

    }

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
            (soundPool, sampleId, status) -> Log.d("SOUNDPOOL", "COMPLETE " + button));
        button = soundPool.load(this, R.raw.button, 1);

    }

    void tutorialMuseum() {

        // Disable buttons
        /*findViewById(R.id.button_reset).setEnabled(false);
        findViewById(R.id.button_tutorial).setEnabled(false);
        findViewById(R.id.button_exit).setEnabled(false);
        findViewById(R.id.button_f).setEnabled(false);
        findViewById(R.id.button_museum).setEnabled(false);*/
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
        //noTouchy();

        // 7. Explain museum
        ImageView arrow = findViewById(R.id.button_point_museum);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 500);

        mp.reset();
        mp = MediaPlayer.create(this, R.raw.instruction_museum);
        mp.setVolume(0.5f, 0.5f);
        new Handler().postDelayed(() -> mp.start(), 500);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                tutorialReset();
                arrow.setVisibility(View.INVISIBLE);
            }
        });
        // TODO Text audio

        /*findViewById(R.id.button_reset).setEnabled(true);
        findViewById(R.id.button_tutorial).setEnabled(true);
        findViewById(R.id.button_exit).setEnabled(true);
        findViewById(R.id.button_f).setEnabled(true);
        findViewById(R.id.button_museum).setEnabled(true);
        new Handler().postDelayed(() -> arrow.setVisibility(View.INVISIBLE), 1000);*/

    }

    void tutorialReset() {
        ImageView arrow = findViewById(R.id.button_point_reset);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 0);

        mp.reset();
        mp = MediaPlayer.create(this, R.raw.instruction_reset);
        mp.start();
        mp.setVolume(0.5f, 0.5f);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                arrow.setVisibility(View.INVISIBLE);
                tutorialTutorial();
            }
        });

    }

    void tutorialTutorial() {
        ImageView arrow = findViewById(R.id.button_point_tutorial);
        new Handler().postDelayed(() -> arrow.setVisibility(View.VISIBLE), 0);

        mp.reset();
        mp = MediaPlayer.create(this, R.raw.instruction_tutorial);
        mp.start();
        mp.setVolume(0.5f, 0.5f);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                arrow.setVisibility(View.INVISIBLE);
                tutorialNowYou();
            }
        });

        //new Handler().postDelayed(() -> arrow.setVisibility(View.INVISIBLE), 1000);

    }

    void tutorialNowYou() {
        ImageView arrow = findViewById(R.id.button_point_f);
        arrow.setVisibility(View.VISIBLE);
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.instruction_now_you);
        mp.setVolume(0.5f, 0.5f);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                arrow.setVisibility(View.INVISIBLE);
                touchy();
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                /*findViewById(R.id.button_reset).setEnabled(true);
                findViewById(R.id.button_tutorial).setEnabled(true);
                findViewById(R.id.button_exit).setEnabled(true);
                findViewById(R.id.button_f).setEnabled(true);
                findViewById(R.id.button_museum).setEnabled(true);      */
            }
        });


    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        hideSystemUI();
        hsv = findViewById(R.id.scroll_horizontal);
        ImageView arrowRight = findViewById(R.id.scroll_right);
        ImageView arrowLeft = findViewById(R.id.scroll_left);
        arrowRight.setVisibility(View.VISIBLE);
        View view = hsv.getChildAt(hsv.getChildCount() - 1);
        int diff = (view.getBottom() - (hsv.getWidth() + hsv.getScrollX()));
        // Log.d("MAXSCROLL", "" + diff);
        hsv.getViewTreeObserver()
            .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    int diff = (view.getRight() - (hsv.getWidth() + hsv.getScrollX()));
                    Log.d("MAXSCROLL", "" + diff);
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

                }
            });

        writeInitialLevels();
        assignButtons();
        buttonSound();
        Intent intent = getIntent();
        boolean tutorial = intent.getBooleanExtra("Tutorial", false);
        if (tutorial) {
            noTouchy();
            SharedPreferences.Editor editor = availableLevels.edit();
            editor.putBoolean("Tutorial", true);
            editor.apply();
            tutorialMuseum();
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

        //char[] buttons = {'f', 'l', 'r', 'm', 'n', 'i', 'e', 'a', 'o', 's', 'b', 't'};
        for (char button : Globals.levels) {
            int idImage;
            int idButton = getResources()
                .getIdentifier("button_" + button, "id", this.getPackageName());
            //Log.d("IDBUTTON", " " + idButton);
            ImageButton iB = findViewById(idButton);
            if (availableLevels.getInt(button + "", 0) == COMPLETE) {

                idImage = getResources()
                    .getIdentifier(button + "_polaroid", "drawable", this.getPackageName());
                //Log.d("IDIMAGE", " " + idImage);

            } else if (availableLevels.getInt(button + "", 0) == UNLOCKED) {

                idImage = getResources()
                    .getIdentifier(button + "_polaroid_unlocked", "drawable",
                        this.getPackageName());
                //Log.d("IDIMAGE", " " + idImage);
            } else {
                idImage = getResources()
                    .getIdentifier(button + "_polaroid_locked", "drawable", this.getPackageName());
                //Log.d("IDIMAGE", " " + idImage);
            }
            iB.setImageResource(idImage);
            //findViewById(idButton).setBackgroundResource(idImage);

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void alertDialogue() {
        mp.reset();
        mp = MediaPlayer.create(this, R.raw.question_reset);
        //new Handler().postDelayed(() -> reset.start(), 500);
        mp.setVolume(0.5f, 0.5f);
        mp.start();

       /* MediaPlayer bloop;
        bloop = MediaPlayer.create(this, R.raw.button);*/

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
                if (button != 0) {
                    soundPool.play(button, 1f, 1f, 1, 0, 1f);
                }
                mp.stop();
                resetLevels();
                touchy();
            })
            .setNegativeButton("Nein", (dialog, id) -> {
                // if this button is clicked, just close
                // the dialog_shape box and do nothing
                if (button != 0) {
                    soundPool.play(button, 1f, 1f, 1, 0, 1f);
                }
                mp.stop();
                dialog.cancel();
                touchy();
            }).setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (mp.isPlaying()) {
                    mp.stop();
                }
                touchy();
            }
        });

        // create alert dialog_shape
        AlertDialog alertDialog = alertDialogBuilder.create();

        /*alertDialog.setOnCancelListener(
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (reset.isPlaying()) {
                        reset.stop();
                        touchy();
                    }
                }
            }
        );*/
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

        Drawable yes = getResources().getDrawable(R.drawable.button_yes, getTheme());
        Drawable no = getResources().getDrawable(R.drawable.button_exit, getTheme());

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
        noTouchy();
        if (button != 0) {
            soundPool.play(button, 1f, 1f, 1, 0, 1f);
        }
        /*MediaPlayer bloop = new MediaPlayer();
        bloop = MediaPlayer.create(this, R.raw.button);
        bloop.start();*/
        //view.setEnabled(false);
        switch (view.getId()) {
            case R.id.button_exit:
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
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
                return;
            case R.id.button_reset:
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
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
                return;

            case R.id.button_unlock:
                unlockLevels();
                touchy();
                return;
            case R.id.button_tutorial:
                Intent intentTut = new Intent(this, TutorialActivity.class);
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startActivity(intentTut);
                        //touchy();
                    }
                });
                view.startAnimation(scale);
                return;

            case R.id.button_museum:
                Intent intentDino = new Intent(this, DinosActivity.class);
                scale.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startActivity(intentDino);
                        //touchy();
                    }
                });
                view.startAnimation(scale);
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

        //Log.d("LEVEL ", level.toString());
        /*extras.putChar("LEVEL", level);
        extras.putSerializable("animalPool", animalPool);
        intent.putExtras(extras);*/

        // Only playable levels
        if (availableLevels.getInt(level.toString(), 0) != LOCKED) {
            ImageButton iB = findViewById(view.getId());
            iB.startAnimation(scale);
            Intent intent = new Intent(this, GameActivity.class);

            intent.putExtra("LEVEL", level);

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


        } else {
            touchy();
        }
        //intent.putExtra("animalPool", animalPool);*/

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