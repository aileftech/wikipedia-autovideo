package com.autovideo.wikidata.datatypes;

import java.util.logging.Logger;

/**
 * The target type of a Wikidata property, e.g. String, URL, item, etc...
 *
 */
public enum WikidataPropertyType {
	ENTITY_ID, STRING, TIME, COORD, QUANTITY;
	
	private static final Logger logger = Logger.getLogger(WikidataPropertyType.class.getSimpleName());
	
	public static WikidataPropertyType fromString(String name) {
		switch (name) {
			case "wikibase-entityid":
				return WikidataPropertyType.ENTITY_ID;
			case "string":
				return WikidataPropertyType.STRING;
			case "time":
				return WikidataPropertyType.TIME;
			case "globecoordinate":
				return WikidataPropertyType.COORD;
			case "quantity":
				return WikidataPropertyType.QUANTITY;
			default:
				logger.fine("Unrecognized type " + name);
				return null;
		}
	}
}
