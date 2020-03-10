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


// Faster than Parcelable? https://bitbucket.org/afrishman/androidserializationtest/src/default/
@SuppressWarnings("serial")
class Animals implements Serializable {
    private static Random rand = new Random();

    // HashMap Character -> ArrayList of animals
    private HashMap<Character, ArrayList<Integer>> animalMap;
    private HashMap<Character, ArrayList<Integer>> animalMapCurrent;

    Animals() {
        this.animalMap = new HashMap<>();
        this.animalMapCurrent = new HashMap<>();
    }

    HashMap<Character, ArrayList<Integer>> getAnimalMap() {
        return this.animalMap;
    }

    HashMap<Character, ArrayList<Integer>> getAnimalMapCurrent() {
        return this.animalMapCurrent;
    }

    void reset() {
        this.animalMapCurrent = copy(this.animalMap);
    }

    // Get random animal for current letter
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

    // Get random distractor
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cleanUp() {
        animalMapCurrent.entrySet().removeIf(level -> level.getValue().size() == 0);
    }

    // Deepcopy of HashMap
    private static HashMap<Character, ArrayList<Integer>> copy(
        HashMap<Character, ArrayList<Integer>> original) {
        HashMap<Character, ArrayList<Integer>> copy = new HashMap<>();
        for (Map.Entry<Character, ArrayList<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}

