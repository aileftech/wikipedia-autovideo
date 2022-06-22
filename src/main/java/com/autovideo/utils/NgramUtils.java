package com.autovideo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility methods to compute ngrams on a variety of objects.
 */
public class NgramUtils {

	/**
	 * Computes ngrams on a list of strings
	 * @param tokens the list of strings
	 * @param length the length of the ngrams
	 * @return
	 */
	public static List<String> ngrams(List<String> tokens, int length) {
		List<String> ngrams = new ArrayList<>();
		for (int i = 0; i < tokens.size() - length + 1; i++) {
			String ngram = 
				tokens.subList(i, i + length)
					.stream()
					.collect(Collectors.joining(" "));
			ngrams.add(ngram.toLowerCase());
		}
		
		return ngrams;
	}
	
	/**
	 * Computes char n-grams on a string
	 * @param string the input string
	 * @param length the length of the ngrams
	 * @return
	 */
	public static List<String> ngrams(String string, int length) {
		List<String> ngrams = new ArrayList<>();
		
		for (int i = 0; i < string.length() - length + 1; i++) {
			String ngram = string.substring(i, i + length);
			ngrams.add(ngram.toLowerCase());
		}
		
		return ngrams;
	}
}