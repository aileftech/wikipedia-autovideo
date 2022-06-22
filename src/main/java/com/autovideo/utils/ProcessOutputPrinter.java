package com.autovideo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class that implements a thread that can print the output
 * of a process launched through Runtime.exec() to stdout.
 *
 */
public class ProcessOutputPrinter extends Thread {
	InputStream is;

	public ProcessOutputPrinter(InputStream is) {
		this.is = is;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(line);
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}