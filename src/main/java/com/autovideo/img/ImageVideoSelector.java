package com.autovideo.img;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.autovideo.utils.Utils;
import com.autovideo.wiki.WikiSection;
import com.autovideo.wikipedia.datatypes.WikipediaItem;

public class ImageVideoSelector {
	private ImageCategory category;
	
	public ImageVideoSelector(ImageCategory category) {
		this.category = category;
	}

	private List<VideoImage> imagesFromWikipedia(WikipediaItem item) {
		List<VideoImage> images = new ArrayList<>();
		
		String wikitext = item.getHtmlContent();
		
		if (wikitext == null)
			throw new RuntimeException("Unable to extract images from Wikipedia page " + item.getTitle() + " because html is null");
		
	    Document parsedHtml = Jsoup.parse(wikitext);
	    parsedHtml.select("div.thumb").forEach(div -> {
	    	Elements captionDiv = div.select(".thumbcaption");
	    	
	    	if (captionDiv.size() == 1) {
	    		Element previousSibling = div.previousElementSibling();
	    		
	    		while (previousSibling != null) {
	    			if (!previousSibling.select(".mw-headline").isEmpty())
	    				break;
	    			
	    			previousSibling = previousSibling.previousElementSibling();
	    		}
		    	
	    		if (previousSibling != null) {
					try {
						String image = URLDecoder.decode(div.select("a").first().attr("href").replace("/wiki/File:", ""), "UTF-8");
				    	String section = previousSibling.select(".mw-headline").text();
				    	
			    		VideoImage videoImage = new VideoImage(
			    			Utils.getThumbUrlForWikipediaImage(image), 
			    			Utils.getUrlForWikipediaImage(image), 
			    			image, 
			    			1000
			    		);
			    		videoImage.setSection(section);
			    		videoImage.setCaption(captionDiv.text().replaceAll("\\[[0-9]+\\]", ""));
			    		images.add(videoImage);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
	    		}
	    	}
	    });
	    
	    return images;
	}
	
	public void retrieve(String baseKeyword, List<WikiSection> sections, int numImagesPerSection, WikipediaItem item) throws IOException {
		Set<VideoImage> usedImages = new HashSet<>();
		
		List<VideoImage> wikipediaImages = imagesFromWikipedia(item);
		
		List<PixabayVideo> videos = new PixabayScraper(baseKeyword, category).getBestVideos();
		
		int videoIndex = 0;
		
		for (WikiSection section : sections) {
			if (section.getContent().trim().isEmpty()) continue;
			
			List<VideoImage> selectedImages = new ArrayList<>();
			
			List<VideoImage> sectionImages = 
				wikipediaImages.stream()
						.filter(i -> i.getSection().equals(section.getTitle()))
						.collect(Collectors.toList());
			
			if (!sectionImages.isEmpty()) {
				sectionImages.stream().limit(numImagesPerSection).forEach(image -> {
					if (!usedImages.contains(image)) {
						selectedImages.add(image);
						usedImages.add(image);
					}
				});
			}
			
			if (videoIndex < videos.size()) {
				section.addVideo(videos.get(videoIndex));
				videoIndex++;
			}
			
			if (selectedImages.size() >= numImagesPerSection)
				continue;
			
			String pixaSearch = baseKeyword;
			if (!section.getTitle().equals("Intro"))
				 pixaSearch += " " + section.getTitle();
			
			List<VideoImage> bestImages = new PixabayScraper(pixaSearch, category).getBestImages();
			
			if (bestImages.isEmpty()) {
				bestImages = new PixabayScraper(baseKeyword, category).getBestImages();
			}
			
			for (VideoImage img : bestImages) {
				if (!usedImages.contains(img)) {
					selectedImages.add(img);
					usedImages.add(img);
				}
				
				if (selectedImages.size() >= numImagesPerSection) break;
			}
			
			selectedImages.forEach(i -> {
				section.addImage(i);
			});
		}
	}
}
