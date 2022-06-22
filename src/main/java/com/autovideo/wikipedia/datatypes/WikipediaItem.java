package com.autovideo.wikipedia.datatypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.autovideo.utils.Language;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class WikipediaItem {
	
	private static final Gson gson = new Gson();
	
	@SerializedName("_id")
	private WikipediaItemID id;
	
	private Set<WikipediaItemID> categories = new HashSet<>();
	
	private Set<String> redirects = new HashSet<>();
	
	private Set<WikipediaItemID> links = new HashSet<>();
	
	/**
	 * The plain text content as returned by Wikipedia APIs.
	 * Usually still needs normalization to remove noise.
	 */
	private String content;
	
	/**
	 * The wikitext as returned by the Wikipedia APIs.
	 */
	private String wikitext;

	private String htmlContent;
	
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	
	public String getHtmlContent() {
		return htmlContent;
	}
	
	public WikipediaItem(WikipediaItemID id) {
		this.id = id;
	}
	
	public boolean isValid() {
		return  id.getTitle() != null && !id.getTitle().isEmpty()
				&& (getWikitext() == null || !getWikitext().toLowerCase().trim().startsWith("#redirect"))
				&& getWikidataId() != null && !getWikidataId().isEmpty();
	}
	
	public Set<WikipediaItemID> getCategories() {
//		if (categories == null) categories = new HashSet<>();
		return categories;
	}

	public Set<String> getRedirects() {
		return redirects;
	}

	public String getTitle() {
		return id.getTitle();
	}

	public Language getLanguage() {
		return id.getLanguage();
	}

	public Set<WikipediaItemID> getLinks() {
		return links;
	}

	/**
	 * Returns the content of the Wikipedia page stripped of its Wiki markup
	 * @return
	 */
	public String getContent() {
		return content;
	}

	public void setLinks(Set<WikipediaItemID> links) {
		this.links = links;
	}

	public void setRedirects(Set<String> redirects) {
		this.redirects = redirects;
	}

	public void setCategories(Set<WikipediaItemID> categories) {
		this.categories = categories;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getWikidataId() {
		return id.getWikidataId();
	}

	public WikipediaItemID getId() {
		return id;
	}

	public void addLinks(Collection<WikipediaItemID> links) {
		this.links.addAll(links);
	}

	public void addCategories(Collection<WikipediaItemID> categories) {
		this.categories.addAll(categories);
	}

	public void setWikitext(String wikitext) {
		this.wikitext = wikitext;
	}
	
	public String getWikitext() {
		return wikitext;
	}
	
	public void addRedirects(Collection<String> redirects) {
		this.redirects.addAll(redirects);
	}
	
	public static WikipediaItem fromJson(String json) {
		return gson.fromJson(json, WikipediaItem.class);
	}
	
	public String toJson() {
		return gson.toJson(this);
	}

	public int getIntegerKey() {
		return id.getNumericId();
	}

	public String getNormalizedTitle() {
		return id.getTitle().replaceAll("\\(.+?\\)", "");
	}
}
