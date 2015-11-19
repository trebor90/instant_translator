package pl.trebor.freegoogletranslate;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by trebor on 6/22/2015.
 */
public class TranslateResult {
    private String textToTranslate;
    private String translation;
    private String translateFrom;
    private String translateTo;
    private HashMap<String, ArrayList<String>> similarTranslation;

    public TranslateResult(String textToTranslate, String translation, String translateFrom, String translateTo, HashMap<String, ArrayList<String>> similarTranslation) {
        this.textToTranslate = textToTranslate;
        this.translation = translation;
        this.translateFrom = translateFrom;
        this.translateTo = translateTo;
        this.similarTranslation = similarTranslation;
    }

    public String getTextToTranslate() {
        return textToTranslate;
    }

    public void setTextToTranslate(String textToTranslate) {
        this.textToTranslate = textToTranslate;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getTranslateFrom() {
        return translateFrom;
    }

    public void setTranslateFrom(String translateFrom) {
        this.translateFrom = translateFrom;
    }

    public String getTranslateTo() {
        return translateTo;
    }

    public void setTranslateTo(String translateTo) {
        this.translateTo = translateTo;
    }

    public HashMap<String, ArrayList<String>> getSimilarTranslation() {
        return similarTranslation;
    }

    public void setSimilarTranslation(HashMap<String, ArrayList<String>> similarTranslation) {
        this.similarTranslation = similarTranslation;
    }

    public boolean isSimilarTranslationExist() {
        return this.similarTranslation != null && !this.similarTranslation.isEmpty();
    }
}
