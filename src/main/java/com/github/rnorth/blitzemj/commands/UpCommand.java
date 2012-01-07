package com.github.rnorth.blitzemj.commands;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.collect.Sets;

/**
 * Command to create a {@link Node} if it does not already exist.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class UpCommand extends BaseCommand implements PerNodeCommand, PerLoadBalancerCommand {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(UpCommand.class);

	/** 
	 * {@inheritDoc}
	 */
	public void execute(final Node node, ComputeService computeService) throws CommandException {

		Set<? extends NodeMetadata> existingNodes = Node.findExistingNodesMatching(node, computeService);

		if (!existingNodes.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (NodeMetadata thisExistingNode : existingNodes) {
				publicAddresses.addAll(thisExistingNode.getPublicAddresses());
			}
			CONSOLE_LOG.info("Node already exists - IP Address(es): {}", publicAddresses);
        } else {
			node.preUp(computeService);
			node.up(computeService);
			node.postUp(computeService);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void execute(LoadBalancer loadBalancer, LoadBalancerService loadBalancerService, ComputeService computeService) throws CommandException {
	
		Set<? extends LoadBalancerMetadata> existingLBs = LoadBalancer.findExistingLoadBalancersMatching(loadBalancer, loadBalancerService);
		if (!existingLBs.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (LoadBalancerMetadata thisExistingLB : existingLBs) {
				publicAddresses.addAll(thisExistingLB.getAddresses());
			}
			CONSOLE_LOG.info("Load Balancer already exists - IP Address(es): {}", publicAddresses);
        } else {
			Iterable<Node> associatedNodes = TaggedItemRegistry.getInstance().findMatching(loadBalancer.getAppliesToTag(), Node.class);
			
			loadBalancer.preUp(loadBalancerService, computeService, associatedNodes);
			loadBalancer.up(loadBalancerService, computeService, associatedNodes);
			loadBalancer.postUp(loadBalancerService, computeService, associatedNodes);
		}
	}
}
