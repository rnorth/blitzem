package org.blitzem.commands;

import java.util.Set;

import org.blitzem.TaggedItemRegistry;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.blitzem.provider.api.Driver;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public void execute(final Node node, Driver driver) throws CommandException {

		Set<? extends NodeMetadata> existingNodes = driver.getLoadMetadataForNodesMatching(node);

		if (!existingNodes.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (NodeMetadata thisExistingNode : existingNodes) {
				publicAddresses.addAll(thisExistingNode.getPublicAddresses());
			}
			CONSOLE_LOG.info("Node already exists - IP Address(es): {}", publicAddresses);
        } else {
			driver.nodeUp(node);
			Set<LoadBalancer> lbsToNotify = node.findLoadBalancersToNotifyOfChange();

	        for (LoadBalancer lb : lbsToNotify) {
	        	if (driver.isUp(lb)) {
        			CONSOLE_LOG.info("Load balancer {} being notified that {} is up", lb, node);
        			driver.addNodeToLoadBalancer(node, lb);
	        	}
	        }
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void execute(LoadBalancer loadBalancer, Driver driver) throws CommandException {

        Set<? extends LoadBalancerMetadata> existingLBs = driver.getLoadMetadataForLoadBalancersMatching(loadBalancer);
		if (!existingLBs.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (LoadBalancerMetadata thisExistingLB : existingLBs) {
				publicAddresses.addAll(thisExistingLB.getAddresses());
			}
			CONSOLE_LOG.info("Load Balancer already exists - IP Address(es): {}", publicAddresses);
        } else {
			Iterable<Node> associatedNodes = TaggedItemRegistry.getInstance().findMatching(loadBalancer.getAppliesToTag(), Node.class);
			
			driver.loadBalancerUp(loadBalancer, associatedNodes);
		}
	}
}
