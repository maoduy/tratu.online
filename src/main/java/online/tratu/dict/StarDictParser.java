package online.tratu.dict;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

/**
 * StarDict dictionary file parser usage as follows:<br/>
 * <code>StarDictParser rdw=new StarDictParser();</code><br/>
 * <code>rdw.loadIndexFile(f);</code><br/>
 * <code>rdw.loadContentFile(fcnt);</code><br/>
 * 
 * @author beethoven99@126.com
 */
public class StarDictParser {

	private byte buf[] = new byte[1024];
	private int smark;
	private int mark;
	private static Map<String, WordPosition> words = new HashMap<String, WordPosition>();
	public static final int MAX_RESULT = 40;
	private RandomAccessFile randomAccessFile;

	public List<Map.Entry<String, WordPosition>> searchWord(String term) {
		List<Entry<String, WordPosition>> resa = new ArrayList<Map.Entry<String, WordPosition>>();
		List<Entry<String, WordPosition>> resb = new ArrayList<Map.Entry<String, WordPosition>>();

		int i = -1;

		// Set<String> fields = new HashSet<>();
		for (Map.Entry<String, WordPosition> en : words.entrySet()) {
			if (en.getKey() == null) {
				System.out.println("oh no null");
			}
			if (en.getKey().toLowerCase().equals(term.toLowerCase())) {
				resa.add(en);
				break;
			} /*
				 * else if (i > 0 && resb.size() < MAX_RESULT) { resb.add(en); } if (resa.size()
				 * > MAX_RESULT) { break; }
				 */

			// String meaning = getWordExplanation(en.getValue().getStartPos(),
			// en.getValue().getLength());

//			System.out.println("=========== " + en.getKey() + " ===============");
//			System.out.println(meaning);

//			Matcher m = Pattern.compile("(?<=@Lĩnh vực:)(.*?)(?=\\n)").matcher(meaning);
//			while (m.find()) {
//				fields.add(m.group());
//			}

		}

		Collections.sort(resa, WordComparator);
		Collections.sort(resb, WordComparator);

		if (resa.size() < MAX_RESULT) {
			int need = MAX_RESULT - resa.size();
			if (need > resb.size()) {
				need = resb.size();
			}
			resa.addAll(resb.subList(0, need));
		}

		return resa;
	}

	private boolean hasOnlyMeaning(String str) {
		int plusIndex = str.indexOf("+");
		int thangIndex = str.indexOf("!");
		int equalIndex = str.indexOf("=");
		int xaydungIndex = str.indexOf("@Lĩnh vực: điện tử & viễn thông");
		int lastXaydungIndex = str.lastIndexOf("@Lĩnh vực: điện tử & viễn thông");

		return plusIndex == -1 && thangIndex == -1 && equalIndex == -1 && xaydungIndex >= 0
				&& lastXaydungIndex != xaydungIndex;
	}

	public boolean loadContentFile(File f) {
		try {
			this.randomAccessFile = new java.io.RandomAccessFile(f, "r");
			System.out.println("is file open valid: " + this.randomAccessFile.getFD().valid());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String getWordExplanation(int start, int len) {
		String res = "";
		byte[] buf = new byte[len];
		try {
			this.randomAccessFile.seek(start);
			int ir = this.randomAccessFile.read(buf);
			if (ir != len) {
				System.out.println("Error occurred, not enought bytes read, wanting:" + len + ",got:" + ir);
			}
			res = new String(buf, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void loadIndexFile(File f) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			int res = fis.read(buf);
			while (res > 0) {
				mark = 0;
				smark = 0;
				parseByteArray(buf, 1024);
				if (mark == res) {
					res = fis.read(buf);
				} else {
					// System.out.println("å†™ buf from: "+(buf.length-smark)+", len:"+(smark));
					res = fis.read(buf, buf.length - smark, smark);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * parse a block of bytes
	 */
	private void parseByteArray(byte buf[], int len) throws UnsupportedEncodingException {
		for (; mark < len;) {
			if (buf[mark] != 0) {
				if (mark == len - 1) {
					System.arraycopy(buf, smark, buf, 0, len - smark);
					break;
				} else {
					mark++;
				}
			} else {
				String tword = null;
				if (mark != 0) {
					byte[] bs = ArrayUtils.subarray(buf, smark, mark);
					tword = new String(bs, "utf-8");
				}

				if (len - mark > 8) {
					smark = mark + 9;
					byte[] bstartpos = ArrayUtils.subarray(buf, mark + 1, mark + 5);
					byte[] blen = ArrayUtils.subarray(buf, mark + 5, mark + 9);
					int startpos = ByteArrayHelper.toIntAsBig(bstartpos);
					int strlen = ByteArrayHelper.toIntAsBig(blen);
					if (tword != null && tword.trim().length() > 0 && strlen < 10000) {
						words.put(tword, new WordPosition(startpos, strlen));
					}
					mark += 8;
				} else {
					System.arraycopy(buf, smark, buf, 0, len - smark);
					break;
				}
			}
		}
	}

	public Map<String, WordPosition> getWords() {
		return words;
	}

	public void setWords(Map<String, WordPosition> words) {
		this.words = words;
	}

	/**
	 * customer comparator
	 */
	private static Comparator<Map.Entry<String, WordPosition>> WordComparator = new Comparator<Map.Entry<String, WordPosition>>() {
		public int compare(Map.Entry<String, WordPosition> ea, Map.Entry<String, WordPosition> eb) {
			return ea.getKey().compareToIgnoreCase(eb.getKey());
		}
	};

	public StarDictParser initStarDictParser(String indexPath, String dictPath) {
		ClassLoader classLoader = getClass().getClassLoader();
		File idxFile = new File(classLoader.getResource(indexPath).getFile());
		File dictFile = new File(classLoader.getResource(dictPath).getFile());
		StarDictParser rdw = new StarDictParser();
		rdw.loadIndexFile(idxFile);
		rdw.loadContentFile(dictFile);

		return rdw;
	}


	static StarDictParser rdw = new StarDictParser().initStarDictParser("dict/en_vi.idx", "dict/en_vi.dict");

	public static List<String> search(String word) {
		List<Entry<String, WordPosition>> hi = rdw.searchWord(word);

		List<String> definitions = new ArrayList<>();
		for (Map.Entry<String, WordPosition> en : hi) {
			definitions.add(rdw.getWordExplanation(en.getValue().getStartPos(), en.getValue().getLength()));
		}

		return definitions;
	}

	public static boolean isWordExisting(String word) {
		boolean isExisting = false;
		for (Map.Entry<String, WordPosition> en : words.entrySet()) {
			if (en.getKey() == null) {
				System.out.println("oh no null");
			}
			if (en.getKey().toLowerCase().equals(word.toLowerCase())) {
				isExisting = true;
				break;
			}
		}

		return isExisting;
	}

	/**
	 * test only
	 */
	public void testByConsole() {
		java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
		String s = null;

		try {
			s = br.readLine();
			while (s.length() > 0) {
				System.out.println(s);
				List<Map.Entry<String, WordPosition>> res = this.searchWord(s);
				int i = 0;
				for (Map.Entry<String, WordPosition> en : res) {
					System.out.println(i++ + " : " + en.getKey());
				}
				s = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
