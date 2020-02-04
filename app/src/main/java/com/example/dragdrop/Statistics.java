package com.example.dragdrop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Statistics {
    // tp, tn, fp, fn
    private HashMap<Integer, String> animalIDs;
    private HashMap<String, ArrayList<Integer>> animalStatistics;
    private HashMap<String, ArrayList<Integer>> levelStatistics;

    Statistics(){
        this.animalIDs = new HashMap<>();
        this.animalStatistics = new HashMap<>();
        this.levelStatistics = new HashMap<>();
    }
    void addToIDList(int id, String animal){
        animalIDs.put(id, animal);
        // tp, tn, fp, fn
        animalStatistics.put(animal, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
        levelStatistics.put(animal, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
    }

    void addToStatistics(int id, int index){
        // animalList
        String animal = animalIDs.get(id);
        int number = animalStatistics.get(animal).get(index);
        animalStatistics.get(animal).set(index, number + 1);

        // levelList
        number = levelStatistics.get(animal).get(index);
        levelStatistics.get(animal).set(index, number + 1);
    }

    void writeToCSV() {

    }

}
