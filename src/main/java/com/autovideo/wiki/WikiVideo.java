package com.autovideo.wiki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.autovideo.img.VideoImage;
import com.autovideo.wikipedia.datatypes.WikipediaItem;

public class WikiVideo {
	private String pageTitle;
	
	private List<WikiSection> sections = new ArrayList<>();

	private String title;
	
	private int estimatedLength;
	
	private WikipediaItem wikipediaPage;
	
	public VideoImage sampleRandomImage() {
		List<VideoImage> collect = 
				sections.stream().flatMap(x -> x.getImages().stream())
					.collect(Collectors.toList());
		
		Collections.shuffle(collect);
		
		if (collect.size() == 0) return null;
		
		return collect.get(0);
	}
	
	public WikiVideo(String title, WikipediaItem wikipediaPage) {
		this.title = title;
		this.pageTitle = wikipediaPage.getTitle();
		this.wikipediaPage = wikipediaPage;
	}
	
	public WikiVideo(List<WikiSection> sections, String title, WikipediaItem wikipediaPage) {
		this(title, wikipediaPage);
		
		this.sections = sections;
	}
	
	public WikipediaItem getWikipediaPage() {
		return wikipediaPage;
	}
	
	public void addSection(WikiSection section) {
		this.sections.add(section);
	}
	
	public List<WikiSection> getSections() {
		return sections;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setEstimatedLength(int estimatedLength) {
		this.estimatedLength = estimatedLength;
	}
	
	public String getPageTitle() {
		return pageTitle;
	}
	
	public int getEstimatedLength() {
		return estimatedLength;
	}
}
