package com.autovideo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.Engine;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SpeechMarkType;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.VoiceId;
import com.autovideo.utils.Language;

public class Polly {

	private AmazonPolly polly;
	
	private VoiceId voiceId;
	
	private Engine engine;
	
	private boolean useSsml = false;
	
	private AutovideoConf conf = AutovideoConf.getInstance();
	
	private Language language;
	
	public Polly(Engine engine, Language language) {
		this.engine = engine;
		this.language = language;
		
		if (engine == Engine.Neural) useSsml = true;
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(conf.getAwsAccessKey(), conf.getAwsSecretKey());
		
		polly = AmazonPollyClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
			.withRegion(Regions.US_EAST_1).build();

		// Use this to get the list of all available voices
		// and then select according to parameters
//		DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
//		DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
//		
//		List<Voice> voices = describeVoicesResult.getVoices();
//		List<Voice> enVoices = new ArrayList<>();
//		for (Voice v : voices) {
//			enVoices.add(v);
//		}
//		Collections.shuffle(enVoices);

		// By default we just select the same voice for every language 
		if (language == Language.EN) {
			voiceId = VoiceId.Matthew;
		} else if (language == Language.NL) {
			voiceId = VoiceId.Ruben;
		} else {
			throw new UnsupportedOperationException("Unsupported polly lang: " + language + ". TODO AUTOMATIC");
		}
	}

	private String escapeSsml(String text) {
		return text.replace("&", "&amp;")
					.replace("\"", "&quot;")
					.replace("'", "&apos;")
					.replace("<", "&lt;")
					.replace(">", "&gt;");
	}
	
	

	public void synthesizeSpeechMarks(String text, String outputFileName) {
        SynthesizeSpeechRequest synthesizeSpeechRequest = new SynthesizeSpeechRequest()
                .withOutputFormat(OutputFormat.Json)
                .withSpeechMarkTypes(SpeechMarkType.Word)
                .withVoiceId(voiceId)
                .withText(text);
        
        try (FileOutputStream outputStream = new FileOutputStream(new File(outputFileName))) {
            SynthesizeSpeechResult synthesizeSpeechResult = polly.synthesizeSpeech(synthesizeSpeechRequest);
            byte[] buffer = new byte[2 * 1024];
            int readBytes;
 
            try (InputStream in = synthesizeSpeechResult.getAudioStream()){
                while ((readBytes = in.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception caught: " + e);
        }
    }
	
	public InputStream synthesize(String text, OutputFormat format) throws IOException {
		SynthesizeSpeechRequest synthReq = 
		new SynthesizeSpeechRequest().withText(text).withVoiceId(voiceId)
				.withEngine(engine)
				.withOutputFormat(format);
		
		if (useSsml)
			synthReq.withTextType(TextType.Ssml);
			
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
		
		return synthRes.getAudioStream();
	}

	public void writeToMp3(String text, String targetFileName) throws IOException {
		System.out.println("Rendering audio: engine=" + engine.name() + ", ssml=" + useSsml);
		if (useSsml) text = "<speak><amazon:domain name=\"conversational\">" + escapeSsml(text) + "</amazon:domain></speak>";
		InputStream speechStream = synthesize(text, OutputFormat.Mp3);
		File targetFile = new File(targetFileName);
		 
	    FileUtils.copyInputStreamToFile(speechStream, targetFile);
	}
	
	public Language getLanguage() {
		return language;
	}
} 