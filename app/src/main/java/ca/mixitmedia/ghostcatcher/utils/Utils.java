package ca.mixitmedia.ghostcatcher.utils;

/**
 * Created by Dante on 07/03/14.
 */
class Utils {

    public static String removeExtension(String name) {
        final int lastPeriodPos = name.lastIndexOf('.');

        if (lastPeriodPos <= 0) {
            // No period after first character - return name as it was passed in
            return name;
        } else {
            // Remove the last period and everything after it
            return name.substring(0, lastPeriodPos);
        }
    }
}
