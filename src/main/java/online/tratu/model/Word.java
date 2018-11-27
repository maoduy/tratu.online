package online.tratu.model;

import org.apache.solr.client.solrj.beans.Field;

public class Word {
	@Field
	private String word;
	
	@Field
	private String meaning;
	
	@Field
	private String formatedMeaning;
	
	@Field
	private Type type;

	public Word() {
	}
	
	public Word(Type type, String meaning) {
		this.type = type;
		this.meaning = meaning;
	}
	
	public Word(Type type, String word, String meaning) {
		this.type = type;
		this.meaning = meaning;
		this.word = word;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getFormatedMeaning() {
		return formatedMeaning;
	}

	public void setFormatedMeaning(String formatedMeaning) {
		this.formatedMeaning = formatedMeaning;
	}
	

}
