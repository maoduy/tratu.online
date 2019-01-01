package online.tratu.services;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import online.tratu.dict.StarDictParser;
import online.tratu.dict.WordPosition;
import online.tratu.model.Type;
import online.tratu.model.Word;

public class DictionaryIndexer {
	private static StarDictParser starDictParser;
	private static Set<String> commonWords = null;
	private static final Logger LOG = LogManager.getLogger(DictionaryIndexer.class);

	public static void main(String[] args) throws Exception {
		LOG.info("STARTING THE INDEX JOB NOW ...");
		DictionaryIndexer vocaIndexing = new DictionaryIndexer();
		vocaIndexing.indexWords(Type.EN_VI, "dict/en_vi.idx", "dict/en_vi.dict");
		// vocaIndexing.indexWords(Type.VI_EN, "dict/vi_en.idx", "dict/vi_en.dict");
		// vocaIndexing.indexWords(Type.VI_EN, "dict/mtBab-EVE2.idx",
		// "dict/mtBab-EVE2.dict");
		// vocaIndexing.indexWords(Type.VI_EN, "dict/mtBab-VE.idx",
		// "dict/mtBab-VE.dict");
	}

	private void indexWords(Type type, String indexPath, String dictPath) throws Exception {
		try {
			commonWords = new HashSet<>(FileUtils
					.readLines(new File(getClass().getClassLoader().getResource("dict/common-words.txt").getFile())));
			SolrService.getInstance().emptyData(type);

			starDictParser = new StarDictParser().initStarDictParser(indexPath, dictPath);
			System.out.println("Dict size: " + starDictParser.getWords().size());
			int counter = 0;
			long start = System.currentTimeMillis();
			
			for (Map.Entry<String, WordPosition> en : starDictParser.getWords().entrySet()) {

				/*if (!en.getKey().equals("catch") && !en.getKey().equals("do")) {
					continue;
				}*/
				System.out.println(
						String.format("============= %d Executing for %s =============", counter, en.getKey()));
				

				try {
					String meaning = starDictParser.getWordExplanation(en.getValue().getStartPos(),
							en.getValue().getLength());
					Word word = new Word();
					word.setMeaning(meaning);
					word.setWord(en.getKey());
					word.setType(type);
					// word.setFormatedMeaning(meaning); //formatMeaning(en.getKey(), meaning));
					word.setFormatedMeaning(formatMeaning(en.getKey(), meaning)); // formatMeaning(en.getKey(),
																					// meaning));
					SolrService.getInstance().addBean(type, word);
					if (counter++ % 500 == 0) {
						SolrService.getInstance().commit(type);
					}

				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(e);
				}

			}

			// Last items
			SolrService.getInstance().commit(type);
			
			System.out.println(String.format("Finish indexing, it took {0} miliseconds.", System.currentTimeMillis() - start));

		} catch (IOException | SolrServerException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static String formatMeaning(String word, String meaning) {
		String logLine = null;
		try {
			// System.out.println(string);
			String[] lines = meaning.split("\n");
			StringBuilder sb = new StringBuilder();

			boolean lastLineWithMark = false;
			boolean printSentenceStructure = false;
			String lineAlgin = "<span style='margin-left: 15px'/>";
			String sentenceStructure = "<span style='font-weight: bold'>Cấu trúc từ</span><br/>";
			String align10px = "<span style='margin-left: 10px'/>";
			String marginLeft = new String();
			// System.out.println(meaning);
			// System.out.println("=======================");
			for (String line : lines) {
				logLine = line;
				// System.out.println(line);
				if (line.startsWith("@")) {
					if (line.contains("@" + word)) {
						line = String.format("<br/><span style='font-size:20px; margin-top: 10px'>%s</span>",
								line.substring(1));
						line += " <i id='speaker' class=\"fas fa-volume-up\" style='margin-left: 20px' word=" + word
								+ " onclick=\"getMp3LinkAndPlay('" + word + "');\"></i>";
					} else {
						line = String.format("<br/><span style='margin-top: 10px; font-weight: bold'>%s</span>",
								line.substring(1));
					}

				} else if (line.startsWith("*")) {
					line = String.format("<br/><span style='font-weight: bold'>%s</span>", line.substring(1));
					printSentenceStructure = false;
					lastLineWithMark = false;
				} else if (line.startsWith("=")) {
					int indexOfPlus = line.indexOf("+");
					if (indexOfPlus > 0) {
						String englishSentence = line.substring(1, indexOfPlus);
						englishSentence = addLookupLink(englishSentence.trim(), word);
						String vietSentence = line.substring(indexOfPlus + 1);
						// vietSentence = addLookupLink(vietSentence.trim());
						line = "=" + englishSentence + "+" + vietSentence;
						indexOfPlus = line.indexOf("+");
						marginLeft = "15px";
						if (lastLineWithMark) {
							sb.append(align10px);
							marginLeft = "25px";
						}
						line = "<span style='color:#002bb8; margin-left: 15px;'>" + line.substring(1, indexOfPlus)
								+ "</span><br/><span style='color:gray; margin-left: " + marginLeft
								+ "; padding-bottom: 25px'>" + line.substring(indexOfPlus + 1) + "</span>";
					}
				} else if (line.startsWith("-")) {
					line = "<i class='fas fa-caret-right' style='font-size: 10px'></i>" + line.substring(1);
					if (lastLineWithMark) {
						sb.append(lineAlgin);
					}
				} else if (line.startsWith("!")) {
					String englishSentence = line.substring(1);
					englishSentence = addLookupLink(englishSentence.trim(), word);
					line = "!" + englishSentence;
					line = "<span style='color:#002bb8; margin-left: 15px'>" + line.substring(1) + "</span>";
					lastLineWithMark = true;
					if (!printSentenceStructure) {
						sb.append(sentenceStructure);
						printSentenceStructure = true;
					}
				}
				sb.append(line).append("</br>");
			}

			return sb.toString();
		} catch (Exception e) {
			System.err.println(logLine);
			e.printStackTrace();
			LOG.error(logLine + ":: " + e.getMessage());
			throw e;
		}
	}

	private static String addLookupLink(String line, String word) {
		try {
			// System.out.println(line);
			String[] words = line.split(" ");
			for (int i = 0; i < words.length; i++) {
				if (word.equals(words[i])) {
					words[i] = String.format("<span style='color:#c00022'>%s</span>", words[i]);
				} else if (!commonWords.contains(words[i])) {
					words[i] = String.format("<a href='%s'>%s</a>", words[i].replaceAll("[^a-zA-Z ]", ""), words[i]);
				}
			}
			return String.join(" ", words);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
