package pl.trebor.instanttranslator.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import pl.trebor.freegoogletranslate.EmptyResponseException;
import pl.trebor.freegoogletranslate.Language;
import pl.trebor.freegoogletranslate.TranslateResult;
import pl.trebor.freegoogletranslate.Translator;
import pl.trebor.instanttranslator.R;
import pl.trebor.instanttranslator.StartActivity;

/**
 * Created by trebor on 8/10/2014.
 */
public class TranslateTask extends AsyncTask<String, String, AsyncTaskResult<TranslateResult>> {

    private static final String TAG = TranslateTask.class.getSimpleName();
    private TranslateTaskCallback callback;
    private SharedPreferences sharedPreferences;

    public interface TranslateTaskCallback {
        void onPreTranslate();

        void onPostTranslate(AsyncTaskResult<TranslateResult> translatedText);
    }

    public TranslateTask(Context ctx) {
        if (ctx instanceof TranslateTaskCallback) {
            this.callback = (TranslateTaskCallback) ctx;
            sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.option_file_name), Context.MODE_PRIVATE);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected AsyncTaskResult<TranslateResult> doInBackground(String... params) {
        String textToTranslate = params[0];

        TranslateResult translateResult = null;
        AsyncTaskResult<TranslateResult> result = new AsyncTaskResult<>();

        try {
            translateResult = translateText(textToTranslate);
            result.setResult(translateResult);
        } catch (Exception e) {
            result.setException(e);
            Log.d(TAG, "Error while translate sentence! " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onPreTranslate();
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<TranslateResult> s) {
        super.onPostExecute(s);
        callback.onPostTranslate(s);
    }

    private TranslateResult translateText(String textToTranslate) throws IOException, EmptyResponseException {
        Translator translator = Translator.getInstance();
        return translator.translate(textToTranslate, sharedPreferences.getString(StartActivity.LANGUAGE_FROM, Language.ENGLISH), sharedPreferences.getString(StartActivity.LANGUAGE_TO, Language.POLISH));
    }
}
