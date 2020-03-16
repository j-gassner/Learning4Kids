package de.tum.in.l4k.weltreise;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Class hiding the system's UI and enabling immersive sticky mode.
 *
 * @author Josefine Gaßner
 */

public abstract class WindowManagementActivity extends Activity {

    /**
     * {@inheritDoc} Immediately hides system UI to avoid distraction.
     *
     * @param savedInstanceState Instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
    }

    /**
     * {@inheritDoc} Makes sure the system UI is hidden again after focus change.
     *
     * @param hasFocus If window has focus.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    /**
     * Enables immersive sticky mode and hides system UI
     */
    protected void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
