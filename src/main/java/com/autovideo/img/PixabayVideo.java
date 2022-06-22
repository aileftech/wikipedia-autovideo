package com.autovideo.img;

import java.util.Set;

public class PixabayVideo {
	private String previewURL;

	private String id;

	private int likes;
	
	private int duration;

	private Set<String> tags;
	
	private String downloadURL;
	
	public PixabayVideo(String previewURL, String downloadURL, String id, int likes, int duration, Set<String> tags) {
		this.previewURL = previewURL;
		this.downloadURL = downloadURL;
		this.id = id;
		this.likes = likes;
		this.tags = tags;
		this.duration = duration;
	}

	public String getPreviewURL() {
		return previewURL;
	}

	public String getId() {
		return id;
	}

	public int getLikes() {
		return likes;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public Set<String> getTags() {
		return tags;
	}
	
	public String getDownloadURL() {
		return downloadURL;
	}

	@Override
	public String toString() {
		return "PixabayVideo [previewURL=" + previewURL + ", id=" + id + ", likes=" + likes + ", duration=" + duration
				+ ", tags=" + tags + ", downloadURL=" + downloadURL + "]";
	}
	
	
}
