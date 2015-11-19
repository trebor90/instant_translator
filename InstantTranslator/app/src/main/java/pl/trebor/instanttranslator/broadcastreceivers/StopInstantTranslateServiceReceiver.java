package pl.trebor.instanttranslator.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pl.trebor.instanttranslator.InstantTranslateService;
import pl.trebor.instanttranslator.R;
import pl.trebor.instanttranslator.StartActivity;
import pl.trebor.instanttranslator.utils.NotificationUtil;

public class StopInstantTranslateServiceReceiver extends BroadcastReceiver {
    public StopInstantTranslateServiceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.stopService(new Intent(context, InstantTranslateService.class));
        NotificationUtil.cancelInstantTranslateNotification(context, StartActivity.ID_INSTANT_TRANSLATE_NOTIFICATION, context.getString(R.string.instant_translator_service_stop));
    }
}
