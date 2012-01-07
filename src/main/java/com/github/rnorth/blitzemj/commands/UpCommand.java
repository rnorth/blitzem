package com.github.rnorth.blitzemj.commands;

import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.github.rnorth.blitzemj.model.ExecutionContext;
import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.collect.Sets;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

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
	public void execute(final Node node, ExecutionContext executionContext) throws CommandException {

        ComputeService computeService = executionContext.getComputeService();
		Set<? extends NodeMetadata> existingNodes = Node.findExistingNodesMatching(node, computeService);

		if (!existingNodes.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (NodeMetadata thisExistingNode : existingNodes) {
				publicAddresses.addAll(thisExistingNode.getPublicAddresses());
			}
			CONSOLE_LOG.info("Node already exists - IP Address(es): {}", publicAddresses);
        } else {
			node.preUp(executionContext);
			node.up(executionContext);
			node.postUp(executionContext);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void execute(LoadBalancer loadBalancer, ExecutionContext executionContext) throws CommandException {

        LoadBalancerService loadBalancerService = executionContext.getLoadBalancerService();
        ComputeService computeService = executionContext.getComputeService();

        Set<? extends LoadBalancerMetadata> existingLBs = LoadBalancer.findExistingLoadBalancersMatching(loadBalancer, loadBalancerService);
		if (!existingLBs.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (LoadBalancerMetadata thisExistingLB : existingLBs) {
				publicAddresses.addAll(thisExistingLB.getAddresses());
			}
			CONSOLE_LOG.info("Load Balancer already exists - IP Address(es): {}", publicAddresses);
        } else {
			Iterable<Node> associatedNodes = TaggedItemRegistry.getInstance().findMatching(loadBalancer.getAppliesToTag(), Node.class);
			
			loadBalancer.preUp(executionContext, associatedNodes);
			loadBalancer.up(executionContext, associatedNodes);
			loadBalancer.postUp(executionContext, associatedNodes);
		}
	}
}
