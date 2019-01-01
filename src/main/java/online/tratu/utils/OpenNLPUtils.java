package online.tratu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

public class OpenNLPUtils {
	private static SimpleTokenizer tokenizer = null;
	private static POSTaggerME posTagger = null;
	private static DictionaryLemmatizer lemmatizer = null;

	static {
		tokenizer = SimpleTokenizer.INSTANCE;
		InputStream inputStreamPOSTagger = OpenNLPUtils.class.getResourceAsStream("/model/en-pos-maxent.bin");
		POSModel posModel;
		try {
			posModel = new POSModel(inputStreamPOSTagger);
			posTagger = new POSTaggerME(posModel);

			InputStream dictLemmatizer = OpenNLPUtils.class.getResourceAsStream("/model/en-lemmatizer.dict");
			lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Set<String> getLemmas(String words) {
		String[] inputTokens = tokenizer.tokenize(words);
		String tags[] = posTagger.tag(inputTokens);
		String[] lemmas = lemmatizer.lemmatize(inputTokens, tags);

		Set<String> lemmasSet = new HashSet<>(Arrays.asList(lemmas));
		if (lemmasSet.contains("O")) {
			lemmasSet.remove("O");
		}

		return lemmasSet;
	}
}
