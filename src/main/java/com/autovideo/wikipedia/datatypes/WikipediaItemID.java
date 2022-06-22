package com.autovideo.wikipedia.datatypes;

import com.autovideo.utils.Language;

/**
 * Represents identifying information for any given Wikipedia page.
 * Equals is implemented on this object on the (id, language) pair.
 *
 */
public class WikipediaItemID {
	
	/**
	 * The internal ID of the page on Wikipedia
	 */
	protected Integer id;
	
	/**
	 * The title of the page
	 */
	protected String title;
	
	/**
	 * Wikimedia namespace for the page (e.g. 0 = Article, 14 = Category)
	 */
	protected int namespace;

	/**
	 * The language of the Wikipedia site
	 */
	protected Language language;
	
	
	/**
	 * The Wikidata ID if available
	 */
	protected String wikidataId;


	public WikipediaItemID(Integer id, String title, int type, Language language, String wikidataId) {
		this.id = id;
		this.title = title;
		this.namespace = type;
		this.language = language;
		this.wikidataId = wikidataId;
	}


	public String getTitle() {
		return title;
	}


	public int getType() {
		return namespace;
	}


	public Language getLanguage() {
		return language;
	}

	public int getNumericId() {
		return id;
	}
	
	public String getWikidataId() {
		return wikidataId;
	}
	
	public void setWikidataId(String wikidataId) {
		this.wikidataId = wikidataId;
	}
	
	@Override
	public String toString() {
		return "[" + id + ", " + title + ", " + language + ", " + wikidataId + "]";
	}


	public int getNamespace() {
		return namespace;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		WikipediaItemID other = (WikipediaItemID) obj;
		if (language != other.language)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}


	public String getId() {
		return String.valueOf(getNumericId());
	}
}
