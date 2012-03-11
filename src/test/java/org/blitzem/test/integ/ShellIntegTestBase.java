package org.blitzem.test.integ;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class ShellIntegTestBase {
	
	static final Logger LOGGER = LoggerFactory.getLogger(ShellIntegTestBase.class);
	static File tempDir;
	
	protected static String exec(String commandTemplate, String... args) throws Exception {
		String command = String.format(commandTemplate.replace("{}", "%s"), args);
		
		File tempScript = File.createTempFile("test-script", ".sh");
		Files.write("#!/bin/bash\n" + command + "\n echo OK", tempScript, Charsets.UTF_8);
		tempScript.setExecutable(true);
		
		
		LOGGER.info("$ {}", command);
		
		ProcessBuilder pb = new ProcessBuilder(tempScript.getCanonicalPath());
		final Process process = pb.start();
		
		final ByteArrayOutputStream stdoutBaos = new ByteArrayOutputStream();
		
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
		Runnable watchdog = new Runnable() {
			
			public void run() {
				LOGGER.error("Process timed out!");
				process.destroy();
			}
		};
		Runnable logPipe = new Runnable() {
	
			public void run() {
				try {
					process.getInputStream().mark(Integer.MAX_VALUE);
					ByteStreams.copy(process.getInputStream(), stdoutBaos);
					process.getInputStream().reset();
					ByteStreams.copy(process.getInputStream(), System.out);
					System.out.flush();
				} catch (IOException e) {
				}
			}
			
		};
		executor.schedule(watchdog, 20 * 60, TimeUnit.SECONDS);
		executor.scheduleAtFixedRate(logPipe, 1000, 1000, TimeUnit.MILLISECONDS);
		process.waitFor();
		Thread.sleep(50L);
		executor.shutdownNow();
		
		String stdout = null;
		String stderr = null;
		try {
			stdout = stdoutBaos.toString();
			stderr = new String(ByteStreams.toByteArray(process.getErrorStream()));
		} catch (IOException e) {
			// swallow
		}
		
		if (process.exitValue() != 0) {
			LOGGER.warn("STDOUT: {}", stdout);
			LOGGER.warn("STDERR: {}", stderr);
			throw new RuntimeException("Exit value was non-zero ("+process.exitValue()+")");
		}
		
		LOGGER.debug("STDOUT: {}", stdout);
		LOGGER.debug("STDERR: {}", stderr);
		
		return stdout;
	}

	protected static String execInDir(String command, String... args) throws Exception {
		return exec("cd " + tempDir.getCanonicalPath()+"/ && " + command, args);
	}

	public ShellIntegTestBase() {
		super();
	}

}