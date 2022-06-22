package com.autovideo.wikidata.datatypes;

public class WikidataPropertyValue {
	private String value;
	
	private WikidataPropertyType type;

	public WikidataPropertyValue(String value, WikidataPropertyType type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public WikidataPropertyType getType() {
		return type;
	}
	
}
