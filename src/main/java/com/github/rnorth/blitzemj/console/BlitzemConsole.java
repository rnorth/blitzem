package com.github.rnorth.blitzemj.console;

import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.codehaus.groovy.control.CompilationFailedException;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.github.rnorth.blitzemj.commands.BaseCommand;
import com.github.rnorth.blitzemj.commands.CommandException;
import com.github.rnorth.blitzemj.commands.DownCommand;
import com.github.rnorth.blitzemj.commands.HelpCommand;
import com.github.rnorth.blitzemj.commands.PerItemCommand;
import com.github.rnorth.blitzemj.commands.StatusCommand;
import com.github.rnorth.blitzemj.commands.UpCommand;
import com.github.rnorth.blitzemj.commands.WholeEnvironmentCommand;
import com.github.rnorth.blitzemj.model.Defaults;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Main Blitzem console entry point.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class BlitzemConsole {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger("blitzem");

	/**
	 * @param args
	 *            command line args.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BaseCommand command = (BaseCommand) new CommandArgsParser(UpCommand.class, DownCommand.class, StatusCommand.class).useDefault(
				HelpCommand.class).parse(args);

		loadEnvironmentFile(command);

		ComputeServiceContext context = loadContext();

		ComputeService computeService = context.getComputeService();

		try {

			if (command instanceof WholeEnvironmentCommand) {
				CONSOLE_LOG.info("Applying command {} to whole environment", command.getClass().getSimpleName());
				((WholeEnvironmentCommand) command).execute(computeService);
			} else if (command instanceof PerItemCommand) {
				for (Node node : TaggedItemRegistry.getInstance().findMatching(command.getNoun(), Node.class)) {
					CONSOLE_LOG.info("Applying command {} to node '{}'", command.getClass().getSimpleName(), node.getName());
					((PerItemCommand) command).execute(node, computeService);
				}
				// always display status
				new StatusCommand().execute(computeService);
			}

		} catch (CommandException e) {
			System.err.println("An unexpected error occurred: ");
			e.printStackTrace(System.err);
		} finally {
			context.close();
		}

	}

	/**
	 * Load cloud provider config files, connect, and instantiate a
	 * {@link ComputeServiceContext} for further work to be carried out on.
	 * 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static ComputeServiceContext loadContext() throws IOException, FileNotFoundException {
		File cloudConfigFile = new File(System.getProperty("user.home") + "/.blitzem/config.properties");
		if (!cloudConfigFile.exists() && !cloudConfigFile.isFile()) {
			System.err.println("Could not find required cloud configuration properties file - expected at: " + cloudConfigFile);
			throw new RuntimeException();
		}
		Properties cloudConfigProperties = new Properties();
		cloudConfigProperties.load(new FileInputStream(cloudConfigFile));
		String cloudProvider = cloudConfigProperties.getProperty("provider");
		String accesskeyid = cloudConfigProperties.getProperty("accesskeyid");
		String secretkey = cloudConfigProperties.getProperty("secretkey");
		if (cloudProvider == null || accesskeyid == null || secretkey == null) {
			System.err.println("Did not find expected provider, accesskeyid or secretkey defined in " + cloudConfigFile);
			throw new RuntimeException();
		}

		ComputeServiceContext context = new ComputeServiceContextFactory().createContext(cloudProvider, accesskeyid, secretkey,
				ImmutableSet.<Module> of(new JschSshClientModule(), new SLF4JLoggingModule()));

		CONSOLE_LOG.info("Connected to Cloud API as {}", accesskeyid);
		return context;
	}

	/**
	 * Load an environment spec file (expected to be a groovy file, referenced
	 * in the command).
	 * 
	 * Execution of the spec file will update the singleton {@link TaggedItemRegistry}.
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
