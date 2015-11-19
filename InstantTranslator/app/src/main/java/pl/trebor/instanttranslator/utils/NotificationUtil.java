package pl.trebor.instanttranslator.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by trebor on 11/2/2014.
 */
public class NotificationUtil {
    /**
     * cancel notification with specified id and show Toast with information
     */
    public static void cancelInstantTranslateNotification(Context context, int notificationId, CharSequence text) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationId);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
