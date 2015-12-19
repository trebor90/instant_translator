package pl.trebor.freegoogletranslate.model;

import java.util.ArrayList;
import java.util.List;

public class Dict {

    private String pos;
    private List<String> terms = new ArrayList<String>();
    private List<Entry> entry = new ArrayList<Entry>();
    private String baseForm;
    private Integer posEnum;

    /**
     * @return The pos
     */
    public String getPos() {
        return pos;
    }

    /**
     * @param pos The pos
     */
    public void setPos(String pos) {
        this.pos = pos;
    }

    /**
     * @return The terms
     */
    public List<String> getTerms() {
        return terms;
    }

    /**
     * @param terms The terms
     */
    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    /**
     * @return The entry
     */
    public List<Entry> getEntry() {
        return entry;
    }

    /**
     * @param entry The entry
     */
    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }

    /**
     * @return The baseForm
     */
    public String getBaseForm() {
        return baseForm;
    }

    /**
     * @param baseForm The base_form
     */
    public void setBaseForm(String baseForm) {
        this.baseForm = baseForm;
    }

    /**
     * @return The posEnum
     */
    public Integer getPosEnum() {
        return posEnum;
    }

    /**
     * @param posEnum The pos_enum
     */
    public void setPosEnum(Integer posEnum) {
        this.posEnum = posEnum;
    }

}
