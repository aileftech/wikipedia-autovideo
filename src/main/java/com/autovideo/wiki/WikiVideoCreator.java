package com.autovideo.wiki;

import java.awt.FontFormatException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.client.fluent.Request;

import com.amazonaws.services.polly.model.Engine;
import com.autovideo.img.ImageCategory;
import com.autovideo.img.ImageVideoSelector;
import com.autovideo.img.ImgUtils;
import com.autovideo.img.VideoImage;
import com.autovideo.utils.Language;
import com.autovideo.utils.ProcessRunner;
import com.autovideo.utils.Utils;
import com.autovideo.video.SectionOverlay;
import com.autovideo.video.VideoRenderer;
import com.autovideo.wikidata.datatypes.WikidataItem;
import com.autovideo.wikidata.downloaders.WikidataItemDownloader;
import com.autovideo.wikipedia.datatypes.WikipediaItem;
import com.autovideo.wikipedia.downloaders.WikipediaItemDownloader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WikiVideoCreator {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	private static final WikidataItemDownloader wd = new WikidataItemDownloader();
	
	private static final WikiSectionSplitter splitter = new WikiSectionSplitter();
	
	private static final WikiSectionAggregator agg = new WikiSectionAggregator();
	
	private static final WikiVideoAggregator videoAggregator = new WikiVideoAggregator();
	
	private boolean withAudio = false;
	
	private Engine engine;
	
	private ImageCategory category;
	
	public WikiVideoCreator(boolean withAudio, Engine engine, ImageCategory category) {
		this.withAudio = withAudio;
		this.engine = engine;
		this.category = category;
	}

	private void normalizeContent(WikipediaItem wikipediaPage) {
		StringBuilder buffer = new StringBuilder();

		String content = wikipediaPage.getContent();
		
		int parenthesisCounter = 0;

		for (char c : content.toCharArray()) {
		    if (c == '(' || c == '{' )
		        parenthesisCounter++;
		    if (c == ')' || c == '}' )
		        parenthesisCounter--;
		    if (!(c == '(' || c == '{' || c == ')' || c == '}') && parenthesisCounter == 0)
		        buffer.append(c);
		}
		
		wikipediaPage.setContent(buffer.toString().replaceAll("\\.([A-Z\"])", ". $1"));
	}
	
	private SectionOverlay createOverlayFromKeyFacts(WikidataItem item, WikipediaItem page) {
		SectionOverlay o = new SectionOverlay("KEY INFO ABOUT " + page.getTitle() + "              ");
		
		if (item.getProperties().get("P571") != null) {
			String inception = item.getProperties().get("P571").get(0).getValue();
			o.addBulletPoint("founded in " + inception);
		}
		
		if (item.getProperties().get("P112") != null) {
			List<String> foundersList = item.getProperties().get("P112").stream().map(p -> {
				try {
					return new WikidataItemDownloader().download(Collections.singleton(p.getValue()))
						.get(0).getLabels().get(Language.EN).getText();
				} catch (IOException e) {
					return null;
				}
			})
			.filter(x -> x != null)
			.collect(Collectors.toList());

			String founders = String.join(
				foundersList.size() == 2 ? " and " : ", ", 
				foundersList
			);
			o.addBulletPoint("founded by " + founders);
		}
		
		if (item.getProperties().get("P2044") != null) {
			String elevation = item.getProperties().get("P2044").get(0).getValue();
			o.addBulletPoint("elevation\\: " + elevation.replace("+", "") + "m");
		}
		return o;
	}
	
	public void createVideo(String pageTitle, Language language) throws IOException, InterruptedException {
		WikipediaItemDownloader w = 
				new WikipediaItemDownloader.Builder(language)
					.setGetHtml(true)
					.setAcceptIds(false).build();

		WikipediaItem wikipediaPage = w.request(pageTitle);
		
		if (wikipediaPage == null) {
			System.out.println("[ERROR] Can't download Wikipedia page " + pageTitle);
			return;
		}
		
		String flagImage = null;
		float latitude = 0, longitude = 0;
		SectionOverlay sOverlay = null;
		if (wikipediaPage.getWikidataId() != null) {
			List<WikidataItem> download = wd.download(Collections.singleton(wikipediaPage.getWikidataId()));
			
			if (!download.isEmpty()) {
				try {
					flagImage = download.get(0).getProperties().get("P41").get(0).getValue();
				} catch (Exception e) {
					// No flag image
				}
				
				try {
					String coordinates = download.get(0).getProperties().get("P625").get(0).getValue();
					latitude = Float.valueOf(coordinates.split(",")[0]);
					longitude = Float.valueOf(coordinates.split(",")[1]);
				} catch (Exception e) {
					
				}
				
				sOverlay = createOverlayFromKeyFacts(download.get(0), wikipediaPage);
			}
		}
		
		normalizeContent(wikipediaPage);
		
		List<WikiSection> sections = splitter.split(wikipediaPage.getContent(), pageTitle);
		
		new ImageVideoSelector(category).retrieve(pageTitle, sections, 9, wikipediaPage);
		
		List<WikiVideo> videos = agg.createVideos(sections, wikipediaPage);
		
		videos = videoAggregator.aggregateVideos(videos);
		
		Files.createDirectories(Paths.get("tmp"));
		Files.createDirectories(Paths.get("output/final/"));

		PrintWriter out = new PrintWriter("output/final/" + pageTitle + ".html");
		for (WikiVideo video : videos) {
			out.println("<h1>Video: " + video.getTitle() + " (estimated length: "  + video.getEstimatedLength() + " minutes)</h1>");
			for (WikiSection s : video.getSections()) {
				out.println("<h2>" + s.getTitle() + "</h2>");
				s.getImages().forEach(img -> {
					out.println("<img width=\"300\" src=\"" + img.getPreviewURL() + "\">");
					if (img.getCaption() != null) {
						out.println("<span>" + img.getCaption() + "</span>");
					}
				});
				
				s.getVideos().forEach(pVideo -> {
					out.println("<img width=\"300\" src=\"" + pVideo.getPreviewURL() + "\"> (VIDEO)");
				});
				out.println("<br/>");
				out.println(s.getContent());
			}
		}
		out.close();
		
		ProcessRunner.run("sh", "output/clean.sh");
		
		WikiSection introSection = 
			videos.get(0).getSections().stream().filter(s -> s.getTitle().equals("Intro")).findFirst().orElse(null);
		
		if (sOverlay != null && sOverlay.getBulletPoints().size() > 0) {
			introSection.setSectionOverlay(sOverlay);
		}
		
		if (introSection != null && flagImage != null) {
			byte[] bytes = Request.Get(Utils.getUrlForWikipediaImage(flagImage)).execute().returnContent().asBytes();
			String[] parts = flagImage.split("\\.");
			String ext = parts[parts.length - 1];
			
			Files.write(Paths.get("tmp/overlay." + ext), bytes);
			
			if (ext.equals("svg")) {
				System.out.println("Converting flag SVG to JPG...");
				ImgUtils.convert("tmp/overlay." + ext, "tmp/overlay.jpg");
				ImgUtils.createCaptionedImage("tmp/overlay.jpg", "Flag of " + pageTitle, true);
				ext = "jpg";
			}
			introSection.setOverlay("tmp/overlay." + ext);
		}

		for (int i = 0; i < videos.size(); i++) {
			WikiVideo video = videos.get(i);
			String outputFileName = video.getPageTitle() + "_" + i + "_-_" + video.getTitle().replace(" ", "_");
			
			VideoImage image = video.sampleRandomImage();
			if (image == null) continue;
			
			System.out.println(video.getTitle());
			video.getSections().forEach(s -> {
				System.out.println("\t" + s.getTitle() + "\t" + (s.getVideos().size() != 0));
			});
			
			VideoRenderer sv = new VideoRenderer(withAudio, engine, language);
			sv.render(video, outputFileName);
			
			byte[] img = Request.Get(image.getLargeImageURL()).execute().returnContent().asBytes();
			Files.write(Paths.get("output/final/" + outputFileName + ".jpg"), img);
			
			ProcessRunner.run("sh", "output/clean.sh");
		}
	}
	
	private static void usage() {
		System.out.println("Usage:\n");
		System.out.println("java -jar autovideo.jar <wikipedia_page_name> [engine]");
		System.out.println("\n* Use an English Wikipedia page name as a first argument, with underscores instead of spaces");
		System.out.println("* The optional engine parameter determines which engine to use for speech syntesis:");
		System.out.println("  the choices are 'standard' (default) and 'neural' (better but more expensive)");
	}

	public static void main(String[] args) throws IOException, InterruptedException, FontFormatException {
		Logger[] logs = new Logger[]{ Logger.getLogger("org.jaudiotagger") };

	    for (Logger l : logs)
	        l.setLevel(Level.OFF);
	    
	    if (args.length == 0) {
	    	usage();
	    	System.exit(0);
	    }
	    
	    String page = args[0];
	    Engine engine = Engine.Standard;
	    if (args.length >= 2) {
	    	String engineArg = args[1];
	    	try {
	    		engine = Engine.valueOf(engineArg);
	    	} catch (IllegalArgumentException e) {
	    		engine = Engine.Standard;
	    	}
	    }
	    
	    System.out.println("Starting video creation for Wikipedia page " + page + " and audio engine " + engine);
	    
//	    new WikiVideoCreator(true, Engine.Neural, ImageCategory.BUILDINGS).createVideo("Nice", Language.EN);
	    new WikiVideoCreator(true, engine, ImageCategory.BUILDINGS).createVideo(page, Language.EN);
//	    new WikiVideoCreator(true, Engine.Neural, ImageCategory.BUILDINGS).createVideo("Marseille", Language.EN);
//	    new WikiVideoCreator(true, Engine.Neural, ImageCategory.BUILDINGS).createVideo("Bordeaux", Language.EN);
	}
}
