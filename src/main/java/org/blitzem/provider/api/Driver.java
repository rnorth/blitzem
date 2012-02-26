package org.blitzem.provider.api;

import java.util.Set;

import org.blitzem.commands.CommandException;
import org.blitzem.model.ExecutionContext;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;

public interface Driver {

	void addNodeToLoadBalancer(Node node, LoadBalancer loadBalancer);

	Set<? extends LoadBalancerMetadata> getLoadMetadataForLoadBalancersMatching(LoadBalancer loadBalancer);

	Set<? extends NodeMetadata> getNodeMetadataForNodesMatching(final Node node);

	void loadBalancerUp(LoadBalancer loadBalancer, Iterable<Node> associatedNodes);
	
	void loadBalancerDown(LoadBalancer loadBalancer);

	void nodeDown(Node node);

	void nodeUp(Node node) throws CommandException;

	void removeNodeFromLoadBalancer(Node node, LoadBalancer loadBalancer);

	boolean isUp(LoadBalancer loadBalancer);
	
	boolean isUp(Node node);

	void close();
}
