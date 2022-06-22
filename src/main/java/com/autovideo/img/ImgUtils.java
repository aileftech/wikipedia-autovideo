package com.autovideo.img;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import com.autovideo.utils.ProcessRunner;
import com.autovideo.wiki.WikiVideo;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.io.FileSaver;
import ij.plugin.CanvasResizer;
import ij.process.FloatPolygon;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class ImgUtils {
	private static final String FONT = "data/fonts/OpenSans-Regular.ttf";
	
	private static final String GEOMETOS = "data/fonts/Geometos_Wide.ttf";

	public static void convert(String inputFile, String outputFile) {
		try {
			ProcessRunner.run("convert", inputFile, outputFile);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static int chooseBreakPoint(String captionText) {
		int length = captionText.length();
		int midpoint = length / 2;
		
		int i = midpoint;
		for ( ; i < captionText.length(); i++) {
			char charAt = captionText.charAt(i);
			if (charAt == ' ' || charAt == '-')
				return i;
		}
		
		return -1;
	}
	
	private static final int MIN_FONT_SIZE = 25;
	
	private static final int MAX_FONT_SIZE = 50;
	
	private static final int MARGIN_LEFT = 20;

	public static void createInfoImage(String outputFile, WikiVideo video) throws FontFormatException, IOException {
		int lineWidth = 16;
		int marginTop = 20;
		
		Font geoFont = Font.createFont(Font.TRUETYPE_FONT, new File(GEOMETOS));
		Font deriveFont = geoFont.deriveFont(60.0f);
		TextRoi roi = new TextRoi(lineWidth / 2 + 1, marginTop, "  ITALIAN CITIES  ", deriveFont);
		
		roi.setFillColor(new Color(0.0f, 0.0f, 0.0f));
		
		TextRoi roiCity = new TextRoi((int)(lineWidth / 2 + roi.getFloatWidth() - 20), marginTop, "    Rome  ", deriveFont);
		
		
		ImagePlus image = IJ.createImage("t", (int)(roi.getFloatWidth() + roiCity.getFloatWidth()), (int)roi.getFloatHeight() + 70, 1, 24);

		image.getProcessor().draw(roi);
		
		image.getProcessor().setColor(new Color(240, 127, 86));
		image.getProcessor().draw(roiCity);
		image.getProcessor().setColor(new Color(1.0f, 1.0f, 1.0f));
		
		Font fontSmall = geoFont.deriveFont(20.0f);
		TextRoi roi2 = new TextRoi(lineWidth / 2 + 1, roi.getFloatHeight() + marginTop + 10, "          History   |   Geography   |   Economy ", fontSmall);
		image.getProcessor().draw(roi2);
		
		TextRoi.setColor(new Color(1.0f, 1.0f, 1.0f));
		roi.setFillColor(new Color(0.0f, 0.0f, 0.0f));
		
		
		Line.setWidth(lineWidth);
		
		image.getProcessor().setColor(new Color(224, 54, 22));
		image.getProcessor().drawLine(0, 0, 0, image.getHeight());
		image.getProcessor().drawLine(image.getWidth(), 0, image.getWidth(), image.getHeight());

		FileSaver f = new FileSaver(image);
		f.saveAsJpeg(outputFile);
	}
	
	public static boolean createCaptionedImage(String inputFile, String captionText) {
		return createCaptionedImage(inputFile, captionText, false);
	}
	
	public static boolean createCaptionedImage(String inputFile, String captionText, boolean forceNormalView) {
		
		captionText = " " + captionText;

		boolean canvasView = false;
		
		System.out.println("Creating caption for " + inputFile);
		
		ImagePlus openImage = IJ.openImage(inputFile);
		
		if (openImage == null) return false;
		
		// Convert image to COLOR RGB
		if (openImage.getType() < 4) {
			ImageConverter conv = new ImageConverter(openImage);
			conv.convertToRGB();
			
			FileSaver f = new FileSaver(openImage);
			f.saveAsJpeg(inputFile);
			openImage = IJ.openImage(inputFile);
		}

		if (openImage.getWidth() > 1920) {
			int newHeight = (1920 * openImage.getHeight()) / openImage.getWidth();
			openImage = openImage.resize(1920, newHeight, "bicubic");
		}
		
		if (openImage.getWidth() < 900 && openImage.getHeight() < 600 && !forceNormalView) {
			CanvasResizer resizer = new CanvasResizer();
			openImage.getProcessor().setLineWidth(3);
			openImage.getProcessor().setColor(new Color(1.0f, 1.0f, 1.0f));
			openImage.getProcessor().drawLine(0, 0, openImage.getWidth(), 0);
			openImage.getProcessor().drawLine(0, 0, 0, openImage.getHeight());
			openImage.getProcessor().drawLine(openImage.getWidth(), 0, openImage.getWidth(), openImage.getHeight());
			openImage.getProcessor().drawLine(0, openImage.getHeight(), openImage.getWidth(), openImage.getHeight());
			ImageProcessor processor = openImage.getProcessor();
			openImage.setProcessor(resizer.expandImage(processor, 1920, 1080, (1920 - openImage.getWidth())/2, (1080 - openImage.getHeight())/2 - 120));
			canvasView = true;
		}
		
		int startMarginBottom = Math.min(125, (int)(openImage.getHeight() * 0.1));
		
		int fontSize = MAX_FONT_SIZE;
		
		TextRoi roi = new TextRoi(MARGIN_LEFT, openImage.getHeight() - startMarginBottom, captionText + " ", new Font(FONT, Font.PLAIN, fontSize));

		if (roi.getFloatWidth() >= openImage.getWidth() - MARGIN_LEFT) {
			int breakPoint = chooseBreakPoint(captionText);
			if (breakPoint != -1) {
				captionText = captionText.substring(0, breakPoint) + " \n" + captionText.substring(breakPoint);
				roi = new TextRoi(MARGIN_LEFT, openImage.getHeight() - startMarginBottom, captionText + " ", new Font(FONT, Font.PLAIN, fontSize));
			}
		}
		
		while (roi.getFloatWidth() >= openImage.getWidth() - MARGIN_LEFT && fontSize >= MIN_FONT_SIZE) {
			roi = new TextRoi(MARGIN_LEFT, openImage.getHeight() - startMarginBottom, captionText + " ", new Font(FONT, Font.PLAIN, fontSize));
			fontSize--;
		}

		if (roi.getFloatWidth() >= openImage.getWidth()) {
			// Can't create caption, e.g. text is to long even if split it goes under 25 font size
			return false;
		}

		FloatPolygon polygon = roi.getFloatPolygon();
		double yBottom = polygon.getBounds().y + roi.getFloatHeight();
		int marginBottom = 5;
		while (yBottom > openImage.getHeight() - 20) {
			roi = new TextRoi(MARGIN_LEFT, openImage.getHeight() - startMarginBottom - marginBottom, captionText + " ", new Font(FONT, Font.PLAIN, fontSize));
			polygon = roi.getFloatPolygon();
			yBottom = polygon.getBounds().y + roi.getFloatHeight();
			marginBottom += 5;
		}

		/*
		 * Center text
		 */
		if (canvasView) {
			roi = new TextRoi((openImage.getWidth() - roi.getFloatWidth())/2, openImage.getHeight() - startMarginBottom - marginBottom, captionText + " ", new Font(FONT, Font.PLAIN, fontSize));;
		}
		
		TextRoi.setColor(new Color(0.95f, 0.95f, 0.95f));
		roi.setFillColor(new Color(0f, 0f, 0f, 0.85f));
		int padding = (int)(roi.getFloatHeight() * 0.10);
		roi.setBounds(new Rectangle2D.Double(roi.getXBase(), roi.getYBase() - padding, roi.getFloatWidth() + padding, roi.getFloatHeight() + padding));
		
		
		
		openImage.setOverlay(new Overlay(roi));
		openImage.flatten();
		FileSaver f = new FileSaver(openImage);
		f.saveAsJpeg(inputFile);
		return true;
	}
	
//	public static void main(String[] args) {
//		createCaptionedImage("tmp/1.jpg", "Flag of Rome", "tmp/tmp.jpg");
//	}
}
