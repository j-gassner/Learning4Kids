package de.tum.in.l4k.weltreise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

/**
 * Class for the first screen the user sees containing the L4K logo. Starts tutorial or main menu
 * depending on level states.
 */
public class MainActivity extends WindowManagementActivity {

    /**
     * {@inheritDoc} Shows L4K logo and sets the animal pool. Starts tutorial or StartActivity
     * depending on if the tutorial or first level are completed already.
     *
     * @param savedInstanceState Instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();

        // Set animalPool
        AnimalPool animalPool = (AnimalPool) getApplication();
        animalPool.setAnimalPool();

        SharedPreferences availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        Intent intent;

        // Start tutorial if it or the first level are not completed yet.
        if (!availableLevels.getBoolean("Tutorial", false)
            && availableLevels.getInt("f", 1) != LevelState.COMPLETED.ordinal()) {
            intent = new Intent(this, TutorialActivity.class);
        } else {
            intent = new Intent(this, StartActivity.class);
        }
        Handler handler = new Handler();
        handler.postDelayed(() -> startActivity(intent), 1000);
    }
}
