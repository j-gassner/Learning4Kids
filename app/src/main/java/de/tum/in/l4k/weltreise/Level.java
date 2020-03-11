package de.tum.in.l4k.weltreise;

import android.content.Context;

/**
 * Class representing a level of the GameActivity.
 *
 * @author Josefine Gaßner
 */

class Level {
    private int backgroundID;
    private int difficulty;
    private boolean isLeft;
    private int winningNumber;

    /**
     * Constructor setting Level attributes.
     *
     * @param level Level.
     * @param difficulty Difficulty.
     * @param isLeft Whether match is left or right.
     * @param winningNumber Number of relevant matches needed to win.
     * @param context Context.
     */
    Level(Character level, int difficulty, boolean isLeft, int winningNumber, Context context) {
        this.difficulty = difficulty;
        this.backgroundID = context.getResources()
            .getIdentifier("background_" + level, "drawable", context.getPackageName());
        this.isLeft = isLeft;
        this.winningNumber = winningNumber;
    }

    /**
     * @return Id of level background.
     */
    int getBackgroundID() {
        return this.backgroundID;
    }

    /**
     * @return Difficulty of level.
     */
    int getDifficulty() {
        return this.difficulty;
    }

    /**
     * @return Position of match and noMatch.
     */
    boolean getIsLeft() {
        return this.isLeft;
    }

    /**
     * @return Number of relevant matches needed to win level.
     */
    int getWinningNumber() {
        return this.winningNumber;
    }
}
