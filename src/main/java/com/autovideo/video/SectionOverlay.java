package com.autovideo.video;

import java.util.ArrayList;
import java.util.List;

public class SectionOverlay {
	private String title;
	
	private List<String> bulletPoints;

	public SectionOverlay(String title, List<String> points) {
		this.title = title;
		this.bulletPoints = points;
	}

	public SectionOverlay(String title) {
		this(title, new ArrayList<>());
	}
	
	public void addBulletPoint(String point) {
		this.bulletPoints.add(point);
	}
	
	public String getTitle() {
		return title;
	}
	
	public List<String> getBulletPoints() {
		return bulletPoints;
	}
}
