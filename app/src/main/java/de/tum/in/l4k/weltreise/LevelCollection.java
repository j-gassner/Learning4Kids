package de.tum.in.l4k.weltreise;

import android.content.Context;
import java.util.HashMap;

class LevelCollection {
    private HashMap<Character, Level> levels;

    LevelCollection(Context context){
        levels = new HashMap<Character, Level>(){{
            put('f', new Level('f', 50, true, 5, context));
            put('l', new Level('l', 50, false, 5, context));
            put('r', new Level('r', 45, false, 5, context));
            put('m', new Level('m', 45, false, 5, context));
            put('n', new Level('n', 40, true, 6, context));
            put('i', new Level('i', 40, false, 6, context));
            put('e', new Level('e', 35, false, 6, context));
            put('a', new Level('a', 35, false, 6, context));
            put('o', new Level('o', 30, false, 7, context));
            put('s', new Level('s', 30, true, 7, context));
            put('b', new Level('b', 25, false, 7, context));
            put('t', new Level('t', 25, true, 7, context));
        }};
    }

    Level getLevel(Character level){
        return levels.get(level);
    }
}
