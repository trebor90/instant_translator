package pl.trebor.instanttranslator;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import pl.trebor.freegoogletranslate.Language;
import pl.trebor.instanttranslator.utils.LanguageUtils;
import pl.trebor.instanttranslator.utils.NotificationUtil;


public class StartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, RetainFragment.InternetConnectionCallback {

    public static final String TAG = StartActivity.class.getSimpleName();

    public static final int ID_INSTANT_TRANSLATE_NOTIFICATION = 99;
    public static final String INSTANT_TRANSLATOR_CLOSE_SERVICE_ACTION = "pl.trebor.instanttranslator.closeservice";
    public static final String LANGUAGE_FROM = "language_from";
    public static final String LANGUAGE_TO = "language_to";
    public static final String RETAIN_FRAGMENT = "retain_fragment";

    private Button startServiceBtn;
    private CloseTranslationServiceReceiver receiver;
    private SharedPreferences sharedPref;
    private RetainFragment retainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        retainFragment = (RetainFragment) supportFragmentManager.findFragmentByTag(RETAIN_FRAGMENT);
        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            supportFragmentManager.beginTransaction().add(retainFragment, RETAIN_FRAGMENT).commit();
        }

        sharedPref = getSharedPreferences(getString(R.string.option_file_name), Context.MODE_PRIVATE);

        startServiceBtn = (Button) findViewById(R.id.start_service_btn);
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMyServiceRunning(InstantTranslateService.class)) {
                    stopInstantTranslatorService();
                    startServiceBtn.setText(getString(R.string.start_translate_service));
                } else {
                    retainFragment.checkInternetConnection();
                }
            }
        });

        initSpinners();
    }

    private void initSpinners() {
        Spinner languageFromSpinner = (Spinner) findViewById(R.id.language_from_spin);
        languageFromSpinner.setOnItemSelectedListener(this);
        Spinner languageToSpinner = (Spinner) findViewById(R.id.language_to_spin);
        languageToSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, LanguageUtils.getNameList(this));
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageFromSpinner.setAdapter(languageAdapter);
        languageToSpinner.setAdapter(languageAdapter);

        languageFromSpinner.setSelection(languageAdapter.getPosition(LanguageUtils.getNameByPrefix(sharedPref.getString(LANGUAGE_FROM, Language.ENGLISH), this)));
        languageToSpinner.setSelection(languageAdapter.getPosition(LanguageUtils.getNameByPrefix(sharedPref.getString(LANGUAGE_TO, Language.POLISH), this)));
    }

    @Override
    public void onPreExecute() {
        startServiceBtn.setEnabled(Boolean.FALSE);
        startServiceBtn.setText(getString(R.string.starting_service));
    }

    @Override
    public void onPostExecute(boolean isOnline) {
        startServiceBtn.setEnabled(Boolean.TRUE);
        String buttonText = getString(R.string.disable_translate_service);
        if (isOnline) {
            startInstantTranslatorService();
        } else {
            Toast.makeText(StartActivity.this, getString(R.string.no_internet_acces), Toast.LENGTH_LONG).show();
            buttonText = getString(R.string.start_translate_service);
        }

        startServiceBtn.setText(buttonText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter closeTranslateServiceFilter = new IntentFilter(INSTANT_TRANSLATOR_CLOSE_SERVICE_ACTION);
        receiver = new CloseTranslationServiceReceiver();
        registerReceiver(receiver, closeTranslateServiceFilter);
        setButtonText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        receiver = null;
    }

    private void setButtonText() {
        if (isMyServiceRunning(InstantTranslateService.class)) {
            startServiceBtn.setText(getString(R.string.disable_translate_service));
        } else {
            startServiceBtn.setText(getString(R.string.start_translate_service));
        }
    }

    private void startInstantTranslatorService() {
        startService(new Intent(this, InstantTranslateService.class));
        startOngoingNotification();
        Toast.makeText(this, getString(R.string.instant_translator_service_start), Toast.LENGTH_LONG).show();
    }

    private void stopInstantTranslatorService() {
        stopService(new Intent(this, InstantTranslateService.class));
        NotificationUtil.cancelInstantTranslateNotification(this, ID_INSTANT_TRANSLATE_NOTIFICATION, getString(R.string.instant_translator_service_stop));
    }

    private void startOngoingNotification() {
        Intent intent = new Intent();
        intent.setAction(INSTANT_TRANSLATOR_CLOSE_SERVICE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 12345, intent, 0);
        Intent activityIntent = new Intent(this, StartActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 123456, activityIntent, 0);
        NotificationCompat.Builder instantTranslateNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.instant_translator))
                .setContentText(getString(R.string.instant_translator_service_is_active))
                .setOngoing(true)
                .addAction(new NotificationCompat.Action(R.drawable.ic_launcher, getString(R.string.stop), pendingIntent))
                .setContentIntent(activityPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(ID_INSTANT_TRANSLATE_NOTIFICATION, instantTranslateNotificationBuilder.build());
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String prefSource = null;
        switch (parent.getId()) {
            case R.id.language_from_spin:
                prefSource = LANGUAGE_FROM;
                break;
            case R.id.language_to_spin:
                prefSource = LANGUAGE_TO;
                break;
            default:
                break;
        }
        sharedPref.edit().putString(prefSource, LanguageUtils.getPrefixByName((String) parent.getItemAtPosition(position), this)).apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private class CloseTranslationServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            startServiceBtn.setText(getString(R.string.start_translate_service));
        }
    }


}
