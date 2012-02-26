package org.blitzem.test.integ;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class BlitzemIntegTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlitzemIntegTest.class);
	private static File tempDir;
	
	@BeforeClass
	public static void unpackAssembly() throws Exception {
		tempDir = Files.createTempDir();
		exec("cp target/*.zip {}/ && cd {} && unzip *.zip", tempDir.getCanonicalPath(), tempDir.getCanonicalPath());
	}

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
					ByteArrayOutputStream thisChunkBaos = new ByteArrayOutputStream();
					ByteStreams.copy(process.getInputStream(), thisChunkBaos);
					System.out.print(thisChunkBaos.toString());
					
					ByteStreams.copy(new ByteArrayInputStream(thisChunkBaos.toByteArray()), stdoutBaos);
				} catch (IOException e) {
				}
			}
			
		};
		executor.schedule(watchdog, 15 * 60, TimeUnit.SECONDS);
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
		return exec("cd " + tempDir.getCanonicalPath()+"/blitzem*/ && " + command, args);
	}
	
	
	@Test
	public void testStatus() throws Exception {
		execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy down");
		String stdout = execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy status");
		
		assertTrue(stdout.contains("Applying command StatusCommand to whole environment"));
		assertTrue(stdout.contains("Fetching status of nodes and load balancers"));
		
		execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy up");
		stdout = execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy status");
		Matcher matcher = Pattern.compile(".*web-lb1\\s+UP\\s+\\[([\\d\\.]+).*", Pattern.MULTILINE + Pattern.DOTALL).matcher(stdout);
		assertTrue(matcher.matches());
		String loadBalancerIpAddress = matcher.group(1);
		
		URL loadBalancerUrl = new URL("http", loadBalancerIpAddress, 80, "");
		System.out.println(loadBalancerUrl);
		Set<String> contentSeen = Sets.newHashSet();
		while (contentSeen.size()<4) {
			String content = new String(ByteStreams.toByteArray((java.io.InputStream) loadBalancerUrl.openStream()));
			contentSeen.add(content);
			Thread.sleep(100L);
		}
		System.out.println(contentSeen);
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy down");
	}
}
