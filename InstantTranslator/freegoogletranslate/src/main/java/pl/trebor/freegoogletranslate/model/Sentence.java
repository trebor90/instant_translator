package pl.trebor.freegoogletranslate.model;

/**
 * Created by trebor on 12/19/2015.
 */
public class Sentence {

    private String trans;
    private String orig;
    private String translit;
    private String srcTranslit;
    private Integer backend;

    /**
     * @return The trans
     */
    public String getTrans() {
        return trans;
    }

    /**
     * @param trans The trans
     */
    public void setTrans(String trans) {
        this.trans = trans;
    }

    /**
     * @return The orig
     */
    public String getOrig() {
        return orig;
    }

    /**
     * @param orig The orig
     */
    public void setOrig(String orig) {
        this.orig = orig;
    }

    /**
     * @return The translit
     */
    public String getTranslit() {
        return translit;
    }

    /**
     * @param translit The translit
     */
    public void setTranslit(String translit) {
        this.translit = translit;
    }

    /**
     * @return The srcTranslit
     */
    public String getSrcTranslit() {
        return srcTranslit;
    }

    /**
     * @param srcTranslit The src_translit
     */
    public void setSrcTranslit(String srcTranslit) {
        this.srcTranslit = srcTranslit;
    }

    /**
     * @return The backend
     */
    public Integer getBackend() {
        return backend;
    }

    /**
     * @param backend The backend
     */
    public void setBackend(Integer backend) {
        this.backend = backend;
    }

}
