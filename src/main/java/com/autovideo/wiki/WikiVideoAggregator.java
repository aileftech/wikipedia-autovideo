package com.autovideo.wiki;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WikiVideoAggregator {
	private static final WikiSectionAggregator agg = new WikiSectionAggregator();
	
	/**
	 * Merges together videos that have short length
	 * @param videos
	 * @return
	 */
	public List<WikiVideo> aggregateVideos(List<WikiVideo> videos) {
		List<WikiVideo> results = new ArrayList<>();
		
		int accumulatedLength = 0;
		int i = 0, start = 0;
		for ( ; i < videos.size() - 1; i++) {
			WikiVideo wikiVideo = videos.get(i);
			int length = wikiVideo.getEstimatedLength();
			int nextLength = videos.get(i + 1).getEstimatedLength();
			
			if (accumulatedLength + length + nextLength >= WikiSectionAggregator.MAX_VIDEO_LENGTH) {
				results.add(mergeVideos(videos.subList(start, i + 1)));
				start = i + 1;
				accumulatedLength = 0;
			} else {
				accumulatedLength += length;				
			}
		}
		
		if (results.isEmpty())
			i = 0;

		results.add(mergeVideos(videos.subList(i, videos.size())));
		
		return results;
	}
	
	private WikiVideo mergeVideos(List<WikiVideo> videos) {
		String createdTitle = "";
		
		if (videos.size() == 1)
			createdTitle = videos.get(0).getTitle();
		else if (videos.size() == 2) {
			createdTitle = videos.get(0).getTitle() + " and " + videos.get(1).getTitle();
		} else {
			createdTitle = videos.stream().limit(videos.size() - 1)
				.map(v -> v.getTitle())
				.collect(Collectors.joining(", "))
				+ " and " + videos.get(videos.size() - 1).getTitle();
		}
		
		WikiVideo result = new WikiVideo(createdTitle, videos.get(0).getWikipediaPage());
		
		for (int i = 0; i < videos.size(); i++) {
			WikiVideo wikiVideo = videos.get(i);
			wikiVideo.getSections().forEach(s -> result.addSection(s));
			result.setEstimatedLength(agg.estimateVideoLength(result));
		}
		
		return result;
	}
}
