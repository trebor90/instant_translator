package pl.trebor.instanttranslator.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import pl.trebor.freegoogletranslate.Language;
import pl.trebor.instanttranslator.R;

/**
 * Created by trebor on 1/5/2015.
 */
public final class LanguageUtils {
    //    key = prefix, value = name
    private static HashMap<String, String> languagePrefixMap;
    private static String NO_LANGUAGE = "-";

    public static String getNameByPrefix(String prefix, Context context) {
        if (languagePrefixMap == null) {
            initLanguagePrefixMap(context);
        }
        return languagePrefixMap.get(prefix);
    }

    public static String getPrefixByName(String name, Context context) {
        if (languagePrefixMap == null) {
            initLanguagePrefixMap(context);
        }
        for (Map.Entry<String, String> entry : languagePrefixMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return NO_LANGUAGE;
    }

    public static String[] getNameList(Context context) {
        if (languagePrefixMap == null) {
            initLanguagePrefixMap(context);
        }
        String[] names = new String[languagePrefixMap.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : languagePrefixMap.entrySet()) {
            names[index++] = entry.getValue();
        }
        return names;
    }

    public static String[] getPrefixList(Context context) {
        if (languagePrefixMap == null) {
            initLanguagePrefixMap(context);
        }
        String[] prefixes = new String[languagePrefixMap.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : languagePrefixMap.entrySet()) {
            prefixes[index++] = entry.getKey();
        }
        return prefixes;
    }

    private static void initLanguagePrefixMap(Context context) {
        languagePrefixMap = new HashMap<>();
        languagePrefixMap.put(Language.POLISH, context.getString(R.string.polish));
        languagePrefixMap.put(Language.ENGLISH, context.getString(R.string.english));
        languagePrefixMap.put(Language.GERMAN, context.getString(R.string.german));
    }

}
