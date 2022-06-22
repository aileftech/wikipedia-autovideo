package com.autovideo.wikidata.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.autovideo.utils.Language;
import com.autovideo.utils.LocalizedText;
import com.autovideo.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class WikidataItem {
	private static final Gson gson = new Gson();
	
	private Map<Language, LocalizedText> labels = new HashMap<>();
	
	private Map<Language, LocalizedText> descriptions = new HashMap<>();
	
	private Map<Language, Set<LocalizedText>> aliases = new HashMap<>();
	
	private Map<Language, String> sitelinks = new HashMap<>();
	
	private Map<String, List<WikidataPropertyValue>> properties = new HashMap<>();
	
	@SerializedName("_id")
	private String id;

	private Integer downloaded;
	
	private String type;
	
	private int namespace;

	
	public Map<Language, LocalizedText> getLabels() {
		return labels == null ? new HashMap<>() : labels;
	}

	public Map<Language, LocalizedText> getDescriptions() {
		return descriptions == null ? new HashMap<>() : descriptions;
	}

	public Map<Language, Set<LocalizedText>> getAliases() {
		return aliases == null ? new HashMap<>() : aliases;
	}

	public Map<Language, String> getSitelinks() {
		return sitelinks == null ? new HashMap<>() : sitelinks;
	}

	/**
	 * Helper method which will return a URL from Wikidata property P18,
	 * if present, which is the name of an image on the Wikimedia servers
	 * @return
	 */
	public String getImageUrl() {
		List<WikidataPropertyValue> list = properties.get("P18");
		if (list == null || list.isEmpty()) {
			list = properties.get("P154");
		}
		if (list == null || list.isEmpty()) return null;
		
		WikidataPropertyValue wikidataPropertyValue = list.get(0);
		return Utils.getThumbUrlForWikipediaImage(wikidataPropertyValue.getValue());
	}
	
	public String getFullImageUrl() {
		List<WikidataPropertyValue> list = properties.get("P18");
		if (list == null || list.isEmpty()) {
			list = properties.get("P154");
		}
		if (list == null || list.isEmpty()) return null;
		
		WikidataPropertyValue wikidataPropertyValue = list.get(0);
		return Utils.getUrlForWikipediaImage(wikidataPropertyValue.getValue());
	}

	public String getId() {
		return id;
	}

	public int getDownloaded() {
		return downloaded;
	}

	public String getType() {
		return type;
	}

	public int getNamespace() {
		return namespace;
	}

	public void setLabels(Map<Language, LocalizedText> labels) {
		this.labels = labels;
	}

	public void setDescriptions(Map<Language, LocalizedText> descriptions) {
		this.descriptions = descriptions;
	}

	public void setAliases(Map<Language, Set<LocalizedText>> aliases) {
		this.aliases = aliases;
	}

	public void setSitelinks(Map<Language, String> sitelinks) {
		this.sitelinks = sitelinks;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDownloaded(int downloaded) {
		this.downloaded = downloaded;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setNamespace(int namespace) {
		this.namespace = namespace;
	}
	
	
	
	public Map<String, List<WikidataPropertyValue>> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, List<WikidataPropertyValue>> properties) {
		this.properties = properties;
	}

	public String toJson() {
		return gson.toJson(this);
	}
	
	public static WikidataItem fromJson(String json) {
		return gson.fromJson(json, WikidataItem.class);
	}

	public int getIntegerKey() {
		return Integer.parseInt(id.replace("Q", ""));
	}

	public String getDescriptionOrDefault(Language language, String defaultValue) {
		LocalizedText localizedText = descriptions.get(language);
		if (localizedText == null) return defaultValue;
		return localizedText.getText();
	}
	
	public String getLabelOrDefault(Language language, String defaultValue) {
		LocalizedText localizedText = labels.get(language);
		if (localizedText == null) return defaultValue;
		return localizedText.getText();
	}
	
	public List<WikidataEdge> getOutgoingEdges(String targetNodeId) {
		List<WikidataEdge> results = new ArrayList<>();
		
		getProperties().forEach((propertyId, values) -> {
			values.forEach(value -> {
				if (value.getValue().equals(targetNodeId)) {
					results.add(new WikidataEdge(getId(), targetNodeId, propertyId));
				}
			});
		});
		
		return results;
	}
	
	@Override
	public String toString() {
		return "WikidataItem [id = " + getId() + ", mainLabel = " + getLabelOrDefault(Language.EN, "N/A") + "]";
	}

	/**
	 * Returns the list of edges that start from the given sourceNodeId
	 * and point to the node of this object.
	 * @param sourceNodeId
	 * @return
	 */
	public List<WikidataEdge> getIncomingEdges(WikidataItem startNode) {
		List<WikidataEdge> results = new ArrayList<>();
		
		startNode.getProperties().forEach((propertyId, values) -> {
			values.forEach(value -> {
				if (value.getValue().equals(getId())) {
					results.add(new WikidataEdge(startNode.getId(), getId(), propertyId));
				}
			});
		});
		
		return results;
	}

	public String getMainLabel() {
		return getLabelOrDefault(Language.EN, null);
	}
}
