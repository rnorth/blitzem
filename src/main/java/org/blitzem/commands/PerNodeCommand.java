package org.blitzem.commands;

import org.blitzem.model.Node;
import org.blitzem.provider.api.Driver;

/**
 * A specialization of {@link Command} which should be executed on every
 * {@link Node} separately.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public interface PerNodeCommand extends Command {

	/**
	 * Execute this command.
	 * 
	 * @param node
	 *            the node to apply the command to
	 * @param driver
	 *            to carry out the command with
	 * @throws CommandException
	 */
	void execute(Node node, Driver driver) throws CommandException;

}
