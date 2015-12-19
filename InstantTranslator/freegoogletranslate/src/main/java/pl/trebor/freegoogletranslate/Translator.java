package pl.trebor.freegoogletranslate;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import pl.trebor.freegoogletranslate.model.Sentence;
import pl.trebor.freegoogletranslate.model.TranslateResponse;

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

    public TranslateResult translate(String text, String languageInput, String languageOutput) throws IOException, EmptyResponseException {
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

        public TranslateResult parse() throws IOException, EmptyResponseException {
            appendURL();
            Log.d(TAG, "Translate url: " + url.toString());
            String result = makeRequest(url.toString());
            Log.d(TAG, "Translate response: " + result);
            return parseJSON(result);
        }

        public TranslateResult parseJSON(String json) throws EmptyResponseException {
            HashMap<String, ArrayList<String>> similarWordsMap = new HashMap<>();
            // TODO: 12/19/2015 similar word to implementation

            Gson gson = new Gson();
            TranslateResponse translateResponse = gson.fromJson(json, TranslateResponse.class);
            if (translateResponse.getSentences().isEmpty()) {
                throw new EmptyResponseException("Translation is not exist in response!");
            }
            Sentence sentence = translateResponse.getSentences().get(0);
            return new TranslateResult(sentence.getOrig(), sentence.getTrans(), this.languageInput, this.languageOutput, similarWordsMap);
        }

        private String makeRequest(String urlSite) throws IOException {
            StringBuilder result = new StringBuilder();

            URL url = new URL(urlSite);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            urlConnection.setRequestMethod("POST");
            String queryParams = getPostParams();

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(queryParams);
            writer.flush();
            writer.close();
            os.close();

            InputStreamReader e = new InputStreamReader(urlConnection.getInputStream(), "UTF-8");
            BufferedReader br = new BufferedReader(e);

            int byteRead;
            while ((byteRead = br.read()) != -1) {
                result.append((char) byteRead);
            }
            return result.toString();
        }

        private void appendURL() {
            url = new StringBuilder("http://clients5.google.com/translate_a/t");
        }

        private String getPostParams() throws IOException {
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("client", "dict")
                    .appendQueryParameter("q", text)
                    .appendQueryParameter("sl", languageInput)
                    .appendQueryParameter("tl", languageOutput)
                    .appendQueryParameter("tbb", "1")
                    .appendQueryParameter("ie", "UTF-8")
                    .appendQueryParameter("oe", "UTF-8");

            return builder.build().getEncodedQuery();
        }
    }
}
