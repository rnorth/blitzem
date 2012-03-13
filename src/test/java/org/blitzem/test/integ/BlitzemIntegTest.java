package org.blitzem.test.integ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.blitzem.util.AddressResolution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

@RunWith(Parameterized.class)
public class BlitzemIntegTest extends ShellIntegTestBase {

	private static final String HOME = System.getProperty("user.home");
	static final Logger LOGGER = LoggerFactory.getLogger(BlitzemIntegTest.class);
	private final String configPath;
	
	@Parameters
	public static Collection<Object[]> config() {
		Object[][] config = new Object[][] { { HOME + "/.blitzem/rackspace-uk.properties" }, { HOME + "/.blitzem/aws.properties" }}; 
		return Arrays.asList(config);
	}
	
	public BlitzemIntegTest(String configPath) {
		this.configPath = configPath;
	}
	
	@Before
	public void unpackAssembly() throws Exception {
		
		// Extract blitzem
		tempDir = Files.createTempDir();
		exec("cp target/*.zip {}/ && cd {} && unzip *.zip", tempDir.getCanonicalPath(), tempDir.getCanonicalPath());
	}
	
	@Before
	public void selectConfiguration() throws IOException {
		
		LOGGER.info("Selecting configuration {}", configPath);
		
		// Put the right configuration file in place
		File cloudConfigFile = new File(HOME + "/.blitzem/config.properties");
		cloudConfigFile.delete();
		Files.copy(new File(this.configPath), cloudConfigFile);
	}

	@Test
	public void testSimpleEndToEnd() throws Exception {
		execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy down");
		String stdout = execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy status");
		
		assertTrue(stdout.contains("Applying command StatusCommand to whole environment"));
		assertTrue(stdout.contains("Fetching status of nodes and load balancers"));
		
		execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy up");
		stdout = execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy status");
		Matcher matcher = Pattern.compile(".*web-lb1\\s+UP\\s+\\[([\\w\\-\\d\\.]+).*", Pattern.MULTILINE + Pattern.DOTALL).matcher(stdout);
		assertTrue(matcher.matches());
		String loadBalancerAddress = matcher.group(1);
		
		URL loadBalancerUrl = new URL("http", loadBalancerAddress, 80, "");
		// Ensure that the load balancer hostname is resolvable (in case DNS propagation takes some time)
		InetAddress loadBalancerResolvedIpAddress = AddressResolution.blockingResolveHostname(loadBalancerAddress);
		LOGGER.info("Trying to fetch representative content from load balancer {} {}", loadBalancerUrl, loadBalancerResolvedIpAddress);
		Set<String> contentSeen = Sets.newHashSet();
		int attempts = 0;
		int failCount = 0;
		while (contentSeen.size()<4 && ++attempts < 100) {
			try {
				String content = new String(ByteStreams.toByteArray((java.io.InputStream) loadBalancerUrl.openStream()));
				contentSeen.add(content.trim());
			} catch (IOException e) {
				if (++failCount > 100) {
					fail("Failed to connect to " + loadBalancerUrl + " 100 times");
				}
				Thread.sleep(1000L);
			}
			Thread.sleep(10L);
		}
		LOGGER.info("Saw the following responses: {}", contentSeen);
		assertEquals(4, contentSeen.size());
	}
	
	@After
	public void tearDown() throws Exception {
		execInDir("bin/blitzem --source=examples/load_balanced/environment.groovy down");
	}
}
