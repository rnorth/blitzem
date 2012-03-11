package org.blitzem.commands;

import java.io.PrintStream;
import java.util.List;

import org.blitzem.console.BlitzemConsole;
import org.blitzem.provider.api.Driver;
import org.blitzem.util.Table;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * A command to display help for using the Blitzem application.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class HelpCommand extends BaseCommand implements WholeEnvironmentCommand {
	
	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(HelpCommand.class);

	
	private static final String logo = 
			" _     _  _                         \n" + 
			"| |   | |(_)  _                     \n" + 
			"| |__ | | _ _| |_ _____ _____ ____  \n" + 
			"|  _ \\| || (_   _|___  ) ___ |    \\ \n" + 
			"| |_) ) || | | |_ / __/| ____| | | |\n" + 
			"|____/ \\_)_|  \\__|_____)_____)_|_|_|\n" + 
			"                                    \n";
	
	public void execute(Driver driver) throws CommandException {
		PrintStream out = new PrintStream(AnsiConsole.wrapOutputStream(System.out));
		
		out.println(logo);
		
		List<List<String>> table = Lists.newArrayList();
		table.add(Lists.newArrayList("Command", "Description"));
		
		for (Class<? extends Command> commandClass : BlitzemConsole.SUPPORTED_COMMANDS) {
			try {
				String commandName = commandClass.getSimpleName().replace("Command", "");
				String helpSummary = commandClass.newInstance().getHelpSummary();
				
				table.add(Lists.newArrayList(commandName, helpSummary));
				
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Table.printTable(table, true);
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getHelpSummary() {
		return "Displays this help text";
	}

}
