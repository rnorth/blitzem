package com.github.rnorth.blitzemj.commands;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;

/**
 * A command to bring down (destroy) a {@link Node}.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class DownCommand extends BaseCommand implements PerNodeCommand, PerLoadBalancerCommand {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(DownCommand.class);

	/** 
	 * {@inheritDoc}
	 */
	public void execute(final Node node, ComputeService computeService) throws CommandException {

		Set<? extends NodeMetadata> existingNodes = Node.findExistingNodesMatching(node, computeService);

		if (existingNodes.isEmpty()) {
			CONSOLE_LOG.info("Node does not exist");
			return;
		} else {
			node.preDown(computeService);
			node.down(computeService);
			node.postDown(computeService);
		}

	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void execute(LoadBalancer loadBalancer, LoadBalancerService loadBalancerService, ComputeService computeService) throws CommandException {
		
		Set<LoadBalancerMetadata> existingLBs = LoadBalancer.findExistingLoadBalancersMatching(loadBalancer, loadBalancerService);

		if (existingLBs.isEmpty()) {
			CONSOLE_LOG.info("Load balancer does not exist");
			return;
		} else {
			loadBalancer.preDown(loadBalancerService, computeService);
			loadBalancer.down(loadBalancerService, computeService);
			loadBalancer.postDown(loadBalancerService, computeService);
		}
	}
}
