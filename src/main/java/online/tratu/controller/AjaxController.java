package online.tratu.controller;

import online.tratu.model.AjaxResponseBody;
import online.tratu.model.LookupHistory;
import online.tratu.model.SearchCriteria;
import online.tratu.model.Type;
import online.tratu.model.Word;
import online.tratu.services.SolrService;
import online.tratu.utils.OpenNLPUtils;
import online.tratu.view.Paragraph;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import online.tratu.services.CambridgeService;
import online.tratu.services.LookupHistoryService;
import online.tratu.services.SecurityService;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Valid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class AjaxController {

	CambridgeService userService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private LookupHistoryService lookupHistoryService;

	@Autowired
	public void setUserService(CambridgeService userService) {
		this.userService = userService;
	}

	private Set<String> sentences = new HashSet<String>();

	@PostMapping("/api/search")
	public ResponseEntity<?> getSearchResultViaAjax(@Valid @RequestBody SearchCriteria searchCriteria, Errors errors)
			throws Exception {

		AjaxResponseBody result = new AjaxResponseBody();

		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {

			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);

		}

		List<Word> words = SolrService.getInstance().search(searchCriteria.getWord(), searchCriteria.getType(), false);
		if (words.isEmpty()) {
			result.setMsg("no user found!");
		} else {
			result.setMsg("success");
		}
		result.setResult(words);

		return ResponseEntity.ok(result);

	}

	@PostMapping("/api/get_mp3")
	public ResponseEntity<?> getMp3(@RequestBody SearchCriteria search, Errors errors) {

		AjaxResponseBody result = new AjaxResponseBody();

		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {

			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);

		}

		String mp3Link = userService.getMp3Link(search.getWord());
		result.setMp3Link(mp3Link);

		return ResponseEntity.ok(result);

	}

	@PostMapping("api/autocomplete")
	public ResponseEntity<String> doAutoComplete(@Valid @RequestBody SearchCriteria searchCriteria) throws Exception {
		List<Word> strings = SolrService.getInstance().search(searchCriteria.getWord(), searchCriteria.getType(),
				false);
		ObjectMapper mapper = new ObjectMapper();
		String resp = "";
		try {
			resp = mapper.writeValueAsString(strings);
		} catch (JsonProcessingException e) {
		}
		return new ResponseEntity<String>(resp, HttpStatus.OK);
	}

	@PostMapping("api/dont-know-word")
	public ResponseEntity<String> dontKnowWord(@Valid @RequestBody SearchCriteria searchCriteria) throws Exception {
		if (securityService.isLoggedIn()) {
			lookupHistoryService.reducePoint(searchCriteria.getWord());

			List<Word> words = SolrService.getInstance().searchMatchingWords(Arrays.asList(searchCriteria.getWord()),
					Type.EN_VI, null);

			ObjectMapper mapper = new ObjectMapper();
			String result = mapper.writeValueAsString(words.get(0));

			//return new ResponseEntity<String>(result, HttpStatus.OK);
			return ResponseEntity.ok(result);
		}

		return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
	}
	
	@PostMapping("api/know-word")
	public ResponseEntity<String> knowWord(@Valid @RequestBody SearchCriteria searchCriteria) throws Exception {
		if (securityService.isLoggedIn()) {
			lookupHistoryService.increasePoint(searchCriteria.getWord());
			
			return new ResponseEntity<String>(HttpStatus.OK);
		}
		
		return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
	}

	@PostMapping("/api/history")
	public ResponseEntity<?> history(@Valid @RequestBody SearchCriteria search, Errors errors) {

		AjaxResponseBody result = new AjaxResponseBody();

		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {

			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);

		}

		String sentence = sentences.stream().filter(s -> s.toLowerCase().contains(search.getWord().toLowerCase()))
				.findAny().orElse(null);

		boolean createResult = lookupHistoryService.createLookupHistory(search.getWord().toLowerCase(),
				search.getType(), sentence);

		if (createResult) {
			result.setHistoryWords(lookupHistoryService.findAll());
		}

		return ResponseEntity.ok(result);
	}

	@PostMapping("/api/delete_history")
	public ResponseEntity<?> deleteHistory(@Valid @RequestBody SearchCriteria search, Errors errors) {

		AjaxResponseBody result = new AjaxResponseBody();

		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {

			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);

		}

		boolean createResult = lookupHistoryService.deleteLookupHistory(search.getWord(), search.getType());

		if (createResult) {
			result.setHistoryWords(lookupHistoryService.findAll());
		}

		return ResponseEntity.ok(result);
	}

	@PostMapping("/api/check-paragraph")
	public ResponseEntity<?> checkParagraph(@Valid @RequestBody Paragraph paragraph, Errors errors) throws IOException {
		Map<String, Set<String>> lemmaWordMap = new HashMap<>();
		Paragraph result = new Paragraph();

		Set<String> commonWords = new HashSet<>(FileUtils
				.readLines(new File(getClass().getClassLoader().getResource("dict/common-words.txt").getFile())));

		Set<String> historyWords = lookupHistoryService.findAll().stream().map(LookupHistory::getWord)
				.collect(Collectors.toSet());

		Set<String> words = new HashSet<>(Arrays.asList(paragraph.getParagraph().replaceAll("\n", " ").split(" ")));
		words.removeIf(item -> historyWords.contains(item.toLowerCase()) | commonWords.contains(item.toLowerCase())
				| item.length() < 2);

		Iterator<String> itr = words.iterator();
		Set<String> cleanedWords = new HashSet<>();

		Pattern shortFormPattern = Pattern.compile("('ll|'s|'d|'re|'m|'ve)+");
		Pattern notCharacterPattern = Pattern.compile("[^a-zA-Z'’`]+");
		String cleanedWord = null;
		while (itr.hasNext()) {
			String itrWord = itr.next();
			System.out.println("WORD " + itrWord);
			Matcher m1 = shortFormPattern.matcher(itrWord);
			Matcher m2 = notCharacterPattern.matcher(itrWord);
			if (m1.find() || m2.find()) {
				cleanedWord = itrWord.replaceAll("[^a-zA-Z'’`]", "").replaceAll("('ll|'s|'d|'re|'m|'ve)", "");
				cleanedWords.add(cleanedWord);
				System.out.println("CLEANED WORD: " + cleanedWord);
				itr.remove();
			}
		}

		if (!cleanedWords.isEmpty()) {
			words.addAll(cleanedWords);
		}

		words.removeIf(item -> historyWords.contains(item.toLowerCase()) | commonWords.contains(item.toLowerCase()));

		if (!words.isEmpty()) {
			Set<String> lemmaWords = null;
			String lemmaWord = null;
			for (String item : words) {
				// remove in common and history
				lemmaWords = OpenNLPUtils.getLemmas(item);
				if (!lemmaWords.isEmpty()) {
					lemmaWord = lemmaWords.iterator().next().toLowerCase();
					if (!historyWords.contains(lemmaWord) && !commonWords.contains(lemmaWord)) {
						if (lemmaWordMap.containsKey(lemmaWord)) {
							lemmaWordMap.get(lemmaWord).add(item);
						} else {
							lemmaWordMap.put(lemmaWord, new HashSet<>(Arrays.asList(item)));
						}
					}
				}
			}

			System.out.println(lemmaWordMap.keySet());

			if (!lemmaWordMap.keySet().isEmpty()) {
				List<Word> searchWords = SolrService.getInstance().searchWords(new ArrayList<>(lemmaWordMap.keySet()),
						Type.EN_VI, lemmaWordMap);

				// Try to search match for remain words
				if (lemmaWordMap.keySet().size() > 0) {
					List<Word> matchingWords = SolrService.getInstance()
							.searchMatchingWords(new ArrayList<>(lemmaWordMap.keySet()), Type.EN_VI, lemmaWordMap);
					if (!matchingWords.isEmpty()) {
						searchWords.addAll(matchingWords);
					}
				}

				if (searchWords != null) {
					sentences = extractSentences(paragraph.getParagraph());
					searchWords.forEach(item -> {
						sentences.forEach(s -> {
							item.getRelatedWords().forEach(relatedWord -> {
								if (s.toLowerCase().contains(relatedWord.toLowerCase())) {
									item.getSentences().add(s);
								}
							});
						});
					});
				}
				Set<String> unknownWords = new HashSet<>();
				lemmaWordMap.values().forEach(item -> unknownWords.addAll(item));
				System.out.println(lemmaWordMap.values());
				result.setWords(searchWords);
				result.setUnknownWords(unknownWords);
			}

			// If error, just return a 400 bad request, along with the error message
			if (errors.hasErrors()) {

				result.setMsg(errors.getAllErrors().stream().map(x -> x.getDefaultMessage())
						.collect(Collectors.joining(",")));
				return ResponseEntity.badRequest().body(result);

			}

			result.setLoggedIn(securityService.isLoggedIn());
		}

		return ResponseEntity.ok(result);
	}

	private Set<String> extractSentences(String paragraph) throws IOException {
		InputStream is = getClass().getResourceAsStream("/model/en-sent.bin");
		SentenceModel model = new SentenceModel(is);

		SentenceDetectorME sdetector = new SentenceDetectorME(model);

		String sentencesArr[] = sdetector.sentDetect(paragraph);

		return new HashSet<>(Arrays.asList(sentencesArr));
	}

}
