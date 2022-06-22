package com.autovideo.wiki;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.autovideo.text.Summarizer;
import com.autovideo.utils.Language;

public class WikiSectionSplitter {
	private static final Summarizer summ = new Summarizer();
	
	private static final Set<String> BLACKLIST = new HashSet<String>() {
		private static final long serialVersionUID = 1L;

		{
			add("see also");
			add("notes");
			add("references");
			add("bibliography");
			add("external links");
			add("further reading");
		}
	};
	
	private boolean isStartSection(String line) {
		return line.trim().startsWith("=== ");
	}
	
	private boolean isStartParentSection(String line) {
		return line.trim().startsWith("== ");
	}
	
	public List<WikiSection> split(String content, String pageTitle) {
		List<WikiSection> sections = new ArrayList<>();
		
		String[] lines = content.split("\\n");
		
		String currentSection = "Intro", currentText = "",	currentParent = "Intro";
		for (int i = 0; i < lines.length - 1; i++) {
			String line = lines[i];
			
			if (isStartSection(line)) {
				if (!BLACKLIST.contains(currentSection.toLowerCase()))
					sections.add(
						new WikiSection(
							currentSection, 
							currentText, 
							currentParent,
							pageTitle
						)
					);
				currentSection = line.replace("=", "").trim();
				currentText = "";
			} else if (isStartParentSection(line)) {
				if (!BLACKLIST.contains(currentSection.toLowerCase()))
					sections.add(
						new WikiSection(
							currentSection, 
							currentText, 
							currentParent,
							pageTitle
						)
					);
				currentParent = line.replace("=", "").trim();
				currentSection = currentParent;
				currentText = "";
			} else if (!line.trim().startsWith("==")) {
				currentText += lines[i].trim() + " ";
			}
			
			if (line.replace("=", "").trim().equalsIgnoreCase("references"))
				break;
		}
		
		sections.forEach(section -> {
			if (section.getContent().length() > 2800) {
				String summary = summ.summarize(section.getContent(), Language.EN, 2700, 0);
				section.setContent(summary);
			}
		});
		
		return sections;
	}
}
