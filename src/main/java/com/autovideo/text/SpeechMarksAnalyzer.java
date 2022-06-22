package com.autovideo.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.autovideo.utils.Language;
import com.autovideo.video.EntityOverlay;
import com.autovideo.wikipedia.datatypes.WikipediaItem;
import com.autovideo.wikipedia.downloaders.WikipediaItemDownloader;
import com.google.gson.Gson;

public class SpeechMarksAnalyzer {
	private static final Gson gson = new Gson();
	
	public static class SpeechMark {
		private String type;
		
		private String value;
		
		private int start, end;
		
		private int time;

		public String getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public int getTime() {
			return time;
		}

		@Override
		public String toString() {
			return "SpeechMark [type=" + type + ", value=" + value + ", start=" + start + ", end=" + end + ", time="
					+ time + "]";
		}
	}
	
	public List<EntityOverlay> analyze(String speechMarksFile, WikipediaItem page) throws IOException {
		List<EntityOverlay> results = new ArrayList<>();
		
		List<SpeechMark> marks = new ArrayList<>();
		Files.lines(Paths.get(speechMarksFile)).forEach(line -> {
			marks.add(gson.fromJson(line, SpeechMark.class));
		});
		
		page.getLinks().forEach(link -> {
			String linkedEntity = link.getTitle();
			if (linkedEntity.equals(page.getTitle())) return;
			
			results.addAll(findEntity(linkedEntity, marks));
		});
		
		Collections.sort(results, (a, b) -> (int)(a.getStartTime() - b.getStartTime()));
		
		/*
		 * Remove results too close to each other
		 */
		List<EntityOverlay> finalResults = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			if (finalResults.isEmpty())
				finalResults.add(results.get(i));
			else {
				
				EntityOverlay lastOverlay = finalResults.get(finalResults.size() - 1);
				if (results.get(i).getStartTime() < lastOverlay.getEndTime()) {
					continue;
				} else {
					finalResults.add(results.get(i));
				}
			}
		}
		
		return finalResults;
	}
	
	private List<EntityOverlay> findEntity(String entityName, List<SpeechMark> marks) {
		List<EntityOverlay> entities = new ArrayList<>();
		
		String[] tokens = entityName.split(" ");
		String normalizedEntityName = entityName.replace(" ", "").toLowerCase();
		
		for (int i = 0; i < marks.size() - tokens.length; i++) {
			String entity = marks.subList(i, i + tokens.length).stream().map(x -> x.value.toLowerCase()).collect(Collectors.joining());
			if (entity.equals(normalizedEntityName)) {
				double startTime = marks.get(i).time / 1000.0;
				entities.add(new EntityOverlay(entity, startTime - 0.5, startTime + 2.5, entityName));
			}
		}
		return entities;
	}
	
	public static void main(String[] args) throws IOException {
		SpeechMarksAnalyzer s = new SpeechMarksAnalyzer();
		
		WikipediaItemDownloader wp = new WikipediaItemDownloader.Builder(Language.EN)
			.setAcceptIds(false)
			.setGetHtml(true).build();
		
		WikipediaItem request = wp.request("Rome");
		s.analyze("tmp/speech.json", request);
	}
	
	
}

