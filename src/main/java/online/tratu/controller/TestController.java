package online.tratu.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import online.tratu.login.model.User;
import online.tratu.login.service.UserService;
import online.tratu.model.LookupHistory;
import online.tratu.model.Test;
import online.tratu.model.Type;
import online.tratu.model.Word;
import online.tratu.services.SolrService;
import online.tratu.utils.OpenNLPUtils;
import online.tratu.view.Paragraph;
import online.tratu.services.CambridgeService;
import online.tratu.services.LookupHistoryService;

@Controller
public class TestController {

	private final Logger logger = LoggerFactory.getLogger(TestController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private LookupHistoryService lookupHistoryService;

	private String highlightWord = null;

	@GetMapping("/test")
	public ModelAndView test() {
		ModelAndView modelAndView = getTestModel();

		return modelAndView;
	}

	private ModelAndView getTestModel() {
		Test test = new Test();
		LookupHistory lookupHistory = lookupHistoryService.findRandomHistory();
		List<Word> words = SolrService.getInstance().searchMatchingWords(Arrays.asList(lookupHistory.getWord()),
				Type.EN_VI, null);

		String[] splittedWords = lookupHistory.getSentence().split(" ");
		highlightWord = null;
		for (String item : splittedWords) {
			Set<String> lemmaWords = OpenNLPUtils.getLemmas(item);
			if (lemmaWords.size() > 0) {
				lemmaWords.forEach(lemmaWord -> {
					if (lookupHistory.getWord().toLowerCase().equals(lemmaWord.toLowerCase())) {
						highlightWord = item;
					}
				});
			}

			if (highlightWord != null) {
				String highlightedSentence = lookupHistory.getSentence().replaceAll(highlightWord,
						"<b style='color:red'>" + highlightWord + "</b>");
				lookupHistory.setSentence(highlightedSentence);
				break;
			}
		}

		if (CollectionUtils.isNotEmpty(words)) {
			test.setWord(lookupHistory.getWord());
			test.setSentence(lookupHistory.getSentence());
		}

		ModelAndView modelAndView = new ModelAndView("test", "test", test);
		setUserSession(modelAndView);
		return modelAndView;
	}

	@PostMapping("/test")
	public ModelAndView testSubmit(@ModelAttribute Test test) {
		lookupHistoryService.increasePoint(test.getWord());
		ModelAndView modelAndView = getTestModel();

		return modelAndView;
	}

	private void setUserSession(ModelAndView modelAndView) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		if (user != null) {
			modelAndView.addObject("userName",
					"Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
			List<LookupHistory> historyWords = lookupHistoryService.findAll();
			modelAndView.addObject("historyWords", historyWords);
		}
	}

}