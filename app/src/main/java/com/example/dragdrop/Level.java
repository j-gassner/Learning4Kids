package com.example.dragdrop;

import android.content.Context;

class Level {
    private Character level;
    private int backgroundID;
    private int difficulty;
    private boolean isLeft;

    Level(Character level, int difficulty, boolean isLeft,Context context){
        this.level = level;
        this.difficulty = difficulty;
        this.backgroundID = context.getResources().getIdentifier("background_" + this.level, "drawable", context.getPackageName());
        this.isLeft = isLeft;
    }

    int getBackgroundID(){
        return this.backgroundID;
    }

    int getDifficulty(){
        return this.difficulty;
    }

    boolean getIsLeft(){
        return this.isLeft;
    }
}
