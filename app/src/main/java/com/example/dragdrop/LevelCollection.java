package com.example.dragdrop;

import android.content.Context;
import java.util.HashMap;

class LevelCollection {
    private HashMap<Character, Level> levels;

    LevelCollection(Context context){
        levels = new HashMap<Character, Level>(){{
            put('f', new Level('f', 90, true, context));
            put('l', new Level('l', 85, false, context));
            put('r', new Level('r', 80, false, context));
            put('m', new Level('m', 75, false, context));
            put('n', new Level('n', 70, true, context));
            put('i', new Level('i', 65, false, context));
            put('e', new Level('e', 60, false, context));
            put('a', new Level('a', 55, false, context));
            put('o', new Level('o', 50, false, context));
            put('s', new Level('s', 45, true, context));
            put('b', new Level('b', 40, false, context));
            put('t', new Level('t', 35, true, context));
        }};
    }

    Level getLevel(Character level){
        return levels.get(level);
    }
}
