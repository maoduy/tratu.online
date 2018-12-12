package online.tratu.view;

import java.util.List;

import online.tratu.model.Type;
import online.tratu.model.Word;

public class Paragraph {
	private String paragraph;
	private List<Word> words;
	private Type type;
	private String msg;

	public String getParagraph() {
		return paragraph;
	}

	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
