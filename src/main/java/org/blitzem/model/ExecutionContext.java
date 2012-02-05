package org.blitzem.model;

import java.io.File;
import java.util.Properties;

import org.blitzem.provider.api.AWSDriver;
import org.blitzem.provider.api.Driver;
import org.blitzem.provider.api.GenericDriver;
import org.blitzem.provider.api.RackspaceUKDriver;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;
import org.jclouds.loadbalancer.LoadBalancerServiceContextFactory;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: 07/01/2012
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
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
		
		if ("aws".equals(provider)) {
        	this.driver = new AWSDriver(cloudComputeAccessKeyId, cloudComputeSecretKey, cloudLBAccessKeyId, cloudLBSecretKey, cloudComputeProvider, cloudLBProvider, cloudConfigFile);
        } else if ("rackspace-uk".equals(provider)) {
        	this.driver = new RackspaceUKDriver(cloudComputeAccessKeyId, cloudComputeSecretKey, cloudLBAccessKeyId, cloudLBSecretKey, cloudComputeProvider, cloudLBProvider, cloudConfigFile);
        } else {
        	this.driver = new GenericDriver(cloudComputeAccessKeyId, cloudComputeSecretKey, cloudLBAccessKeyId, cloudLBSecretKey, cloudComputeProvider, cloudLBProvider, cloudConfigFile);
        }
    }

	/**
	 * @return the driver
	 */
	public Driver getDriver() {
		return driver;
	}

	public void close() {
		driver.close();
	}
}
