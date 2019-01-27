package online.tratu.services;

import java.util.Collections;
import java.util.List;
import java.util.Random;

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

	@Autowired
	private SecurityService securityService;

	private LookupHistoryService() {
	}

	public boolean createLookupHistory(String word, Type type, String sentence) {

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
				history.setType(type);
				history.setSentence(sentence);
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

		if (user != null) {
			LookupHistory history = new LookupHistory();
			history.setUser(user);

			Example<LookupHistory> ex = Example.of(history);
			return repo.findAll(ex);
		}

		return Collections.emptyList();
	}

	public LookupHistory findRandomHistory() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());

		List<LookupHistory> words = repo.findRandomHistory(String.valueOf(user.getId()));
		Random random = new Random();
		int randomIndex = random.nextInt(words.size() - 1);
		if (words!= null && !words.isEmpty()) {
			return words.get(randomIndex);
		}
		
		return null;
	}

	public boolean reducePoint(String word) {
		User user = securityService.getLoggedInUser();
		if (user != null) {
			repo.updatePoint(word, -3, user.getId());
			return true;
		}

		return false;
	}
	
	public boolean increasePoint(String word) {
		User user = securityService.getLoggedInUser();
		if (user != null) {
			repo.updatePoint(word, 1, user.getId());
			return true;
		}
		
		return false;
	}

}
