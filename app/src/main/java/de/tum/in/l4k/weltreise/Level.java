package de.tum.in.l4k.weltreise;

import android.content.Context;

class Level {
    private int backgroundID;
    private int difficulty;
    private boolean isLeft;
    private int winningNumber;

    Level(Character level, int difficulty, boolean isLeft, int winningNumber, Context context) {
        this.difficulty = difficulty;
        this.backgroundID = context.getResources()
            .getIdentifier("background_" + level, "drawable", context.getPackageName());
        this.isLeft = isLeft;
        this.winningNumber = winningNumber;
    }

    int getBackgroundID() {
        return this.backgroundID;
    }

    int getDifficulty() {
        return this.difficulty;
    }

    boolean getIsLeft() {
        return this.isLeft;
    }

    int getWinningNumber() {
        return this.winningNumber;
    }
}
