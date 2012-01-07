package com.github.rnorth.blitzemj.commands;

import com.github.rnorth.blitzemj.model.ExecutionContext;
import com.github.rnorth.blitzemj.model.Node;

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
