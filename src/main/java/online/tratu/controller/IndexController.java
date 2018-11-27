package online.tratu.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import online.tratu.model.Type;
import online.tratu.model.Word;
import online.tratu.services.SolrService;
import online.tratu.services.UserService;


@Controller
public class IndexController {

    private final Logger logger = LoggerFactory.getLogger(IndexController.class);
    
    @Autowired
    private UserService userService;

    @GetMapping("/english-vietnamese/")
    public ModelAndView indexEnglishVietnamese() {
    	return new ModelAndView("ajax", "word", new Word(Type.EN_VI, null));
    }
    
    @GetMapping("/vietnamese-english/")
    public ModelAndView indexVietnameseEnglish() {
    	return new ModelAndView("ajax", "word", new Word(Type.VI_EN, null));
    }
    
	@GetMapping("/*-*/{word}")
	public ModelAndView view(@PathVariable("word") String word, HttpServletRequest request) {
		Type type = Type.getType(request.getRequestURI());
		List<Word> words = SolrService.getInstance().search(word, type);
		Word w = null;
        if (!words.isEmpty()) {
        	w = new Word(type, words.get(0).getMeaning());
        	w.setWord(word);
        } 
        
        if (w == null) {
        	w = new Word(type, null);
        }
		return new ModelAndView("ajax", "word", w);
	}

}