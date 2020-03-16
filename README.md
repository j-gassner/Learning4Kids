# Tierische Weltreise

This is a Java literacy app for the project Learning4Kids: http://www.psy.lmu.de/ffp/forschung/ag-niklas/learning-4kids/index.html

It is developed to run on the Samsung Galaxy Tab A SM-T510.

## Requirements
The project was built in Android Studio 3.6.1 using Gradle version 5.6.4.

The minimum required Android API version is 24.

## Documentation
You can build a Doxygen documentation by running the following command in the root directory:

```sh
doxygen Doxyfile
```

The documentation then can be found in the directory `doc_doxygen`.

## How it works

### Main Screen
Upon starting the app the L4K logo is shown. Then, depending on the player's previous progress, the tutorial or main menu is presented.

### Main Menu
The main menu contains all twelve levels, which can be locked, unlocked, or completed, depending on the progress. It also contains a button for resetting all levels, starting the tutorial, accessing the museum, and exiting the app.

### Museum
The museum contains twelve dinosaurs, one for each level. As soon as a level is completed the corresponding dinosaur is unlocked.

### Levels
There are twelve levels, each with a different letter. Each level requires the player to correctly identify the first letter of animals by dragging them to the correct place. A level is won when sufficient animals that start with the level's letter have been drags correctly. If a level is completed for the first time a new dinosaur is presented and added to the museum.

### Tutorial
The tutorial explains the game mechanics more thoroughly. It is started automatically if it or the first level have not been completed, yet.

## Extending the App
If you want to add a new animal to the resources the image in `drawable` must be names `${animalName}_animal` and the corresponding sound file `${animalName}_sound` in `raw`. The new animal will then be processed automatically.



