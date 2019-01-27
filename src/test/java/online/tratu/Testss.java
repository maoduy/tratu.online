package online.tratu;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;

public class Testss {

	@Test
	public void test() throws IOException {
		SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
		String[] tokens = tokenizer.tokenize("went");

		InputStream inputStreamPOSTagger = getClass().getResourceAsStream("/en-pos-maxent.bin");
		POSModel posModel = new POSModel(inputStreamPOSTagger);
		POSTaggerME posTagger = new POSTaggerME(posModel);
		String tags[] = posTagger.tag(tokens);

		System.out.println(tags);
	}

	@Test
	public void test2() throws IOException {
		SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
		String[] inputTokens = tokenizer.tokenize("went spoke told speaking books booking needed studies");
		Set<String> expectedOutput = new HashSet<String>(Arrays.asList("go", "speak", "tell", "book", "need", "study"));

		InputStream inputStreamPOSTagger = getClass().getResourceAsStream("/en-pos-maxent.bin");
		POSModel posModel = new POSModel(inputStreamPOSTagger);
		POSTaggerME posTagger = new POSTaggerME(posModel);
		String tags[] = posTagger.tag(inputTokens);
		InputStream dictLemmatizer = getClass().getResourceAsStream("/en-lemmatizer.dict");
		DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
		String[] lemmas = lemmatizer.lemmatize(inputTokens, tags);
		Set<String> lemasSet = new HashSet<String>(Arrays.asList(lemmas));

		assertEquals(lemasSet.size(), expectedOutput.size());
		assertTrue(lemasSet.containsAll(expectedOutput));
	}

	@Test
	public void givenEnglishModel_whenDetect_thenSentencesAreDetected() throws Exception {

		String paragraph = "This is a statement. This is another statement." + "Now is an abstract word for time, "
				+ "that is always flying. And my email address is google@gmail.com.";

		InputStream is = getClass().getResourceAsStream("/en-sent.bin");
		SentenceModel model = new SentenceModel(is);

		SentenceDetectorME sdetector = new SentenceDetectorME(model);

		String sentences[] = sdetector.sentDetect(paragraph);
		for (String item : sentences) {
			System.out.println(item);
		}
	}

}
