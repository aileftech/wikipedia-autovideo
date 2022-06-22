package com.autovideo.utils;

/**
 * An interface for all objects that are uniquely identified with a String ID.
 * Mainly used for objects that have to be serialized into a RocksDB instance
 * where the ID is used as a key.
 *
 */
public interface ItemWithId {
	public String getItemId();
}
