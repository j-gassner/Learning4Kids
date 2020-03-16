package de.tum.in.l4k.weltreise;

import android.app.Application;
import android.content.res.Resources;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Class managing an animalPool that contains all animals used during gameplay.
 *
 * @author Josefine Gaßner
 */

public class AnimalPool extends Application {

    private Animals animalPool;

    public Animals getAnimalPool() {
        return this.animalPool;
    }

    /**
     * Finds all animals in drawable and adds them to the animalPool
     */
    public void setAnimalPool() {
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
            //final int resourceId = resources.getIdentifier(filename, "drawable", getPackageName());
            final int resourceId = ResourceManager.getDrawableIdResource(this, filename);
            if (!animalPool.getAnimalMap().containsKey(firstLetter)) {
                animalPool.getAnimalMap().put(firstLetter, new ArrayList<>());
            }
            Objects.requireNonNull(animalPool.getAnimalMap().get(firstLetter)).add(resourceId);
        }

        animalPool.reset();
    }
}
