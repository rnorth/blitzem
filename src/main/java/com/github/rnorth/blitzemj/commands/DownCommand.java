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

		Set<? extends NodeMetadata> existingNodes = findExistingNodesMatching(node, computeService);

		if (existingNodes.isEmpty()) {
			CONSOLE_LOG.info("Node does not exist");
			return;
		}

		for (NodeMetadata existingNode : existingNodes) {
			CONSOLE_LOG.info("Bringing down node {}", existingNode.getName());
			computeService.destroyNode(existingNode.getId());
			CONSOLE_LOG.info("Node destroyed");
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void execute(LoadBalancer loadBalancer, LoadBalancerService loadBalancerService, ComputeService computeService) throws CommandException {
		
		Set<LoadBalancerMetadata> existingLBs = findExistingLoadBalancersMatching(loadBalancer, loadBalancerService);

		if (existingLBs.isEmpty()) {
			CONSOLE_LOG.info("Load balancer does not exist");
			return;
		}

		for (LoadBalancerMetadata existingLB : existingLBs) {
			CONSOLE_LOG.info("Bringing down load balancer {}", existingLB.getName());
			loadBalancerService.destroyLoadBalancer(existingLB.getId());
			CONSOLE_LOG.info("Load balancer destroyed");
		}
	}
}
