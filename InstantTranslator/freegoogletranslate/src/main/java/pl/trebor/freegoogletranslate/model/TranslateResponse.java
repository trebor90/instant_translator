package pl.trebor.freegoogletranslate.model;

import java.util.ArrayList;
import java.util.List;

public class TranslateResponse {

    private List<Sentence> sentences = new ArrayList<Sentence>();
    private List<Dict> dict = new ArrayList<Dict>();
    private String src;
    private Integer serverTime;

    /**
     *
     * @return
     * The sentences
     */
    public List<Sentence> getSentences() {
        return sentences;
    }

    /**
     *
     * @param sentences
     * The sentences
     */
    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    /**
     *
     * @return
     * The dict
     */
    public List<Dict> getDict() {
        return dict;
    }

    /**
     *
     * @param dict
     * The dict
     */
    public void setDict(List<Dict> dict) {
        this.dict = dict;
    }

    /**
     *
     * @return
     * The src
     */
    public String getSrc() {
        return src;
    }

    /**
     *
     * @param src
     * The src
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     *
     * @return
     * The serverTime
     */
    public Integer getServerTime() {
        return serverTime;
    }

    /**
     *
     * @param serverTime
     * The server_time
     */
    public void setServerTime(Integer serverTime) {
        this.serverTime = serverTime;
    }

}
