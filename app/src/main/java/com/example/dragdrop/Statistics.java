package com.example.dragdrop;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Statistics {
    // tp, tn, fp, fn
    private HashMap<Integer, String> animalIDs;
    private HashMap<String, ArrayList<Integer>> animalStatistics;

    Statistics(){
        this.animalIDs = new HashMap<>();
        this.animalStatistics = new HashMap<>();
    }
    void addToIDList(int id, String animal){
        animalIDs.put(id, animal);
        animalStatistics.put(animal, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
    }

    void addToStatistics(int id, int index){
        String animal = animalIDs.get(id);
        int number = animalStatistics.get(animal).get(index);
        animalStatistics.get(animal).set(index, number + 1);
    }

}
