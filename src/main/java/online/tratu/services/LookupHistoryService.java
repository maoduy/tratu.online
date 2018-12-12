package online.tratu.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import online.tratu.login.model.User;
import online.tratu.login.repository.LookupHistoryRepository;
import online.tratu.login.service.UserService;
import online.tratu.model.LookupHistory;
import online.tratu.model.Type;

@Service
public class LookupHistoryService {

	@Autowired
	private UserService userService;

	@Autowired
	private LookupHistoryRepository repo;

	private LookupHistoryService() {
	}

	public boolean createLookupHistory(String word, Type type) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		if (user != null) {
			LookupHistory history = new LookupHistory();
			history.setUser(user);
			history.setWord(word);
			history.setType(type);
			Example<LookupHistory> ex = Example.of(history);
			long count = repo.count(ex);

			if (count == 0) {
				repo.save(history);
				return true;
			}
		}

		return false;
	}
	
	public boolean deleteLookupHistory(String word, Type type) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		if (user != null) {
			LookupHistory history = new LookupHistory();
			history.setUser(user);
			history.setWord(word);
			history.setType(type);
			Example<LookupHistory> ex = Example.of(history);
			List<LookupHistory> histories = repo.findAll(ex);
			
			if (histories.size() > 0) {
				repo.deleteById(histories.get(0).getId());
				return true;
			}
		}
		
		return false;
	}

	public List<LookupHistory> findAll() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		LookupHistory history = new LookupHistory();
		history.setUser(user);

		Example<LookupHistory> ex = Example.of(history);
		return repo.findAll(ex);
	}

}
