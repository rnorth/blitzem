package org.blitzem.model;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.blitzem.provider.api.AWSDriver;
import org.blitzem.provider.api.Driver;
import org.blitzem.provider.api.GenericDriver;
import org.blitzem.provider.api.RackspaceUKDriver;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.inject.CreationException;
import com.google.inject.spi.Message;

/**
 * Encapsulates the configuration/driver-selection specific aspects of
 * connecting to the cloud API.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class ExecutionContext {

	private Properties cloudConfigProperties;
	private File cloudConfigFile;
	protected String cloudComputeProvider;
	protected String cloudComputeAccessKeyId;
	protected String cloudComputeSecretKey;
	protected String cloudLBProvider;
	protected String cloudLBAccessKeyId;
	protected String cloudLBSecretKey;
	private Driver driver;

	private static final Logger CONSOLE_LOG = (Logger) LoggerFactory.getLogger(ExecutionContext.class);

	/**
	 * Create a new {@link ExecutionContext} based on given configuration
	 * properties.
	 * 
	 * @param cloudConfigProperties
	 * @param cloudConfigFile
	 *            only used to report the filename to the user in event of a
	 *            problem.
	 */
	public ExecutionContext(Properties cloudConfigProperties, File cloudConfigFile) {
		this.cloudConfigProperties = cloudConfigProperties;
		this.cloudConfigFile = cloudConfigFile;

		cloudComputeProvider = cloudConfigProperties.getProperty("compute-provider");
		cloudComputeAccessKeyId = cloudConfigProperties.getProperty("compute-accesskeyid");
		cloudComputeSecretKey = cloudConfigProperties.getProperty("compute-secretkey");

		cloudLBProvider = cloudConfigProperties.getProperty("loadbalancer-provider");
		cloudLBAccessKeyId = cloudConfigProperties.getProperty("loadbalancer-accesskeyid");
		cloudLBSecretKey = cloudConfigProperties.getProperty("loadbalancer-secretkey");

		String provider = cloudConfigProperties.getProperty("provider");

		CONSOLE_LOG.info("Connecting to {} Cloud API", provider);

		try {
			if ("aws".equals(provider)) {
				this.driver = new AWSDriver(cloudComputeAccessKeyId, cloudComputeSecretKey, cloudLBAccessKeyId, cloudLBSecretKey,
						cloudComputeProvider, cloudLBProvider, cloudConfigFile);
			} else if ("rackspace-uk".equals(provider)) {
				this.driver = new RackspaceUKDriver(cloudComputeAccessKeyId, cloudComputeSecretKey, cloudLBAccessKeyId, cloudLBSecretKey,
						cloudComputeProvider, cloudLBProvider, cloudConfigFile);
			} else {
				this.driver = new GenericDriver(cloudComputeAccessKeyId, cloudComputeSecretKey, cloudLBAccessKeyId, cloudLBSecretKey,
						cloudComputeProvider, cloudLBProvider, cloudConfigFile);
			}
		} catch (CreationException e) {
			/*
			 * Guava exceptions are already formatted, but not in a way we want.
			 * We unpack and reformat.
			 */
			Set<String> causes = Sets.newLinkedHashSet();
			for (Message m : e.getErrorMessages()) {
				final Throwable rootCause = Throwables.getRootCause(m.getCause());
				causes.add(rootCause.toString());
			}
			CONSOLE_LOG.error("An unexpected error occurred while connecting to the Cloud API: " + causes);
			throw new RuntimeException(causes.toString());
		}
	}

	/**
	 * @return the driver
	 */
	public Driver getDriver() {
		return driver;
	}

	/**
	 * Close associated resources at end of life.
	 */
	public void close() {
		driver.close();
	}

	/**
	 * @return the cloudConfigProperties
	 */
	public Properties getCloudConfigProperties() {
		return cloudConfigProperties;
	}

	/**
	 * @return the cloudConfigFile
	 */
	public File getCloudConfigFile() {
		return cloudConfigFile;
	}
}
