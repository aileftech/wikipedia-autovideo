package com.autovideo.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.http.client.fluent.Request;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import com.amazonaws.services.polly.model.Engine;
import com.autovideo.Polly;
import com.autovideo.img.ImgUtils;
import com.autovideo.img.PixabayVideo;
import com.autovideo.img.VideoImage;
import com.autovideo.utils.Language;
import com.autovideo.utils.ProcessRunner;
import com.autovideo.wiki.WikiSection;
import com.autovideo.wiki.WikiVideo;

public class VideoRenderer {
	private Polly polly;
	
	private boolean withAudio = false;
	
	public VideoRenderer(boolean withAudio, Engine engine, Language language) {
		this.withAudio = withAudio;
		polly = new Polly(engine, language);
	}

	/**
	 * Fit and pad filter
	 */
	private static final String SCALE_FILTER = "scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2";
	
//	/**
//	 * Crop and fit filter
//	 */
//	private static final String SCALE_FILTER = "scale=w=1280:h=720:force_original_aspect_ratio=2,crop=1280:720";
	
	
	private int getMp3Duration(String file) {
		int duration = 0;

		try {
			AudioFile audioFile = AudioFileIO.read(new File(file));
			duration = audioFile.getAudioHeader().getTrackLength();
			return duration;
		} catch (Exception e) {
			return -1;
		}
	}
	
	public void render(WikiVideo video, String outputFileName) {
		for (int i = 0; i < video.getSections().size(); i++) {
			try {
				createVideo(video.getSections().get(i), i, video);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		
		// Concat videos
		try {
			PrintWriter out2 = new PrintWriter("output/list.txt");
			boolean found = false;
			for (int i = 0; i <= video.getSections().size(); i++) {
				if (Files.exists(Paths.get("output/video.final." + i + ".mp4"))) {
					out2.println("file video.final." + i + ".mp4");
					found = true;
				}
			}
			out2.close();
			
			if (found)
				ProcessRunner.run("sh", "concat.sh", "output/list.txt", "output/final/" + outputFileName.replace(" ", "_") + ".mp4");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
		}
	}
	
	public void createVideo(WikiSection section, int sectionIndex, WikiVideo video) throws FileNotFoundException {
		if (section.getContent().length() < 40) {
			System.out.println("[INFO] Skipping too short section < 40 chars: " + section.getTitle());
			return;
		}
		
		/*
		 * Initial introduction
		 */
		if (sectionIndex == 0) {
			section.setContent("Hello! I am the story teller... and today we will talk about " + section.getPageTitle() + ". " + section.getContent());
		}
		
		/*
		 * Download videos
		 */
		PixabayVideo pixabayVideo = null;
		if (section.getVideos().size() > 0) {
			pixabayVideo = section.getVideos().get(0);
		}
		
		int videoDuration = 0;
		if (pixabayVideo != null) {
			try {
				System.out.println("Downloading video: " + pixabayVideo.getDownloadURL());
				byte[] asBytes = Request.Get(pixabayVideo.getDownloadURL()).execute().returnContent().asBytes();
				Files.write(Paths.get("tmp/0.mp4"), asBytes);
				videoDuration = pixabayVideo.getDuration();
			} catch (IOException e) {
			}
		}
		
		StringBuilder cmd = new StringBuilder();
		if (section.getContent().length() == 0) return;
		
		cmd.append("ffmpeg -y \\\n");
		
		System.out.println("Section: " + section.getTitle() + " (" + section.getContent().length() + " chars)");
		if (withAudio) {
			try {
				polly.writeToMp3(section.getContent(), "audio.mp3");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			System.out.println("Proceding without audio.");
		}
		
		int duration = getMp3Duration("audio.mp3");
		
		System.out.println("Audio duration: " + duration);
		
		if (section.getImages().size() == 0)
			// TODO Don't skip section without images
			return;
		
		double imgDuration = (1.0 * (duration - videoDuration) / section.getImages().size()) + 0.25;
		
		if (imgDuration <= 0) {
			pixabayVideo = null;
			imgDuration = (1.0 * (duration) / section.getImages().size()) + 0.25;
		}
		
		System.out.println("[INFO] Video duration: " + videoDuration);
		while (imgDuration < 6.5 && section.getImages().size() > 1) {
			System.out.println("[INFO] Image duration too short: removing least liked image:");
			List<VideoImage> images = section.getImages().stream().sorted((a, b) -> a.getLikes() - b.getLikes())
				.collect(Collectors.toList());
			List<VideoImage> subList = new ArrayList<>(images.subList(1, images.size()));
			System.out.println("\t" + section.getImages().size() + "\t" + images.size() + " => " + images.get(0));
			
			if (subList.size() == 1)
				break;
			
			section.getImages().clear();
			section.getImages().addAll(subList);
			imgDuration = (1.0 * (duration - videoDuration) / section.getImages().size()) + 0.25;
			
			System.out.println("\tNew imgDuration: " + imgDuration);
		}
		
		System.out.println("\t" + section.getImages().size() + " images");
		System.out.println("\t\t" + imgDuration + " seconds each");
		System.out.println("\t" + videoDuration + " seconds video");
		System.out.println("\tTotal = " + (videoDuration + imgDuration * section.getImages().size()));
		
		AtomicInteger progress = new AtomicInteger(0);

		if (section.hasOverlay()) cmd.append(" -i " + section.getOverlay() + "\\\n");

		int imagesFound = 0;
		for (VideoImage image : section.getImages()) {
			if (progress.incrementAndGet() % 1000 == 0) {
				System.out.println("Progress: " + progress.get());
			}
			try {
				byte[] asBytes = Request.Get(image.getLargeImageURL()).execute().returnContent().asBytes();
				Files.write(Paths.get("tmp/" + progress.get() + ".jpg"), asBytes);
				
				if (image.getCaption() != null) {
					System.out.println("Captioning image " + image.getLargeImageURL());
					ImgUtils.createCaptionedImage("tmp/" + progress.get() + ".jpg", image.getCaption());
				}
				
				cmd.append(" -loop 1 -t " + imgDuration + " -i tmp/" + progress.get() + ".jpg \\\n");
				imagesFound++;
			} catch (IOException e) {
				System.err.println("[ERROR] Error retrieving image: " + image.getLargeImageURL());
			}
		}
		
		if (imagesFound == 0 && pixabayVideo == null)
			return;
		else if (imagesFound != section.getImages().size())
			imgDuration = (1.0 * (duration - videoDuration) / section.getImages().size()) + 0.25;
		
		cmd.append(" -i audio.mp3 \\\n");
		
		if (pixabayVideo != null)
			cmd.append(" -i tmp/0.mp4 \\\n");
		
		
		int startIndex = section.hasOverlay() ? 1 : 0;

		cmd.append(" -filter_complex \\\n\"");
		
		for (int i = startIndex; i < section.getImages().size() + startIndex; i++) {
			String zoompan = "";
			if (section.getImages().get(i - startIndex).getCaption() == null) {
				double startZoom = 1.1 + Math.random()*0.2;
				double endZoom = 1.05 + Math.random()*0.1;
				double speed = 0.0002 + Math.random() * 0.0005;
				zoompan = ",scale=8000:-1,zoompan=z='if(lte(pzoom,1.0)," + startZoom +",max(" 
				+ endZoom + ",pzoom-" + speed + "))':x='iw/2-(iw/pzoom/2)':y='ih/2-(ih/pzoom/2)':d=1,scale=hd1080";
			}
			
			String overlayCmd = "";
			if (section.getSectionOverlay() != null && i - startIndex == 0) {
				String fullText = section.getSectionOverlay().getTitle().replace("'", "\''") + "\n\n\n" 
					+ String.join("\n\n\n", section.getSectionOverlay().getBulletPoints().stream().map(x -> " • " + x).collect(Collectors.toList()));
				
				List<String> overlayTexts = new ArrayList<>();
				
				double charDuration = 3.0 / fullText.length(); 
				
				for (int oi = 1; oi <= fullText.length(); oi++) {
					if (fullText.charAt(oi - 1) == '\\')
						oi++;
					overlayTexts.add(fullText.substring(0, oi));
				}
				
				
				overlayCmd = "[v" + startIndex + "]"; 
				double startTime = 0.0;
				for (int oti = 0; oti < overlayTexts.size(); oti++) {
					String ot = overlayTexts.get(oti);
					
					double endTime = startTime + charDuration;
					if (oti == overlayTexts.size() - 1)
						endTime = 10.0;
					
					overlayCmd += "drawtext=text='" + ot +
					"':x='if(lte(t*500,170),t*500,170)':y=270:\\\n" + 
					" box=1:boxcolor=black@0.75:boxborderw=70:fontsize=40:fontcolor=#EEEEEE:enable='between(t," + startTime + "," + endTime + ")'";
					
					startTime += charDuration;
					
					if (oti != overlayTexts.size() - 1) {
						overlayCmd += ",\\\n";
					}
				}
				
				overlayCmd += "[v" + startIndex + "];";
			}
			
			cmd.append(
				"[" + i + ":v]\\\n"
				+ SCALE_FILTER + ",setsar=1,\\\n"
				+ "fade=t=in:st=0:d=1,fade=t=out:st=" + (imgDuration - 1) + ":d=1\\\n"
				+ zoompan + "\\\n"
				+ "[v" + i + "];\\\n "
				+ overlayCmd + "\\\n"
			);
			
			if (section.hasOverlay() && i == startIndex) {
				cmd.append("[0:v]scale=320:-1 [ovrl],[v1][ovrl]overlay=x='if(gte(main_w-(t)*500,main_w-w-120),main_w-(t)*500,main_w-w-120)':y=25:enable='between(t,0,10)'[v1];\\\n");
			}
		}
		
		if (pixabayVideo != null) {
			cmd.append("[" + (section.getImages().size() + startIndex + 1) +
				":v]" + SCALE_FILTER + ",setsar=1,fade=t=in:st=0:d=1,fade=t=out:st=17:d=1[vvv];"
			);
		}
		
		List<String> videoStreams = new ArrayList<>();
		for (int i = startIndex; i < section.getImages().size() + startIndex; i++) {
			videoStreams.add("[v" + i + "]");
		}
		
		if (pixabayVideo != null)
			videoStreams.add("[vvv]");
		
		String firstSectionStream = videoStreams.get(0);
		List<String> otherSections = videoStreams.subList(1, videoStreams.size());
		Collections.shuffle(otherSections);
		
		cmd.append(firstSectionStream + String.join("", otherSections) + "\\\nconcat=n=" + (videoStreams.size()));
		cmd.append(":v=1:a=0,format=yuv420p[v];\\\n");
		
		cmd.append("[v]drawtext=text='\\ " + section.getTitle().replace("'", "\''") + "\\\n " 
				+ "':x=(0):y=(text_h+50):fontfile=/usr/share/fonts/truetype/dejavu/DejaVuSansCondensed-Bold.ttf\\\n"
				+ ":box=1: boxcolor=#A4031F@0.95:boxborderw=15\\\n"
				+ ":fontsize=45:fontcolor=white[v]\\\n"
				+ "\"\\\n -map \"[v]\" -map " 
				+ (section.getImages().size() + (section.hasOverlay() ? 1 : 0)) 
				+ ":a output/video.final." + sectionIndex + ".mp4");

		PrintWriter out = new PrintWriter("gen_video.sh");
		out.println(cmd);
		out.close();

		System.out.println("CREATING VIDEO FILE output/video.final." + sectionIndex + ".mp4");
		try {
			ProcessRunner.run("sh", "gen_video.sh");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
