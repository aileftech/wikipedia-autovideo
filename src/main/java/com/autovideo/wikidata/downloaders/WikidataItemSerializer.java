package com.autovideo.wikidata.downloaders;

import com.autovideo.wikidata.datatypes.WikidataItem;

public class WikidataItemSerializer  {

	public String serialize(WikidataItem item) {
		return item.toJson();
	}

	public WikidataItem deserialize(String json) {
		return WikidataItem.fromJson(json);
	}
	
}
