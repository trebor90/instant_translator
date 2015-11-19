package pl.trebor.instanttranslator.utils;

import java.io.IOException;

/**
 * Created by trebor on 3/26/2015.
 */
public class NetworkUtils {
    public static boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
