package online.tratu.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import online.tratu.login.model.User;
import online.tratu.login.service.UserService;
import online.tratu.model.LookupHistory;
import online.tratu.model.Type;
import online.tratu.model.Word;
import online.tratu.services.SolrService;
import online.tratu.view.Paragraph;
import online.tratu.services.CambridgeService;
import online.tratu.services.LookupHistoryService;

@Controller
public class TratuController {

	private final Logger logger = LoggerFactory.getLogger(TratuController.class);

	@Autowired
	private CambridgeService cambridgeService;

	@Autowired
	private UserService userService;

	@Autowired
	private LookupHistoryService lookupHistoryService;

	@GetMapping("/english-vietnamese/")
	public ModelAndView indexEnglishVietnamese() {
		ModelAndView modelAndView = new ModelAndView("ajax", "word", new Word(Type.EN_VI, null));
		setUserSession(modelAndView);

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

	@GetMapping("/vietnamese-english/")
	public ModelAndView indexVietnameseEnglish() {
		ModelAndView modelAndView = new ModelAndView("ajax", "word", new Word(Type.VI_EN, null));
		setUserSession(modelAndView);
		
		return modelAndView;
	}
	
	@GetMapping("/check-paragraph/")
	public ModelAndView checkParagraph() {
		ModelAndView modelAndView = new ModelAndView("check-paragraph", "model", new Paragraph());
		setUserSession(modelAndView);
		
		return modelAndView;
	}

	@GetMapping("/*-*/{word}")
	public ModelAndView view(@PathVariable("word") String word, HttpServletRequest request) throws Exception {
		Type type = Type.getType(request.getRequestURI());
		List<Word> words = SolrService.getInstance().search(word, type, true);

		lookupHistoryService.createLookupHistory(word.toLowerCase(), type, null);

		Word w = null;
		if (words.isEmpty()) {
			w = new Word(type, null); // Try to initialize to avoid UI error
		} else {
			w = new Word(type, words.get(0).getMeaning()); // Get first item
			w.setWord(word);
		}

		ModelAndView modelAndView = new ModelAndView("ajax", "word", w);
		List<LookupHistory> historyWords = lookupHistoryService.findAll();
		modelAndView.addObject("historyWords", historyWords);
		
		setUserSession(modelAndView);

		return modelAndView;
	}

}