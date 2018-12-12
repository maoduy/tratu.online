package online.tratu.login.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "role_id")
    private int id;
    @Column(name = "role")
    private String role;
	int getId() {
		return id;
	}
	void setId(int id) {
		this.id = id;
	}
	String getRole() {
		return role;
	}
	void setRole(String role) {
		this.role = role;
	}
    
    
}
