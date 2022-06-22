package com.autovideo.wikidata.datatypes;

import java.nio.ByteBuffer;

/**
 * A triple containing the starting node, the propertyId and the target node.
 */
public class WikidataEdge {
	private String startNode;
	private String targetNode;
	private String propertyId;

	public WikidataEdge(String startNode, String targetNode, String propertyId) {
		this.startNode = startNode;
		this.targetNode = targetNode;
		this.propertyId = propertyId;
	}

	public WikidataEdge(int startNode, int targetNode, int propertyId) {
		this.startNode = "Q" + startNode;
		this.targetNode = "Q" + targetNode;
		this.propertyId = "P" + propertyId;
	}
	
	public String getStartNode() {
		return startNode;
	}

	public String getTargetNode() {
		return targetNode;
	}

	public String getPropertyId() {
		return propertyId;
	}
	
	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(12);

		buffer.putInt(Integer.parseInt(startNode.replace("Q", "")));
		buffer.putInt(Integer.parseInt(propertyId.replace("P", "")));
		buffer.putInt(Integer.parseInt(targetNode.replace("Q", "")));
		
		return buffer.array();
	}

	@Override
	public String toString() {
		return "WikidataEdge [startNode=" + startNode + ", targetNode=" + targetNode + ", propertyId=" + propertyId
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyId == null) ? 0 : propertyId.hashCode());
		result = prime * result + ((startNode == null) ? 0 : startNode.hashCode());
		result = prime * result + ((targetNode == null) ? 0 : targetNode.hashCode());
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
		WikidataEdge other = (WikidataEdge) obj;
		if (propertyId == null) {
			if (other.propertyId != null)
				return false;
		} else if (!propertyId.equals(other.propertyId))
			return false;
		if (startNode == null) {
			if (other.startNode != null)
				return false;
		} else if (!startNode.equals(other.startNode))
			return false;
		if (targetNode == null) {
			if (other.targetNode != null)
				return false;
		} else if (!targetNode.equals(other.targetNode))
			return false;
		return true;
	}
	
}
