package online.tratu.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import online.tratu.model.Type;
import online.tratu.model.Word;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

public class SolrService {
	private static SolrClient enviClient = null;
	private static SolrClient vienClient = null;
	private static SolrService instance = null;
	private static DictionaryLemmatizer lemmatizer = null;

	private SolrService() {
	}

	public static SolrService getInstance() {
		if (instance == null) {
			instance = new SolrService();
			enviClient = new HttpSolrClient("http://localhost:8983/solr/en_vi");
			vienClient = new HttpSolrClient("http://localhost:8983/solr/vi_en");
		}

		return instance;
	}

	protected void emptyData(Type type) throws SolrServerException, IOException {
		getClient(type).deleteByQuery("*");
		getClient(type).commit();
	}

	private SolrClient getClient(Type type) {
		switch (type) {
		case EN_VI:
			return enviClient;
		case VI_EN:
			return vienClient;
		default:
			break;
		}

		return null;
	}

	protected void addBean(Type type, Object object) throws IOException, SolrServerException {
		getClient(type).addBean(object);
	}

	public void commit(Type type) throws SolrServerException, IOException {
		getClient(type).commit();
	}

	public List<Word> search(String searchKey, Type dictType, boolean isMatchingSearch) throws Exception {
		SolrQuery query = null;

		if (!isMatchingSearch) {
			query = createSolrQuery(searchKey);
		} else {
			query = createMatchingSearchSolrQuery(searchKey);
		}

		List<Word> words = searchWithQuery(Arrays.asList(searchKey), dictType, query, null);

		if (CollectionUtils.isNotEmpty(words)) {
			return words;
		}

		return Collections.emptyList();
	}

	public List<Word> searchWords(List<String> searchKeys, Type dictType, Map<String, Set<String>> lemmaWordMap) {
		/*
		 * searchKeys = getLemmas(searchKeys); SolrQuery matchQuery =
		 * createMatchingSearchQuery(searchKeys); List<Word> words =
		 * searchWithQuery(searchKeys, dictType, matchQuery); if (!words.isEmpty()) {
		 * Set<String> foundWords =
		 * words.stream().map(Word::getWord).collect(Collectors.toSet());
		 * 
		 * // Remove found words, continue searching un-found words..
		 * searchKeys.removeIf(word -> foundWords.contains(word)); }
		 */

		SolrQuery query = createSearchQuery(searchKeys);
		List<Word> words2 = searchWithQuery(searchKeys, dictType, query, lemmaWordMap);
		/*
		 * if (words2 != null && !words2.isEmpty()) { words.addAll(words2); }
		 */

		return words2;
	}
	
	public List<Word> searchMatchingWords(List<String> searchKeys, Type dictType, Map<String, Set<String>> lemmaWordMap) {
		SolrQuery query = createMatchingSearchQuery(searchKeys);
		List<Word> words = searchWithQuery(searchKeys, dictType, query, lemmaWordMap);
		return words;
	}

	private SolrQuery createMatchingSearchSolrQuery(String searchKey) {
		SolrQuery query = new SolrQuery("word:" + searchKey);
		query.setFields("id", "word", "formatedMeaning");
		query.setRows(1);

		return query;
	}

	private static SolrQuery createSolrQuery(String keyword) {
		SolrQuery query = new SolrQuery("word:(" + keyword + " OR " + keyword + "*)");
		query.setFields("id", "word", "formatedMeaning");
		query.setRows(10);

		return query;
	}

	private static SolrQuery createMatchingSearchQuery(List<String> keywords) {
		String params = StringUtils.join(keywords, " OR ");
		SolrQuery query = new SolrQuery("word_str:(" + params + ")");
		query.setFields("id", "word", "formatedMeaning");
		query.setRows(keywords.size() * 5);

		return query;
	}

	private static SolrQuery createSearchQuery(List<String> keywords) {
		String params = StringUtils.join(keywords, " OR ");
		SolrQuery query = new SolrQuery("word:(" + params + ")");
		query.setFields("id", "word", "formatedMeaning");
		query.setRows(keywords.size() * 5);

		return query;
	}

	private List<Word> searchWithQuery(List<String> searchKeys, Type dictType, SolrQuery query,
			Map<String, Set<String>> lemmaWordMap) {
		QueryResponse response = null;

		try {
			System.out.println(query.toQueryString());
			System.out.println(query.toString());
			if (Type.EN_VI == dictType) {
				response = enviClient.query(query);
			} else if (Type.VI_EN == dictType) {
				response = vienClient.query(query);
			}
			System.out.println("status : " + response.getStatus());
			System.out.println("QTime : " + response.getQTime());
			System.out.println("numFound : " + response.getResults().getNumFound());
			SolrDocumentList list = response.getResults();
			if (!list.isEmpty()) {
				List<Word> words = new ArrayList<>();
				for (SolrDocument doc : list) {
					System.out.println("------------" + doc.getFieldValue("word"));
					System.out.println("++++++++++++: " + doc.getFieldValue("meaning"));
					String wordStr = ((List<String>) doc.getFieldValue("word")).get(0);
					if (lemmaWordMap == null || lemmaWordMap.get(wordStr) != null) {
						Word word = new Word();
						words.add(word);
						if (lemmaWordMap != null) {
							word.setWord(lemmaWordMap.get(wordStr).toString());
							lemmaWordMap.remove(wordStr);
						} else {
							word.setWord(wordStr);
						}
						word.setMeaning(((List<String>) doc.getFieldValue("formatedMeaning")).get(0));
					}
				}
				return words;
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

}
