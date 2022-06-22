package com.autovideo.wikipedia.downloaders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.autovideo.utils.Language;
import com.autovideo.wikipedia.datatypes.WikipediaItem;
import com.autovideo.wikipedia.datatypes.WikipediaItemID;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Download a set of Wikipedia pages through calls to the APIs
 *
 */
public class WikipediaItemDownloader {
	private static final Logger logger = Logger.getLogger(WikipediaItemDownloader.class.getName());
	
	private static final String WIKI_API = "http://$LANG$.wikipedia.org/w/api.php";
	
	private static final Gson gson = new Gson();
	
	private Language language;
	
	private HttpClient httpClient;
	
	/**
	 * Whether to interpret the input as Wikipedia page IDs or Wikipedia page titles.
	 * By default, accept IDs as input.
	 */
	private boolean acceptIds = true;
	
	private boolean getHtml = false;
	
	public static class Builder {
		private Language language;
		private boolean acceptIds = true;
		
		private boolean getHtml = false;
		
		public Builder(Language language) {
			this.language = language;
		}
		
		public Builder setAcceptIds(boolean acceptIds) {
			this.acceptIds = acceptIds;
			return this;
		}
		
		public Builder setGetHtml(boolean getHtml) {
			this.getHtml = getHtml;
			return this;
		}
		
		public WikipediaItemDownloader build() {
			return new WikipediaItemDownloader(this);
		}
		
	}
	
	private WikipediaItemDownloader(Builder builder) {
		this.language = builder.language;
		this.acceptIds = builder.acceptIds;
		this.getHtml = builder.getHtml;
		
		httpClient = HttpClients.custom()
	        .setDefaultRequestConfig(RequestConfig.custom()
	            .setCookieSpec(CookieSpecs.STANDARD).build())
	        .build();
	}
	
	public WikipediaItem request(String pageIdentifier) {
		List<WikipediaItem> request = request(Collections.singletonList(pageIdentifier));
		if (request.size() > 0)
			return request.get(0);
		else {
			return null;
		}
	}
	
	/**
	 * Downloads the batch of Wikipedia pages where the pageIdsParams
	 * is a pipe separated string (|) with  numeric IDs or title, 
	 * @param pageIdsParams
	 * @return
	 */
	public List<WikipediaItem> request(List<String> pageIdsParams) {
		Map<Integer, WikipediaItem> partialResults = new HashMap<>();
		
		URIBuilder uriBuilder;
		
		try {
			uriBuilder = new URIBuilder(getApiUrl());
			
			if (acceptIds)
				uriBuilder.setParameter("pageids", String.join("|", pageIdsParams));
			else
				uriBuilder.setParameter("titles", String.join("|", pageIdsParams));

			uriBuilder.setParameter("action", "query");
			uriBuilder.setParameter("prop", "extracts");
			uriBuilder.setParameter("format", "json");
			uriBuilder.setParameter("explaintext", "");
			uriBuilder.setParameter("continue", "");
			
			uriBuilder.setParameter("action", "query");
			uriBuilder.setParameter("prop", "redirects|langlinks|categories|links|images|revisions|extracts|pageprops");
			uriBuilder.setParameter("rvprop", "content");
			uriBuilder.setParameter("ppprop", "wikibase_item");
			uriBuilder.setParameter("lllimit", "500");
			uriBuilder.setParameter("pllimit", "500");
			uriBuilder.setParameter("imlimit", "500");
			uriBuilder.setParameter("rdlimit", "500");
			
			String requestUrl = uriBuilder.build().toString();
			System.out.println(requestUrl);
			try {
				HttpEntity entity = httpClient.execute(new HttpGet(requestUrl)).getEntity();
				String jsonResult = EntityUtils.toString(entity);
				
				JsonObject pageObj = gson.fromJson(jsonResult, JsonObject.class);
				boolean hasContinuation = false;
				do {
					if (pageObj.has("error")) {
						logger.warning("Error requesting Wikipedia page batch. Returning partial results.");
						return new ArrayList<>(partialResults.values());
					}
					
					// Parse results of query
					if (pageObj.has("query")) {
						JsonObject allPages;
						try {
							allPages = pageObj.get("query").getAsJsonObject().get("pages").getAsJsonObject();
						} catch (IllegalStateException e) {
							allPages = new JsonObject();
						}
						
						// Iterate all pages in the result set
						for (Map.Entry<String, JsonElement> entry : allPages.entrySet()) {
							JsonObject page = entry.getValue().getAsJsonObject();
							
							int id;
							int type;
							String title;
							String wikidataId = null;

							if (page.has("missing")) {
								continue;
							} else {
								id = page.get("pageid").getAsInt();
								title = page.get("title").getAsString();
								type = page.get("ns").getAsInt();
								
								if (page.has("pageprops") && wikidataId == null) {
									JsonObject wikidataItem = page.get("pageprops").getAsJsonObject();
									wikidataId = wikidataItem.get("wikibase_item").getAsString();
								}
							}
							
							// Populate redirects if any
							Set<String> redirects = new HashSet<String>();
							if (page.has("redirects")) {
								JsonArray redirectList = page.get("redirects").getAsJsonArray();
								
								for (int i = 0; i < redirectList.size(); i++) {
									JsonObject element = redirectList.get(i).getAsJsonObject();
									
									redirects.add(
										element.get("title").getAsString()
									);
								}
							}
							
							// Populate categories
							List<WikipediaItemID> categories = new ArrayList<WikipediaItemID>();
							if (page.has("categories")) {
								JsonArray categoryList = page.get("categories").getAsJsonArray();
								
								for (int i = 0; i < categoryList.size(); i++) {
									JsonObject element = categoryList.get(i).getAsJsonObject();
									
									categories.add(
											new WikipediaItemID(
												null,
												element.get("title").getAsString(), 
												element.get("ns").getAsInt(), 
												language, 
												null
											)
									);
								}
							}

							// Populate links if any
							List<WikipediaItemID> links = new ArrayList<WikipediaItemID>();
							if (page.has("links")) {
								JsonArray linkList = page.get("links").getAsJsonArray();
								
								for (int i = 0; i < linkList.size(); i++) {
									JsonObject element = linkList.get(i).getAsJsonObject();
									
									links.add(
										new WikipediaItemID(
											null,
											element.get("title").getAsString(), 
											element.get("ns").getAsInt(), 
											language, 
											null
										)
									);
								}
							}

							String wikitext = null;
							if (page.has("revisions")) {
								JsonArray revisionsList = page.get("revisions").getAsJsonArray();
								
								try {
									JsonObject jsonElement = revisionsList.get(0).getAsJsonObject();
									wikitext = jsonElement.get("*").getAsString();
								} catch (Exception e) {
									System.err.println("Unable to find revisions for page " + title);
									continue;
								}
							}
							
							String pageContent = null;
							if (page.has("extract")) pageContent = page.get("extract").getAsString();
							
							WikipediaItemID wikipediaPageID = new WikipediaItemID(id, title, type, language, wikidataId);
							WikipediaItem currentPage;
							
							if (partialResults.containsKey(id)) {
								currentPage = partialResults.get(id);
								
								if (wikidataId != null) {
									currentPage.getId().setWikidataId(wikidataId);
								}
								
								if (wikitext != null && currentPage.getWikitext() == null) {
									currentPage.setWikitext(wikitext);
								}
								
								if (currentPage.getContent() == null && pageContent != null) {
									currentPage.setContent(pageContent);
								}
							} else {
								currentPage = new WikipediaItem(wikipediaPageID);
								currentPage.setContent(pageContent);
								currentPage.setWikitext(wikitext);
								partialResults.put(id, currentPage);
							}
							
							currentPage.addCategories(categories);
							currentPage.addLinks(links);
							currentPage.addRedirects(redirects);
						}
					}
					
					// Checks if we have to make more requests to get
					// all the content, otherwise exit the loop
					hasContinuation = pageObj.has("continue");
					if (hasContinuation) {
						JsonObject continueParams = pageObj.get("continue").getAsJsonObject();

						for (Map.Entry<String, JsonElement> param : continueParams.entrySet()) {
							uriBuilder.setParameter(param.getKey(), param.getValue().getAsString());
						}

						requestUrl = uriBuilder.build().toString();
						
						entity = httpClient.execute(new HttpGet(requestUrl)).getEntity();
						jsonResult = EntityUtils.toString(entity);
						
						pageObj = gson.fromJson(jsonResult, JsonObject.class);
					}
				} while (hasContinuation);
			} catch (IOException e) {
				return partialResults.values().stream().collect(Collectors.toList());
			}

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		List<WikipediaItem> collect = partialResults.values().stream()
				.filter(WikipediaItem::isValid)
				.collect(Collectors.toList());
		
		if (this.getHtml) {
			collect.forEach(c -> {
				System.out.println(c.getTitle());
				try {
					String json = Request.Get("https://" + language + ".wikipedia.org/w/api.php?action=parse&page=" + URLEncoder.encode(c.getTitle(), "UTF-8") + "&prop=text&formatversion=2&format=json")
						.execute().returnContent().asString();
					JsonObject fromJson = gson.fromJson(json, JsonObject.class);
					fromJson = fromJson.get("parse").getAsJsonObject();
					if (fromJson.has("text")) {
						c.setHtmlContent(fromJson.get("text").getAsString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		
		return collect;
	}

	public String getApiUrl() {
		return WIKI_API.replace("$LANG$", language.toString().toLowerCase());
	}
	
	public Language getLanguage() {
		return language;
	}

	public boolean getAcceptIds() {
		return acceptIds;
	}
}
