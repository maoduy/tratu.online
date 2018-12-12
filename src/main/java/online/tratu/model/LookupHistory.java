package online.tratu.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import online.tratu.login.model.User;

/**
 * @author Mao
 *
 */
@Entity
public class LookupHistory implements Serializable {

	private static final long serialVersionUID = 2661037776535481737L;
	
	private String word;
	
	@Enumerated(EnumType.STRING)
	private Type type;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne
	private User user;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
