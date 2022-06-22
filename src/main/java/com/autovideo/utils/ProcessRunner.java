package com.autovideo.utils;

import java.io.IOException;

public class ProcessRunner {
	/**
	 * Executes a command using Runtime.exec() and waits for it to end.
	 * Starts two thread that will print the stdout and stderr of the
	 * command to the stdout of the current process.
	 * @param command the command to execute
	 * @return	the exit code of the process, 0 meaning success
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static int run(String ...command) throws IOException, InterruptedException {
		String finalCommand = String.join(" ", command);
		
		System.out.println("Running: " + finalCommand);
		Process cmd = Runtime.getRuntime().exec(finalCommand);
		
		
		ProcessOutputPrinter errorGobbler = new ProcessOutputPrinter(cmd.getErrorStream());
		ProcessOutputPrinter outputGobbler = new ProcessOutputPrinter(cmd.getInputStream());
		errorGobbler.start();
		outputGobbler.start();
		
		int exitCode = cmd.waitFor();
		if (exitCode == 0) {
			System.out.println(" OK!");
		} else {
			System.out.println(" ERROR!");
			throw new RuntimeException("Error executing sh script");
		}
		return exitCode;
	}
	
}
