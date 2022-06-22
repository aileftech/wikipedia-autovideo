package com.autovideo.wikidata.downloaders;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.fluent.Request;

import com.autovideo.utils.Language;
import com.autovideo.utils.LocalizedText;
import com.autovideo.wikidata.datatypes.WikidataItem;
import com.autovideo.wikidata.datatypes.WikidataPropertyType;
import com.autovideo.wikidata.datatypes.WikidataPropertyValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class WikidataItemDownloader {
	private static final String API_URL = "https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&ids=";
	
	private static final Gson gson = new Gson();
	
	public WikidataItem parseEntity(JsonObject entity) {
		WikidataItem result = new WikidataItem();
		
		String id = null;
		try {
			id = entity.get("id").getAsString();
		} catch (NullPointerException e) {
			return null;
		}
		
		int downloaded = (int)(System.currentTimeMillis() / 1000.0);
		
		result.setId(id);
		result.setNamespace(0);
		result.setDownloaded(downloaded);
		
		result.setLabels(parseMapWithLocalizedString(entity.get("labels").getAsJsonObject()));
		result.setDescriptions(parseMapWithLocalizedString(entity.get("descriptions").getAsJsonObject()));
		result.setAliases(parseMapWithLocalizedStrings(entity.get("aliases").getAsJsonObject()));
		result.setSitelinks(parseSitelinks(entity.get("sitelinks").getAsJsonObject()));
		result.setProperties(parseProperties(entity.get("claims").getAsJsonObject()));
		
		return result;
	}
	
	private Map<Language, Set<LocalizedText>> parseMapWithLocalizedStrings(JsonObject map) {
		Map<Language, Set<LocalizedText>> result = new HashMap<>();
		
		map.entrySet().forEach(e -> {
			JsonArray values = e.getValue().getAsJsonArray();
			for (int i = 0; i < values.size(); i++) {
				JsonObject value = values.get(i).getAsJsonObject();
				
				try {
					Language language = Language.valueOf(value.get("language").getAsString().toUpperCase());
					result.putIfAbsent(language, new HashSet<>());
					
					
					result.get(language).add(
						new LocalizedText(
							value.get("value").getAsString(), 
							language
						)
					);
				} catch (IllegalArgumentException ex) {
					// Skip unsupported languages
				}
			}
			
		});

		return result;
	}
	
	private Map<String, List<WikidataPropertyValue>> parseProperties(JsonObject map) {
		Map<String, List<WikidataPropertyValue>> results = new HashMap<>();
		
		map.entrySet().forEach(e -> {
			JsonArray values = e.getValue().getAsJsonArray();
			
			values.forEach(value -> {
				try {
					JsonObject mainsnak = 
						value.getAsJsonObject().get("mainsnak").getAsJsonObject();
					
					if (mainsnak.get("snaktype").getAsString().equals("value")) {
						JsonObject datavalue = mainsnak.get("datavalue").getAsJsonObject();
						String datatype = datavalue.get("type").getAsString();
						WikidataPropertyType type = WikidataPropertyType.fromString(datatype);
						
						if (type == null) return;

						JsonElement jsonElement = datavalue.get("value");
						
						
						
						if (jsonElement.isJsonObject()) {
							if (type == WikidataPropertyType.ENTITY_ID) {
								String entityType = jsonElement.getAsJsonObject().get("entity-type").getAsString();
								if (entityType.equals("item")) {
									String id = jsonElement.getAsJsonObject().get("id").getAsString();
									results.putIfAbsent(e.getKey(), new ArrayList<>());
									results.get(e.getKey()).add(new WikidataPropertyValue(id, type));
								}
							} else if (type == WikidataPropertyType.STRING) {
								String stringValue = jsonElement.getAsJsonObject().get("value").getAsString();
								results.putIfAbsent(e.getKey(), new ArrayList<>());
								results.get(e.getKey()).add(new WikidataPropertyValue(stringValue, type));
							} else if (type == WikidataPropertyType.TIME) {
								String stringValue = jsonElement.getAsJsonObject().get("time").getAsString();
								
								boolean negativeYear = false;
								if (stringValue.startsWith("-")) {
									negativeYear = true;
									stringValue = stringValue.substring(1);
								}
								
								stringValue = stringValue.split("-")[0].replace("+", ""); // Extract only year from date
								results.putIfAbsent(e.getKey(), new ArrayList<>());

								if (negativeYear)
									stringValue = stringValue.replaceAll("^0+", "") + " BC";
								
								results.get(e.getKey()).add(new WikidataPropertyValue(stringValue, type));
							} else if (type == WikidataPropertyType.COORD) {
								JsonObject coordValues = jsonElement.getAsJsonObject();
								double lat = coordValues.get("latitude").getAsDouble();
								double lon = coordValues.get("longitude").getAsDouble();
								results.putIfAbsent(e.getKey(), new ArrayList<>());
								results.get(e.getKey()).add(new WikidataPropertyValue(lat + "," + lon, type));
							} else if (type == WikidataPropertyType.QUANTITY) {
								String amount = jsonElement.getAsJsonObject().get("amount").getAsString();
								results.putIfAbsent(e.getKey(), new ArrayList<>());
								results.get(e.getKey()).add(new WikidataPropertyValue(amount, type));
							}
						} else {
							String realValue = jsonElement.getAsString();
							results.putIfAbsent(e.getKey(), new ArrayList<>());
							results.get(e.getKey()).add(new WikidataPropertyValue(realValue, type));
						}
						
					}
				} catch (JsonParseException | IllegalStateException | NullPointerException ex) {
					// Skip
//					System.err.println("EXCEEPEP");
					System.err.println(e.getKey());
					ex.printStackTrace();
				}
			});
		});
		
		return results;
	}
	
	private Map<Language, String> parseSitelinks(JsonObject map) {
		Map<Language, String> result = new HashMap<>();
		
		map.entrySet().forEach(e -> {
			String key = e.getKey();
			if (!key.endsWith("wiki")) return;
			
			try {
				Language language = Language.valueOf(e.getKey().toUpperCase().replace("WIKI", ""));
				
				result.put(
					language, 
					e.getValue().getAsJsonObject().get("title").getAsString()
				);
			} catch (IllegalArgumentException ex) {
				// Skip unsupported languages
			}
		});
		return result;
	}
	
	private Map<Language, LocalizedText> parseMapWithLocalizedString(JsonObject map) {
		Map<Language, LocalizedText> result = new HashMap<>();
		
		map.entrySet().forEach(e -> {
			try {
				Language language = Language.valueOf(e.getKey().toUpperCase());
				result.put(
					language, 
					new LocalizedText(e.getValue().getAsJsonObject().get("value").getAsString(), language)
				);
			} catch (IllegalArgumentException ex) {
				// Skip unsupported languages
			}
		});
		
		return result;
	}
	
	public List<WikidataItem> download(Set<String> ids) throws IOException {
		List<WikidataItem> results = new ArrayList<>();
		
		Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.SEVERE);
		String json = Request.Get(API_URL + URLEncoder.encode(String.join("|", ids), "UTF-8")).execute().returnContent().asString();
		
		JsonObject obj = gson.fromJson(json, JsonObject.class);
		
		try {
			JsonObject entities = 
				obj.get("entities").getAsJsonObject();
			
			entities.entrySet().forEach(e -> {
				WikidataItem item = parseEntity(e.getValue().getAsJsonObject());
				if (item != null) results.add(item);
			});
		} catch (JsonParseException | IllegalStateException | NullPointerException e) {
			e.printStackTrace();
		}

		return results;
	}
}
