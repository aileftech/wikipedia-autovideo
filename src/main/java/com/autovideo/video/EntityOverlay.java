package com.autovideo.video;

public class EntityOverlay {
	private String text;
	
	private double startTime, endTime;
	
	private String wikipediaPageTitle;
	
	private int inputIndex = -1;
	
	public EntityOverlay(String text, double startTime, double endTime, String wikipediaPageTitle) {
		this.text = text;
		this.startTime = startTime;
		this.endTime = endTime;
		this.wikipediaPageTitle = wikipediaPageTitle;
	}
	
	public String getWikipediaPageTitle() {
		return wikipediaPageTitle;
	}

	public String getText() {
		return text;
	}

	public void setInputIndex(int inputIndex) {
		this.inputIndex = inputIndex;
	}
	
	public int getInputIndex() {
		return inputIndex;
	}
	
	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}
	
	@Override
	public String toString() {
		return "EntityOverlay [text=" + text + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}
	
	
}
