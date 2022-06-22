package com.autovideo.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.autovideo.utils.AtomicFloat;
import com.autovideo.utils.Language;
import com.autovideo.utils.Stopwords;
import com.autovideo.wiki.WikiSectionAggregator;
import com.autovideo.wiki.WikiVideo;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

/**
 * A very rough (and unreliable) method to extract a summary of a text
 * by selecting only the most important sentences,
 * based on a simple metric based on word counts.
 */
public class Summarizer {
	private static final WikiSectionAggregator agg = new WikiSectionAggregator();
	
	public void summarize(WikiVideo video) {
		int estimateVideoLength = agg.estimateVideoLength(video);
		
		if (estimateVideoLength < WikiSectionAggregator.MAX_VIDEO_LENGTH)
			return;
		
		while (true) {
			video.getSections().forEach(section -> {
				String summarize = summarize(section.getContent(), Language.EN, (int)(section.getContent().length() * 0.9), 0);
				section.setContent(summarize);
			});
			
			if (agg.estimateVideoLength(video) < WikiSectionAggregator.MAX_VIDEO_LENGTH)
				return;
		}
	}
	
	public String summarize(String text, Language language, int maxLength, int minLength) {
		Document document = new Document(text);

		if (maxLength == 0) return text;
		
		if (text.length() >= minLength && text.length() < maxLength) return text;
		
		Map<String, Double> frequencies = new HashMap<>();
		
		AtomicInteger countTokens = new AtomicInteger(0);
		document.sentences().forEach(sentence -> {
			sentence.tokens().forEach(token -> {
				if (!Stopwords.isStopword(token.lemma(), language)) {
					frequencies.merge(token.lemma(), 1.0, Double::sum);
					countTokens.getAndIncrement();
				}
			});
		});
		
		frequencies.keySet().forEach(k -> {
			frequencies.put(k, frequencies.get(k) / countTokens.get());
		});
		
		int sentenceIndex = 0;
		double avgScore = 0;
		for ( ; sentenceIndex < document.sentences().size(); sentenceIndex++) {
			Sentence sentence = document.sentence(sentenceIndex);
			
			double computeSentenceScore = computeSentenceScore(sentence, frequencies);
			avgScore += computeSentenceScore;
		}
		
		avgScore /= document.sentences().size();
		
		Map<Integer, Double> scores = new HashMap<>();
		
		for (sentenceIndex = 0; sentenceIndex < document.sentences().size(); sentenceIndex++) {
			Sentence sentence = document.sentence(sentenceIndex);
			double computeSentenceScore = computeSentenceScore(sentence, frequencies);
			
			scores.put(sentenceIndex, computeSentenceScore);
		}
		
		double startThreshold = avgScore * 0.8;
		String currentText = text;
		do {
			currentText = buildText(document.sentences(), startThreshold, scores);
			startThreshold *= 1.1;
		} while (currentText.length() >= maxLength);
		
		
		return currentText;
	}
	
	private String buildText(List<Sentence> sentences, double threshold, Map<Integer, Double> sentenceScores) {
		String result = "";
		
		for (int i = 0; i < sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			double score = sentenceScores.get(i);
			if (score >= threshold)
				result += sentence.text() + " ";
		}
		
		return result;
	}
	
	private double computeSentenceScore(Sentence sentence, Map<String, Double> frequencies) {
		AtomicFloat score = new AtomicFloat();
		sentence.tokens().forEach(token -> {
			score.set(score.get() + (float)frequencies.getOrDefault(token.lemma(), 0.0).doubleValue());
		});
		
		return score.get() / sentence.tokens().size();
	}
	
}
