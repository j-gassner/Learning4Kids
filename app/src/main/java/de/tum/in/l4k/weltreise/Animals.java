package de.tum.in.l4k.weltreise;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class used for managing the set of animals used in the game.
 *
 * @author Josefine Gaßner
 */

// Faster than Parcelable? https://bitbucket.org/afrishman/androidserializationtest/src/default/
@SuppressWarnings("serial")
class Animals implements Serializable {

    private static Random rand = new Random();
    private HashMap<Character, ArrayList<Integer>> animalMap;
    private HashMap<Character, ArrayList<Integer>> animalMapCurrent;

    /**
     * Constructor initializing HashMaps.
     */
    Animals() {
        this.animalMap = new HashMap<>();
        this.animalMapCurrent = new HashMap<>();
    }

    /**
     * @return animalMap
     */
    HashMap<Character, ArrayList<Integer>> getAnimalMap() {
        return this.animalMap;
    }

    /**
     * @return animalMapCurrent
     */
    HashMap<Character, ArrayList<Integer>> getAnimalMapCurrent() {
        return this.animalMapCurrent;
    }

    /**
     * Deep copies animalMap to animalMapCurrent so that it contains all animals again.
     */
    void reset() {
        this.animalMapCurrent = copy(this.animalMap);
    }

    /**
     * Draws animal starting with currentLetter randomly from animalMapCurrent.
     *
     * @param currentLetter Letter of level.
     * @return Random animal starting with currentLetter.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    Integer getAnimal(char currentLetter) {
        cleanUp();
        ArrayList<Integer> animals = animalMapCurrent.get(currentLetter);

        //assert animals != null;
        // List for current level is empty
        if (animals == null) {
            reset();
            return getAnimal(currentLetter);
        }

        return animals.get(rand.nextInt(animals.size()));
    }

    /**
     * Finds a random letter that is not currentLetter and draws animal starting with it.
     *
     * @param currentLetter Letter of level.
     * @return Random animal not starting with currentLetter.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    Integer getDistractorAnimal(char currentLetter) {
        cleanUp();
        Set<Character> keySet = new TreeSet<>(animalMapCurrent.keySet());

        // No distractor animals left
        if (keySet.size() == 0) {
            reset();
            return getDistractorAnimal(currentLetter);
        }

        keySet.remove(currentLetter);
        ArrayList<Character> letters = new ArrayList<>(keySet);
        Character distractor = letters.get(rand.nextInt(letters.size()));

        return getAnimal(distractor);
    }


    /**
     * Removes empty entries from animalMapCurrent.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cleanUp() {
        animalMapCurrent.entrySet().removeIf(level -> level.getValue().size() == 0);
    }

    /**
     * Creates a deepcopy of passed HashMap.
     *
     * @param original HashMap to be copied.
     * @return Deepcopy of original.
     */
    private static HashMap<Character, ArrayList<Integer>> copy(
        HashMap<Character, ArrayList<Integer>> original) {
        HashMap<Character, ArrayList<Integer>> copy = new HashMap<>();
        for (Map.Entry<Character, ArrayList<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}

