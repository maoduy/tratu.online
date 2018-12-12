package online.tratu.controller;

import online.tratu.model.AjaxResponseBody;
import online.tratu.model.SearchCriteria;
import online.tratu.model.Type;
import online.tratu.model.Word;
import online.tratu.services.SolrService;
import online.tratu.view.Paragraph;
import online.tratu.services.CambridgeService;
import online.tratu.services.LookupHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AjaxController {

	CambridgeService userService;

	@Autowired
	private LookupHistoryService lookupHistoryService;

	@Autowired
	public void setUserService(CambridgeService userService) {
		this.userService = userService;
	}

	@PostMapping("/api/search")
	public ResponseEntity<?> getSearchResultViaAjax(@Valid @RequestBody SearchCriteria searchCriteria, Errors errors) {

		AjaxResponseBody result = new AjaxResponseBody();

		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {

			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);

		}

		List<Word> words = SolrService.getInstance().search(searchCriteria.getWord(), searchCriteria.getType());
		if (words.isEmpty()) {
			result.setMsg("no user found!");
		} else {
			result.setMsg("success");
		}
		result.setResult(words);

		return ResponseEntity.ok(result);

	}

	@PostMapping("/api/get_mp3")
	public ResponseEntity<?> getMp3(@Valid @RequestBody SearchCriteria search, Errors errors) {

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
	public ResponseEntity<String> doAutoComplete(@Valid @RequestBody SearchCriteria searchCriteria) {
		List<Word> strings = SolrService.getInstance().search(searchCriteria.getWord(), searchCriteria.getType());
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
	public ResponseEntity<?> checkParagraph(@Valid @RequestBody Paragraph paragraph, Errors errors) {
		
		String[] words = paragraph.getParagraph().split(" ");
		System.out.println(words);
		
		Paragraph result = new Paragraph();
		result.setWords(SolrService.getInstance().matchingSearch(Arrays.asList(words), Type.EN_VI));
		
		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {
			
			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);
			
		}
		
		return ResponseEntity.ok(result);
	}

}
