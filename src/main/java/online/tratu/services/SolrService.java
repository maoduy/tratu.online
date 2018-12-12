package online.tratu.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import online.tratu.model.SearchCriteria;
import online.tratu.model.Type;
import online.tratu.model.Word;

public class SolrService {
	private static SolrClient enviClient = null;
	private static SolrClient vienClient = null;
	private static SolrService instance = null;

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

	@SuppressWarnings("unchecked")
	public List<Word> search(String searchKey, Type dictType) {
		SolrQuery query = createSolrQuery(searchKey);
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
					Word word = new Word();
					words.add(word);
					word.setWord(((List<String>) doc.getFieldValue("word")).get(0));
					word.setMeaning(((List<String>) doc.getFieldValue("formatedMeaning")).get(0));
				}
				return words;
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}
	
	public List<Word> matchingSearch(List<String> searchKeys, Type dictType) {
		SolrQuery query = createMatchingSearchQuery(searchKeys);
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
					Word word = new Word();
					words.add(word);
					word.setWord(((List<String>) doc.getFieldValue("word")).get(0));
					word.setMeaning(((List<String>) doc.getFieldValue("formatedMeaning")).get(0));
				}
				return words;
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
		
		return Collections.emptyList();
	}

	private static SolrQuery createSolrQuery(String keyword) {
		SolrQuery query = new SolrQuery("word:" + keyword);
		query.setFields("id", "word", "formatedMeaning");
		query.setRows(10);

		return query;
	}
	
	private static SolrQuery createMatchingSearchQuery(List<String> keywords) {
		
		String params = StringUtils.join(keywords, " ");
		SolrQuery query = new SolrQuery("word_st:(" + params + ")");
		query.setFields("id", "word", "formatedMeaning");
		query.setRows(keywords.size());
		
		return query;
	}
}
