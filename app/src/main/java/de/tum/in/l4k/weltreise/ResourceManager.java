package de.tum.in.l4k.weltreise;

import android.content.Context;

/**
 * Wrapper class for resource loading.
 *
 * @author Josefine Gaßner
 */
class ResourceManager {

    /**
     * Function for finding an ID.
     *
     * @param context Context.
     * @param resource Resource the ID should be found of.
     * @return Resource ID.
     */
    private static int getId(Context context, String resource) {
        return context.getResources().getIdentifier(resource, "id", context.getPackageName());
    }

    /**
     * Function for finding a raw ID.
     *
     * @param context Context.
     * @param resource Resource the ID should be found of.
     * @return Resource ID.
     */
    private static int getRawId(Context context, String resource) {
        return context.getResources().getIdentifier(resource, "raw", context.getPackageName());
    }

    /**
     * Function for finding a drawable ID.
     *
     * @param context Context.
     * @param resource Resource the ID should be found of.
     * @return Resource ID.
     */
    private static int getDrawableId(Context context, String resource) {
        return context.getResources().getIdentifier(resource, "drawable", context.getPackageName());
    }

    /**
     * Finds the ID of a dino.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of dino.
     */
    static int getIdDino(Context context, Character level) {
        return getId(context, "dino_" + level);
    }

    /**
     * Finds the ID of a button.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of button.
     */
    static int getIdButton(Context context, Character level) {
        return getId(context, "button_" + level);
    }

    /**
     * Finds the raw ID of an instruction.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of instruction.
     */
    static int getRawIdInstruction(Context context, Character level) {
        return getRawId(context, "instruction_" + level);
    }

    /**
     * Finds the raw ID of letter sound of a level.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of level letter sound.
     */
    static int getRawIdLevel(Context context, Character level) {
        return getRawId(context, level + "_sound");
    }

    /**
     * Finds the raw ID of an animal sound.
     *
     * @param context Context.
     * @param name Name of animal.
     * @return Resource ID of animal sound.
     */
    static int getRawIdAnimal(Context context, String name) {
        return getRawId(context, name + "sound");
    }

    /**
     * Finds the raw ID of a dino sound.
     *
     * @param context Context.
     * @param dino Name of dino.
     * @return Resource ID of dino sound.
     */
    static int getRawIdDino(Context context, String dino) {
        return getRawId(context, dino + "_sound");
    }

    /**
     * Finds the raw ID of indication that a new dino has been unlocked.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of indication.
     */
    static int getRawIdMuseum(Context context, Character level) {
        return getRawId(context, "museum_" + level);
    }

    /**
     * Finds the raw ID of random praise.
     *
     * @param context Context.
     * @param number Number of praise.
     * @return Resource ID of random praise sound.
     */
    static int getRawIdPraise(Context context, int number) {
        return getRawId(context, "praise" + number);
    }

    /**
     * Finds the raw ID of random encouragement.
     *
     * @param context Context.
     * @param number Number of encouragement.
     * @return Resource ID of random encouragement sound.
     */
    static int getRawIdEncourage(Context context, int number) {
        return getRawId(context, "encourage" + number);
    }

    /**
     * Finds the drawable ID of a resource.
     *
     * @param context Context.
     * @param resource Resource name.
     * @return Resource ID of drawable.
     */

    static int getDrawableIdResource(Context context, String resource) {
        return getDrawableId(context, resource);
    }

    /**
     * Finds the drawable ID of a dino.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of dino drawable.
     */
    static int getDrawableIdDino(Context context, Character level) {
        return getDrawableId(context, "dino_" + level);
    }

    /**
     * Finds the drawable ID of a letter filling.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of letter filling.
     */
    static int getDrawableIdLetterFill(Context context, Character level) {
        return getDrawableId(context, level + "_letter_fill");
    }

    /**
     * Finds the drawable ID of a letter outline.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of letter outline.
     */
    static int getDrawableIdLetterOutline(Context context, Character level) {
        return getDrawableId(context, level + "_letter_outline");
    }

    /**
     * Finds the drawable ID of a level background.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of level background.
     */
    static int getDrawableIdBackground(Context context, Character level) {
        return getDrawableId(context, "background_" + level);
    }

    /**
     * Finds the drawable ID of a polaroid.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of polaroid.
     */
    static int getDrawableIdPolaroid(Context context, Character level) {
        return getDrawableId(context, level + "_polaroid");
    }

    /**
     * Finds the drawable ID of an unlocked polaroid.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of unlocked polaroid.
     */
    static int getDrawableIdPolaroidUnlocked(Context context, Character level) {
        return getDrawableId(context, level + "_polaroid_unlocked");
    }

    /**
     * Finds the drawable ID of a locked polaroid.
     *
     * @param context Context.
     * @param level Level.
     * @return Resource ID of locked polaroid.
     */
    static int getDrawableIdPolaroidLocked(Context context, Character level) {
        return getDrawableId(context, level + "_polaroid_locked");
    }

}
