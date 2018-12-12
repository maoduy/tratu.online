package online.tratu.model;

import java.util.List;

public class AjaxResponseBody {

    String msg;
    List<Word> result;
    String mp3Link;
    List<LookupHistory> historyWords;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Word> getResult() {
        return result;
    }

    public void setResult(List<Word> result) {
        this.result = result;
    }

	public String getMp3Link() {
		return mp3Link;
	}

	public void setMp3Link(String mp3Link) {
		this.mp3Link = mp3Link;
	}

	public List<LookupHistory> getHistoryWords() {
		return historyWords;
	}

	public void setHistoryWords(List<LookupHistory> historyWords) {
		this.historyWords = historyWords;
	}

}
