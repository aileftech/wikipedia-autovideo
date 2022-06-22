package com.autovideo.wiki;

import java.util.ArrayList;
import java.util.List;

import com.autovideo.img.PixabayVideo;
import com.autovideo.img.VideoImage;
import com.autovideo.video.EntityOverlay;
import com.autovideo.video.SectionOverlay;

public class WikiSection {
	private String title;
	
	private String content;

	private String parentTitle;
	
	private String pageTitle;
	
	private List<VideoImage> images = new ArrayList<>();
	
	private List<PixabayVideo> videos = new ArrayList<>();
	
	private String overlay;
	
	private SectionOverlay sectionOverlay = null; 
	
	private List<EntityOverlay> entityOverlays = new ArrayList<>();
	
	public WikiSection(String title, String content, String parentTitle, String pageTitle) {
		this.title = title;
		this.content = content;
		this.parentTitle = parentTitle;
		this.pageTitle = pageTitle;
	}
	
	public void setSectionOverlay(SectionOverlay sectionOverlay) {
		this.sectionOverlay = sectionOverlay;
	}
	
	public SectionOverlay getSectionOverlay() {
		return sectionOverlay;
	}
	
	public String getPageTitle() {
		return pageTitle;
	}
	
	public List<VideoImage> getImages() {
		return images;
	}
	
	public void setVideos(List<PixabayVideo> videos) {
		this.videos = videos;
	}
	
	public List<PixabayVideo> getVideos() {
		return videos;
	}
	
	public void addVideo(PixabayVideo video) {
		this.videos.add(video);
	}
	
	public String getParentTitle() {
		return parentTitle;
	}
	
	public void addImage(VideoImage image) {
		this.images.add(image);
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return "WikiSection [title=" + title + ", content=" + content + "]";
	}
	
	
	public void setOverlay(String overlay) {
		this.overlay = overlay;
	}
	
	public String getOverlay() {
		return overlay;
	}
	
	public boolean hasOverlay() {
		return overlay != null;
	}
}
