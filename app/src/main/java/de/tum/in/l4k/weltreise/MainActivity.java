package de.tum.in.l4k.weltreise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends WindowManagement {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();

        AnimalPool animalPool = (AnimalPool) getApplication();
        animalPool.setAnimalPool();

        SharedPreferences availableLevels = getSharedPreferences("availableLevels", MODE_PRIVATE);
        Intent intent;

        // Start tutorial on first start
        if (!availableLevels.getBoolean("Tutorial", false) && availableLevels.getInt("f", 1) != 2) {
            intent = new Intent(this, TutorialActivity.class);
        } else {
            intent = new Intent(this, StartActivity.class);
        }
        Handler handler = new Handler();
        handler.postDelayed(() -> startActivity(intent), 1000);

    }

}
