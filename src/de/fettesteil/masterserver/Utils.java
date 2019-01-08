package de.fettesteil.masterserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Utils {
	public static JSONObject loadJsonFromFile(File f) {
		try {
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new FileReader(f));
			String ln;
			while ((ln = br.readLine()) != null)
				sb.append(ln);
			br.close();
			return (JSONObject) new JSONParser().parse(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error while loading json from " + f.getAbsolutePath());
		}
		return null;
	}
}
