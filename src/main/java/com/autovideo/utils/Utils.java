package com.autovideo.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
	public static String getUrlForWikipediaImage(String image) {
		if (image == null || image.isEmpty()) return "http://via.placeholder.com/300x150";
		image = image.replace(" ", "_");
		String hash = Hashing.md5Hash(image);
		
		String s = hash.charAt(0) + "";
		String s1 = hash.substring(0, 2);
		
		return "https://upload.wikimedia.org/wikipedia/commons/" + s + "/" + s1 + "/" + image;
	}
	
	public static String getThumbUrlForWikipediaImage(String image) {
		if (image == null || image.isEmpty()) return "http://via.placeholder.com/300x150";
		image = image.replace(" ", "_");
		String hash = Hashing.md5Hash(image);
		
		String s = hash.charAt(0) + "";
		String s1 = hash.substring(0, 2);
		String url =  "https://upload.wikimedia.org/wikipedia/commons/thumb/" + s + "/" + s1 + "/" + image + "/400px-" + image;
		if (!url.endsWith(".svg")) url = url + ".png";
		return url;
	}
	
	/**
	 * Returns a Zipf distribution for the supplied elements
	 * @param elements
	 * @return
	 */
	public static Map<String, Double> zipf(List<String> elements) {
		Map<String, Double> zipf = new HashMap<>();
		
		double normalization = 0;
		for (int i = 0; i < elements.size(); i++) {
			zipf.put(elements.get(i), 1.0 / (i + 1));
			normalization += 1.0 / (i + 1);
		}
		
		for (String v : zipf.keySet()) {
			zipf.put(v, zipf.get(v) / normalization);
		}
		
		return zipf;
	}
}
