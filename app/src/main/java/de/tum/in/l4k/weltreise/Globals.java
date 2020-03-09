package de.tum.in.l4k.weltreise;

import android.app.Application;
import android.content.res.Resources;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Globals extends Application {
    private Animals animalPool;
    //private Statistics statistics = new Statistics();
    //static ArrayList<Character> levels = new ArrayList<>(Arrays.asList('f', 'l', 'r', 'm', 'n', 'i', 'e', 'a', 'o', 's', 'b', 't'));

    public Animals getAnimalPool(){
        return this.animalPool;
    }

    /*public Statistics getStatistics(){
        return this.statistics;
    }*/

    public void setAnimalPool(){
        animalPool = new Animals();
        final R.drawable drawableResources = new R.drawable();
        final Class<R.drawable> drawableClass = R.drawable.class;
        final Field[] fields = drawableClass.getDeclaredFields();
        final ArrayList<String> filenames = new ArrayList<>();


        for (Field field : fields) {
            final int resourceId;
            try {
                resourceId = field.getInt(drawableResources);
                String id = getResources().getResourceEntryName(resourceId);

                if (id.contains("animal")) {
                    filenames.add(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Resources resources = getResources();
        for (String filename : filenames) {
            Character firstLetter = filename.charAt(0);
            final int resourceId = resources.getIdentifier(filename, "drawable", getPackageName());
            //statistics.addToIDList(resourceId, filename.replace("_animal", ""));
            if (!animalPool.getAnimalMap().containsKey(firstLetter)) {
                //animalPool.getAnimalMapCurrent().put(firstLetter, new ArrayList<>());
                animalPool.getAnimalMap().put(firstLetter, new ArrayList<>());
            }
            assert animalPool.getAnimalMap().get(firstLetter) != null;
            //animalPool.getAnimalMapCurrent().get(firstLetter).add(resourceId);
            animalPool.getAnimalMap().get(firstLetter).add(resourceId);
        }

        animalPool.reset();
    }
}
