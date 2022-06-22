package com.autovideo.wiki;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.autovideo.text.Summarizer;
import com.autovideo.wikipedia.datatypes.WikipediaItem;

public class WikiSectionAggregator {
	public static final int WPM = 120;
	
	public static final int MAX_VIDEO_LENGTH = 18;
	
	private static final Summarizer summarizer = new Summarizer();
	
	public int estimateVideoLength(WikiSection section) {
		return (section.getContent().split(" ").length / WPM) + 1;
	}
	
	public int estimateVideoLength(WikiVideo video) {
		return estimateVideoLength(video.getSections());
	}
	
	
	public int estimateVideoLength(List<WikiSection> sections) {
		return sections.stream().map(s -> estimateVideoLength(s))
			.reduce(Integer::sum)
			.orElse(0);
	}
	
	public List<WikiVideo> createVideos(List<WikiSection> sections, WikipediaItem page) {
		List<WikiVideo> results = new ArrayList<>();
		
		Map<String, List<WikiSection>> groupedSections = new LinkedHashMap<>();
		
		sections.forEach(s -> {
			groupedSections.putIfAbsent(s.getParentTitle(), new ArrayList<>());
			groupedSections.get(s.getParentTitle()).add(s);
		});
		
		for (String section : groupedSections.keySet()) {
			List<WikiSection> ss = groupedSections.get(section);
			
			if (ss.isEmpty()) continue;
			
			WikiVideo v = new WikiVideo(ss, section, page);
			summarizer.summarize(v);
			v.setEstimatedLength(estimateVideoLength(ss));
			results.add(v);
		}
		
		return results;
	}
}
