package com.autovideo.img;

public class VideoImage {
	private String previewURL;

	private String largeImageURL;

	private String id;

	private int likes;

	private String caption;
	
	private String section;

	public VideoImage(String previewURL, String largeImageURL, String id, int likes) {
		this.previewURL = previewURL;
		this.largeImageURL = largeImageURL;
		this.id = id;
		this.likes = likes;
	}
	
	public void setSection(String section) {
		this.section = section;
	}
	
	public String getSection() {
		return section;
	}

	public String getPreviewURL() {
		return previewURL;
	}

	public void setCaption(String description) {
		this.caption = description;
	}

	public String getCaption() {
		return caption;
	}

	public String getLargeImageURL() {
		return largeImageURL;
	}

	public String getId() {
		return id;
	}

	public int getLikes() {
		return likes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VideoImage other = (VideoImage) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VideoImage [previewURL=" + previewURL + ", id=" + id + ", likes=" + likes + "]";
	}
}