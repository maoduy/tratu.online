package online.tratu.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class SearchCriteria {

	@NotBlank(message = "Word can't empty!")
	String word;

	@NotNull(message = "Type must be not null!")
	Type type;

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

}