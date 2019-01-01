package online.tratu.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import online.tratu.login.model.User;
import online.tratu.login.service.UserService;

@Service
public class SecurityService {
	@Autowired
	private UserService userService;

	public boolean isLoggedIn() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		return user != null;
	}

}
