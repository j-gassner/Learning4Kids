package de.tum.in.l4k.weltreise;

import android.content.Context;
import java.util.HashMap;

class LevelCollection {

    private HashMap<Character, Level> levels = new HashMap<>();

    /**
     * Constructor initializing the different levels.
     *
     * @param context Context.
     */
    LevelCollection(Context context) {
        levels.put('f', new Level('f', 50, true, 5, context));
        levels.put('l', new Level('l', 50, false, 5, context));
        levels.put('r', new Level('r', 45, false, 5, context));
        levels.put('m', new Level('m', 45, false, 5, context));
        levels.put('n', new Level('n', 40, true, 6, context));
        levels.put('i', new Level('i', 40, false, 6, context));
        levels.put('e', new Level('e', 35, false, 6, context));
        levels.put('a', new Level('a', 35, false, 6, context));
        levels.put('o', new Level('o', 30, false, 7, context));
        levels.put('s', new Level('s', 30, true, 7, context));
        levels.put('b', new Level('b', 25, false, 7, context));
        levels.put('t', new Level('t', 25, true, 7, context));
    }

    /**
     * @param level Letter of level requested.
     * @return Level starting with specified character.
     */
    Level getLevel(Character level) {
        return levels.get(level);
    }
}
