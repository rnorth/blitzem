package org.blitzem.commands;

import org.blitzem.model.ExecutionContext;
import org.blitzem.model.Node;

/**
 * A specialization of {@link Command} which should be executed for the
 * environment as a whole and not on every {@link Node} separately.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public interface WholeEnvironmentCommand extends Command {
	
	/**
	 * Execute this command.
	 * 
	 * @param executionContext
	 *            to carry out the command with
	 * @throws CommandException
	 */
	void execute(ExecutionContext executionContext) throws CommandException;

}
