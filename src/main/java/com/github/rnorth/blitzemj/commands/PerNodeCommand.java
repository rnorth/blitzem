package com.github.rnorth.blitzemj.commands;

import org.jclouds.compute.ComputeService;

import com.github.rnorth.blitzemj.model.Node;

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
	 * @param computeService
	 *            to carry out the command with
	 * @throws CommandException
	 */
	void execute(Node node, ComputeService computeService) throws CommandException;

}
