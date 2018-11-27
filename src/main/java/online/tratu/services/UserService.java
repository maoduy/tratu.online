package online.tratu.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.stereotype.Service;

@Service
public class UserService {

	public String getMp3Link(String word) {
		try {
			URL oracle = new URL("https://dictionary.cambridge.org/dictionary/english/" + word);
			URLConnection yc = oracle.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;
			String mp3Link = null;
			while ((inputLine = in.readLine()) != null)
				if (inputLine.contains("data-src-mp3")) {
					mp3Link = inputLine.substring(inputLine.indexOf("data-src-mp3") + 15,
							inputLine.indexOf(".mp3") + 4);
					break;
				}
			in.close();
			
			return mp3Link;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
