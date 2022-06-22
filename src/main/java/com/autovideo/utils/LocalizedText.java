package com.autovideo.utils;

/**
 * A text with its language.
 */
public class LocalizedText {
	private String text;
	
	private Language language;
	
	public static final LocalizedText NA = new LocalizedText("N/A", Language.EN);

	public LocalizedText(String text, Language language) {
		this.text = text;
		this.language = language;
	}

	public String getText() {
		return text;
	}

	public Language getLanguage() {
		return language;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		LocalizedText other = (LocalizedText) obj;
		if (language != other.language)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + language.toString().toLowerCase() + ") " + text;
	}
	
}
