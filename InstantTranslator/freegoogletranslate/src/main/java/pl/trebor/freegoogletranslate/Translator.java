package pl.trebor.freegoogletranslate;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by trebor on 4/12/2015.
 */
public class Translator {

    public static final String TAG = Translator.class.getSimpleName();

    private static Translator translator;

    private Translator() {
    }

    public static synchronized Translator getInstance() {
        if (translator == null) {
            translator = new Translator();
        }
        return translator;
    }

    public TranslateResult translate(String text, String languageInput, String languageOutput) throws IOException {
        TextParser textParser = new TextParser(text, languageInput, languageOutput);
        return textParser.parse();
    }

    private static class TextParser {
        String text;
        String languageInput;
        String languageOutput;
        StringBuilder url;

        public TextParser(String text, String languageInput, String languageOutput) {
            this.text = text;
            this.languageInput = languageInput;
            this.languageOutput = languageOutput;
        }

        public TranslateResult parse() throws IOException {
            appendURL();
            Log.d(TAG, "Translate url: " + url.toString());
            String result = makeRequest(url.toString());

            return parseJSON(result);
        }

        public TranslateResult parseJSON(String json) {
            String textToTranslate = null;
            String translation = null;
            HashMap<String, ArrayList<String>> similarWordsMap = new HashMap<>();

            try {
                JSONArray jsonArray = new JSONArray(json);
                JSONArray mainTranslationArray = jsonArray.getJSONArray(0);
                JSONArray translateArray = mainTranslationArray.getJSONArray(0);
                translation = translateArray.getString(0);
                textToTranslate = translateArray.getString(1);
                Log.d(TAG, "en: " + textToTranslate + " ,pl: " + translation);

//                parse nouns
//                if null, that means is sentence, skip this code
                JSONArray preNounsArray = jsonArray.getJSONArray(1);
                if (preNounsArray != null) {
                    JSONArray nounsArray = preNounsArray.getJSONArray(0);
                    JSONArray wordsArray = nounsArray.getJSONArray(2);

                    for (int i = 0; i < wordsArray.length(); i++) {
                        JSONArray wordArray = wordsArray.getJSONArray(i);
                        String w = wordArray.getString(0);
                        Log.d(TAG, "pl similar: " + w);
                        ArrayList<String> similarWords = new ArrayList<>();
                        JSONArray similarWordsArray = wordArray.getJSONArray(1);
                        for (int j = 0; j < similarWordsArray.length(); j++) {
                            String similarWord = similarWordsArray.getString(j);
                            similarWords.add(similarWord);
                            Log.d(TAG, "en s" + Integer.toString(j + 1) + " :" + similarWord);
                        }
                        similarWordsMap.put(w, similarWords);
                    }
                }

            } catch (JSONException e) {
                Log.d(TAG, "Lack of similar words for this sentence!", e);
            }
            return new TranslateResult(textToTranslate, translation, this.languageInput, this.languageOutput, similarWordsMap);
        }

        private String makeRequest(String urlSite) throws IOException {
            StringBuilder result = new StringBuilder();

            URL url = new URL(urlSite);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);

            InputStreamReader e = new InputStreamReader(urlConnection.getInputStream(), "utf-8");
            BufferedReader br = new BufferedReader(e);

            int byteRead;
            while ((byteRead = br.read()) != -1) {
                result.append((char) byteRead);
            }
            return result.toString();
        }

        private void appendURL() {
            url = new StringBuilder("http://translate.google.pl/translate_a/single?dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=at&ie=UTF-8&oe=UTF-8&otf=2&ssel=0&tsel=0&tk=519261|988582&client=t");
            url.append("&q=").append(text.replace(" ", "%20"));
            url.append("&hl=").append(languageInput);
            url.append("&sl=").append(languageInput);
            url.append("&tl=").append(languageOutput);
        }
    }
}
