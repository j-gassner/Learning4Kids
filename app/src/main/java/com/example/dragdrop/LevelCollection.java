package com.example.dragdrop;

import android.content.Context;
import java.util.HashMap;

class LevelCollection {
    private HashMap<Character, Level> levels;

    LevelCollection(Context context){
        levels = new HashMap<Character, Level>(){{
            put('f', new Level('f', 90, true, 5, context));
            put('l', new Level('l', 85, false, 5, context));
            put('r', new Level('r', 80, false, 5, context));
            put('m', new Level('m', 75, false, 5, context));
            put('n', new Level('n', 70, true, 6, context));
            put('i', new Level('i', 65, false, 6, context));
            put('e', new Level('e', 60, false, 6, context));
            put('a', new Level('a', 55, false, 6, context));
            put('o', new Level('o', 50, false, 7, context));
            put('s', new Level('s', 45, true, 7, context));
            put('b', new Level('b', 40, false, 7, context));
            put('t', new Level('t', 35, true, 7, context));
        }};
    }

    Level getLevel(Character level){
        return levels.get(level);
    }
}
