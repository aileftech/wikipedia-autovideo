package com.autovideo.utils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class Hashing {
	public static String md5Hash(String yourString) {
		try {
			byte[] bytesOfMessage = yourString.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytesOfDigest = md.digest(bytesOfMessage);
			String digest = DatatypeConverter.printHexBinary(bytesOfDigest).toLowerCase();
			return digest;
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
