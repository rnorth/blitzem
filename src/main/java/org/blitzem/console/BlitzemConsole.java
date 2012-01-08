package org.blitzem.console;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import groovy.lang.GroovyShell;
import org.blitzem.TaggedItemRegistry;
import org.blitzem.commands.*;
import org.blitzem.model.Defaults;
import org.blitzem.model.ExecutionContext;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;
import org.jclouds.loadbalancer.LoadBalancerServiceContextFactory;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Main Blitzem console entry point.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class BlitzemConsole {

	private static final Logger CONSOLE_LOG = (Logger) LoggerFactory.getLogger(BlitzemConsole.class);
	private static ComputeServiceContext computeServiceContext;
	private static LoadBalancerServiceContext loadBalancerServiceContext;

	/**
	 * @param args
	 *            command line args.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BaseCommand command = (BaseCommand) new CommandArgsParser(UpCommand.class, DownCommand.class, StatusCommand.class).useDefault(
				HelpCommand.class).parse(args);

		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		Logger jclouds = (Logger) LoggerFactory.getLogger("jclouds");
		if (command.isVerbose()) {
			root.setLevel(Level.DEBUG);
			jclouds.setLevel(Level.INFO);
		} else if (command.isSuperVerbose()) {
			root.setLevel(Level.TRACE);
			jclouds.setLevel(Level.TRACE);
		}

		loadEnvironmentFile(command);

		loadContext();

		ComputeService computeService = computeServiceContext!=null ? computeServiceContext.getComputeService() : null;
		LoadBalancerService loadBalancerService = loadBalancerServiceContext!=null ? loadBalancerServiceContext.getLoadBalancerService() : null;

        ExecutionContext executionContext = new ExecutionContext(computeService, loadBalancerService);


        try {

			if (command instanceof WholeEnvironmentCommand && computeService!=null && loadBalancerService!=null) {
				CONSOLE_LOG.info("Applying command {} to whole environment", command.getClass().getSimpleName());
				((WholeEnvironmentCommand) command).execute(executionContext);
			} else if (command instanceof PerNodeCommand) {
				
				if (computeService!=null) {
					for (Node node : TaggedItemRegistry.getInstance().findMatching(command.getNoun(), Node.class)) {
						CONSOLE_LOG.info("Applying command {} to node '{}'", command.getClass().getSimpleName(), node.getName());
						((PerNodeCommand) command).execute(node, executionContext);
					}
				}
				
				if (loadBalancerService!=null) {
					for (LoadBalancer loadBalancer : TaggedItemRegistry.getInstance().findMatching(command.getNoun(), LoadBalancer.class)) {
						CONSOLE_LOG.info("Applying command {} to load balancer '{}'", command.getClass().getSimpleName(),
								loadBalancer.getName());
						((PerLoadBalancerCommand) command).execute(loadBalancer, executionContext);
					}
				}

				if (computeService!=null && loadBalancerService!=null) {
					// always display status
					new StatusCommand().execute(executionContext);
				}
			}

		} catch (CommandException e) {
			System.err.println("An unexpected error occurred: ");
			e.printStackTrace(System.err);
		} finally {
			if (computeServiceContext!=null) {
				computeServiceContext.close();
			}
			if (loadBalancerService!=null) {
				loadBalancerServiceContext.close();
			}
		}

	}

	/**
	 * Load cloud provider config files, connect, and instantiate a
	 * {@link ComputeServiceContext} and {@link LoadBalancerServiceContext} for
	 * further work to be carried out on.
	 * 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void loadContext() throws IOException {
		File cloudConfigFile = new File(System.getProperty("user.home") + "/.blitzem/config.properties");
		if (!cloudConfigFile.exists() && !cloudConfigFile.isFile()) {
			System.err.println("Could not find required cloud configuration properties file - expected at: " + cloudConfigFile);
			throw new RuntimeException();
		}

		Properties cloudConfigProperties = new Properties();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(cloudConfigFile);
            cloudConfigProperties.load(fileInputStream);
        } finally {
            if (fileInputStream!=null) {
                fileInputStream.close();
            }
        }

		String cloudComputeProvider = cloudConfigProperties.getProperty("compute-provider");
		String cloudComputeAccessKeyId = cloudConfigProperties.getProperty("compute-accesskeyid");
		String cloudComputeSecretKey = cloudConfigProperties.getProperty("compute-secretkey");
		
		if (cloudComputeProvider == null || cloudComputeAccessKeyId == null || cloudComputeSecretKey == null) {
			CONSOLE_LOG.warn("Did not find expected compute-provider, compute-accesskeyid or compute-secretkey defined in " + cloudConfigFile);
			CONSOLE_LOG.warn("No Cloud Compute nodes will be managed!");
			
		} else {
			computeServiceContext = new ComputeServiceContextFactory().createContext(cloudComputeProvider, cloudComputeAccessKeyId, cloudComputeSecretKey,
					ImmutableSet.<Module> of(new JschSshClientModule(), new SLF4JLoggingModule()));
		}

		String cloudLBProvider = cloudConfigProperties.getProperty("loadbalancer-provider");
		String cloudLBAccessKeyId = cloudConfigProperties.getProperty("loadbalancer-accesskeyid");
		String cloudLBSecretKey = cloudConfigProperties.getProperty("loadbalancer-secretkey");
		
		if (cloudLBProvider == null || cloudLBAccessKeyId == null || cloudLBSecretKey == null) {
			CONSOLE_LOG.warn("Did not find expected loadbalancer-provider, loadbalancer-accesskeyid or loadbalancer-secretkey defined in " + cloudConfigFile);
			CONSOLE_LOG.warn("No Cloud Load Balancers will be managed!");
			
		} else {
			loadBalancerServiceContext = new LoadBalancerServiceContextFactory().createContext("cloudloadbalancers-uk", cloudComputeAccessKeyId, cloudComputeSecretKey,
					ImmutableSet.<Module> of(new JschSshClientModule(), new SLF4JLoggingModule()));
		}

		

		CONSOLE_LOG.info("Connected to Cloud Compute API as {}", cloudComputeAccessKeyId);
	}

	/**
	 * Load an environment spec file (expected to be a groovy file, referenced
	 * in the command).
	 * 
	 * Execution of the spec file will update the singleton
	 * {@link TaggedItemRegistry}.
	 * 
	 * @param command
	 */
	private static void loadEnvironmentFile(BaseCommand command) {

		File sourceFile = new File(command.getSource());
		try {
			final GroovyShell groovyShell = new GroovyShell();
			groovyShell.setVariable("defaults", Defaults.DEFAULTS);
			groovyShell.evaluate(sourceFile);
		} catch (CompilationFailedException e) {
			System.err.println("Failed to parse environment definition file: " + sourceFile);
			System.err.println(e.getMessage());
			throw new RuntimeException();
		} catch (IOException e) {
			System.err.println("Could not open environment definition file: " + sourceFile);
			throw new RuntimeException();
		}
	}

}
