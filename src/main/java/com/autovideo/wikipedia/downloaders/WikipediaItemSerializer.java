package com.autovideo.wikipedia.downloaders;

import com.autovideo.wikipedia.datatypes.WikipediaItem;
import com.google.gson.Gson;

public class WikipediaItemSerializer {

	public String serialize(WikipediaItem item) {
		return item.toJson();
	}

	public WikipediaItem deserialize(String json) {
		return WikipediaItem.fromJson(json);
	}
	
}
