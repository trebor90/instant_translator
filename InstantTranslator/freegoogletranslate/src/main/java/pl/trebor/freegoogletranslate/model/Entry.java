package pl.trebor.freegoogletranslate.model;

import java.util.ArrayList;
import java.util.List;

public class Entry {

    private String word;
    private List<String> reverseTranslation = new ArrayList<String>();
    private Double score;

    /**
     * @return The word
     */
    public String getWord() {
        return word;
    }

    /**
     * @param word The word
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * @return The reverseTranslation
     */
    public List<String> getReverseTranslation() {
        return reverseTranslation;
    }

    /**
     * @param reverseTranslation The reverse_translation
     */
    public void setReverseTranslation(List<String> reverseTranslation) {
        this.reverseTranslation = reverseTranslation;
    }

    /**
     * @return The score
     */
    public Double getScore() {
        return score;
    }

    /**
     * @param score The score
     */
    public void setScore(Double score) {
        this.score = score;
    }

}
