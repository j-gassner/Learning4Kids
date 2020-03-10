package de.tum.in.l4k.weltreise;

import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.animation.Animation;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import androidx.annotation.RequiresApi;
import java.util.HashMap;

/**
 * Base class for Start- and DinosActivity
 *
 * @author Josefine Gaßner
 */
public abstract class ScrollActivity extends BaseActivity {

    // Remember scroll position
    protected static HashMap<Class, Integer> scrollXMap = new HashMap<Class, Integer>() {{
        put(StartActivity.class, 0);
        put(DinosActivity.class, 0);
    }};

    HorizontalScrollView hsv;
    Animation locked;
    int button;

    /**
     * Snaps scrollview.
     *
     * @param left Whether left button has been pressed.
     */
    abstract void snap(boolean left);

    /**
     * Find elements of scrollview.
     */
    abstract void assignScrollElements();

    @Override
    protected void onStart() {
        super.onStart();
        scrollPosition();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scrollXMap.put(this.getClass(), hsv.getScrollX());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Integer scrollX = scrollXMap.get(this.getClass());
        if (scrollX != null) {
            hsv.post(() -> hsv.scrollTo(scrollX, 0));
        }
    }

    /**
     * Make left and right button (in-)visible depending on scroll position.
     */
    void scrollPosition() {
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
    }

    /**
     * Load sound used by all extending activities to soundPool.
     */
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    void loadButtonSounds() {
        super.loadButtonSounds();
        button = soundPool.load(this, R.raw.button, 1);

    }
}
