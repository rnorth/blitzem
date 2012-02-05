package org.blitzem.commands;

import org.blitzem.model.LoadBalancer;
import org.blitzem.provider.api.Driver;

/**
 * A specialization of {@link Command} which should be executed on every
 * {@link LoadBalancer} separately.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public interface PerLoadBalancerCommand extends Command {

	/**
	 * Execute this command.
	 * 
	 * @param loadBalancer
	 *            the load balancer to apply the command to
	 * @param driver
	 *            to carry out the command with
	 * @throws CommandException
	 */
	void execute(LoadBalancer loadBalancer, Driver driver) throws CommandException;

}