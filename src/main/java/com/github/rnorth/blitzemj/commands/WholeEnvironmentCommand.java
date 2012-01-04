package com.github.rnorth.blitzemj.commands;

import org.jclouds.compute.ComputeService;
import org.jclouds.loadbalancer.LoadBalancerService;

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
	 * @param computeService
	 *            to carry out the command with
     * @param loadBalancerService
	 *            to carry out the command with
	 * @throws CommandException
	 */
	void execute(ComputeService computeService, LoadBalancerService loadBalancerService) throws CommandException;

}
