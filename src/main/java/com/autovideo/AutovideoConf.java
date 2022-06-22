package com.autovideo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AutovideoConf {
	private static AutovideoConf instance = null;
	
	private String awsAccessKey, awsSecretKey;
	
	private String pixabayApiKey;
	
	public synchronized static AutovideoConf getInstance() {
		if (instance == null)
			instance = new AutovideoConf();
		return instance;
	}
	
	private AutovideoConf() {
		try (InputStream input = new FileInputStream("autovideo.conf")) {

            Properties prop = new Properties();
            prop.load(input);
            
            this.awsAccessKey = prop.getProperty("aws_access_key");
            this.awsSecretKey = prop.getProperty("aws_secret_key");
            this.pixabayApiKey = prop.getProperty("pixabay_api_key");
            
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read properties file: autovideo.conf");
        }
	}

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public String getPixabayApiKey() {
		return pixabayApiKey;
	}
	
	
}
