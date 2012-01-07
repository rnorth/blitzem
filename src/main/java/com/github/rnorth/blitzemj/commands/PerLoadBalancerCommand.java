package com.github.rnorth.blitzemj.commands;

import com.github.rnorth.blitzemj.model.ExecutionContext;
import org.jclouds.compute.ComputeService;
import org.jclouds.loadbalancer.LoadBalancerService;

import com.github.rnorth.blitzemj.model.LoadBalancer;

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
	 * @param executionContext
	 *            to carry out the command with
	 * @throws CommandException
	 */
	void execute(LoadBalancer loadBalancer, ExecutionContext executionContext) throws CommandException;

}