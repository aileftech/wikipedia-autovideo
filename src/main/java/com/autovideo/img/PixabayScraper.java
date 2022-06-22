package com.autovideo.img;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;

import com.autovideo.AutovideoConf;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PixabayScraper {
	private String keyword;
	
	private ImageCategory category;
	
	private AutovideoConf conf = AutovideoConf.getInstance();
	
	private static final Set<String> BLACKLIST = new HashSet<String>() {
		private static final long serialVersionUID = 1L;

		{
			add("-phone");
			add("-leaves");
			add("-solar");
			add("-models");
		}
	};
	
	private List<String> scrapedUrls = new ArrayList<>();
	
	private static final String[] VIDEO_QUALITY = new String[] {
		"large", "medium"
	}; 
	
	public PixabayScraper(String keyword, ImageCategory category) {
		this.category = category;
		this.keyword = keyword;
	}
	
	public ImageCategory getCategory() {
		return category;
	}
	
	public List<PixabayVideo> getBestVideos() throws IOException {
		List<PixabayVideo> results = new ArrayList<>();
		
		String url = "https://pixabay.com/api/videos/?key=" + conf.getPixabayApiKey() + "&q=$QUERY$&category=buildings";
		
		String finalKeyword = keyword + " " + String.join(" ", BLACKLIST);
		String finalUrl = url.replace("$QUERY$", URLEncoder.encode(finalKeyword, "UTF-8"));
			
		try {
			String content = 
				Request.Get(finalUrl)
					.execute()
					.returnContent()
					.asString();
			
			JsonObject fromJson = new Gson().fromJson(content, JsonObject.class);
			if (fromJson.has("hits")) {
				JsonArray array = fromJson.get("hits").getAsJsonArray();
				
				array.forEach(element -> {
					JsonObject videosObj = element.getAsJsonObject().get("videos").getAsJsonObject();
					
					int duration = element.getAsJsonObject().get("duration").getAsInt();
					int likes = element.getAsJsonObject().get("likes").getAsInt();
					String id = element.getAsJsonObject().get("id").getAsInt() + "";
					String pictureId = element.getAsJsonObject().get("picture_id").getAsString();
					String tagsString = element.getAsJsonObject().get("tags").getAsString();
					
					Set<String> tags = Arrays.stream(tagsString.split(",")).map(x -> x.trim()).collect(Collectors.toSet());
					
					String previewURL = "https://i.vimeocdn.com/video/" + pictureId + "_295x166.jpg";
					
					for (String quality : VIDEO_QUALITY) {
						if (videosObj.has(quality)) {
							JsonObject videoQualityObj = videosObj.get(quality).getAsJsonObject();
							
							if (videoQualityObj.has("url")) {
								String videoUrl = videoQualityObj.get("url").getAsString();
								
								if (!videoUrl.isEmpty() && duration < 30) {
									results.add(new PixabayVideo(previewURL, videoUrl + "&download=1", id, likes, duration, tags));
									// First check for large (1920x1080), then medium (1080x720) which is available for all videos
									// So when the first one (which is the highest availble is found, we can break
									break;
								}
							}
						}
					}
					
				});
			}
		} catch (HttpResponseException e) {
			throw new RuntimeException(e);
		}
		
		Collections.sort(results, (a, b) -> {
			return b.getLikes() - a.getLikes();
		});
		
		return results;
	}
	
	public List<VideoImage> getBestImages() throws ClientProtocolException, IOException {
		String url = "https://pixabay.com/api/?key=" + conf.getPixabayApiKey() + "&q=$QUERY$&image_type=photo&min_width=800&category=buildings&per_page=100&page=";
		
		List<VideoImage> images = new ArrayList<>();
		
		for (int i = 1; i <= 1; i++) {
			String finalUrl = url.replace("$QUERY$", URLEncoder.encode(keyword + " " + String.join(" ", BLACKLIST), "UTF-8")) + i;
			
			try {
				String content = 
					Request.Get(finalUrl)
						.execute()
						.returnContent()
						.asString();
				
				JsonObject fromJson = new Gson().fromJson(content, JsonObject.class);
				if (fromJson.has("hits")) {
					JsonArray array = fromJson.get("hits").getAsJsonArray();
					
					array.forEach(element -> {
						String largeImageURL = element.getAsJsonObject().get("largeImageURL").getAsString();
						String previewURL = element.getAsJsonObject().get("previewURL").getAsString();
						String id = element.getAsJsonObject().get("id").getAsString();
						int likes = element.getAsJsonObject().get("likes").getAsInt();
						images.add(new VideoImage(previewURL, largeImageURL, id, likes));
					});
				}
			} catch (HttpResponseException e) {
				break;
			}
		}
		
		Collections.sort(images, (a, b) -> {
			return b.getLikes() - a.getLikes();
		});
		
		return images;
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public List<String> getScrapedUrls() {
		return scrapedUrls;
	}
}
