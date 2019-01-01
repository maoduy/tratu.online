package online.tratu.controller;

import online.tratu.model.AjaxResponseBody;
import online.tratu.model.LookupHistory;
import online.tratu.model.SearchCriteria;
import online.tratu.model.Type;
import online.tratu.model.Word;
import online.tratu.services.SolrService;
import online.tratu.utils.OpenNLPUtils;
import online.tratu.view.Paragraph;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	@PostMapping("/api/history")
	public ResponseEntity<?> history(@Valid @RequestBody SearchCriteria search, Errors errors) {

		AjaxResponseBody result = new AjaxResponseBody();

		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {

			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);

		}

		boolean createResult = lookupHistoryService.createLookupHistory(search.getWord(), search.getType());

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
		Set<String> commonWords = new HashSet<>(FileUtils
				.readLines(new File(getClass().getClassLoader().getResource("dict/common-words.txt").getFile())));

		Map<String, Set<String>> lemmaWordMap = new HashMap<>();
		Paragraph result = new Paragraph();

		List<LookupHistory> historyWords = lookupHistoryService.findAll();
		List<String> words = new LinkedList<String>(new HashSet<>(
				Arrays.asList(paragraph.getParagraph().replaceAll("\n", " ").replaceAll("[^a-zA-Z ]", "").split(" "))));
		if (!words.isEmpty()) {
			Set<String> lemmaWords = null;
			String lemmaWord = null;
			for (String item : words) {
				lemmaWords = OpenNLPUtils.getLemmas(item);
				if (!lemmaWords.isEmpty()) {
					lemmaWord = lemmaWords.iterator().next();
					if (lemmaWordMap.containsKey(lemmaWord)) {
						lemmaWordMap.get(lemmaWord).add(item);
					} else {
						lemmaWordMap.put(lemmaWord, new HashSet<>(Arrays.asList(item)));
					}
				}
			}

			if (historyWords != null) {
				historyWords.forEach(historyWord -> lemmaWordMap.remove(historyWord.getWord()));
			}

			if (commonWords != null) {
				commonWords.forEach(commonWord -> lemmaWordMap.remove(commonWord));
			}
			// words.removeIf(w -> !w.toLowerCase().equals(w));
			// words.removeIf(word -> StringUtils.isEmptyOrWhitespace(word));

			System.out.println(lemmaWordMap.keySet());

			if (!lemmaWordMap.keySet().isEmpty()) {
				result.setWords(SolrService.getInstance().searchWords(new ArrayList<>(lemmaWordMap.keySet()),
						Type.EN_VI, lemmaWordMap));
				if (lemmaWordMap.keySet().size() > 0) {
					List<Word> matchingWords = SolrService.getInstance().searchMatchingWords(new ArrayList<>(lemmaWordMap.keySet()),
							Type.EN_VI, lemmaWordMap);
					if (!matchingWords.isEmpty()) {
						result.getWords().addAll(matchingWords);
					}
				}
				List<String> unknownWords = new ArrayList<>();
				lemmaWordMap.values().forEach(item -> unknownWords.addAll(item));
				System.out.println(lemmaWordMap.values());
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

}
