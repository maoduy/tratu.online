package online.tratu.view;

import java.util.List;

import online.tratu.model.Type;
import online.tratu.model.Word;

public class Paragraph {
	private String paragraph;
	private List<Word> words;
	private List<String> unknownWords;
	private Type type;
	private String msg;
	private boolean isLoggedIn;

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

	public List<String> getUnknownWords() {
		return unknownWords;
	}

	public void setUnknownWords(List<String> unknownWords) {
		this.unknownWords = unknownWords;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

}
