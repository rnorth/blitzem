package org.blitzem.model.thirdparty;

import org.blitzem.TaggedAndNamedItem;
import org.blitzem.model.ExecutionContext;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import com.google.common.collect.Sets;
import org.jclouds.cloudloadbalancers.CloudLoadBalancersClient;
import org.jclouds.cloudloadbalancers.domain.NodeRequest;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.rest.RestContext;

import java.util.Set;

/**
 * Provider-specific variant of {@link LoadBalancer} which adds Rackspace Cloud
 * Load Balancer features.
 * 
 * Primarily, when a load-balanced node is up/down, the load balancer will be
 * notified and its config modified to reflect the change.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class RackspaceCloudLoadBalancer extends LoadBalancer {

	@Override
	public void notifyIsUp(TaggedAndNamedItem itemWhichIsUp, ExecutionContext executionContext) {
		super.notifyIsUp(itemWhichIsUp, executionContext);

		Set<? extends NodeMetadata> nodeMetadatas = Node.findExistingNodesMatching((Node) itemWhichIsUp,
				executionContext.getComputeService());
		Set<String> nodeAddressesToAdd = Sets.newHashSet();
		Set<String> nodeIPAddressesToRemove = Sets.newHashSet();

		for (NodeMetadata nodeMetadata : nodeMetadatas) {
			nodeAddressesToAdd.addAll(nodeMetadata.getPublicAddresses());
		}

		CONSOLE_LOG.info("Adding node {} to load balancer {}", ((Node) itemWhichIsUp).getName(), this.getName());
		modifyNodesForLoadBalancer(executionContext, nodeAddressesToAdd, nodeIPAddressesToRemove);

	}

	@Override
	public void notifyIsGoingDown(TaggedAndNamedItem itemWhichIsGoingDown, ExecutionContext executionContext) {
		super.notifyIsGoingDown(itemWhichIsGoingDown, executionContext);

		Set<? extends NodeMetadata> nodeMetadatas = Node.findExistingNodesMatching((Node) itemWhichIsGoingDown,
				executionContext.getComputeService());
		Set<String> nodeAddressesToAdd = Sets.newHashSet();
		Set<String> nodeIPAddressesToRemove = Sets.newHashSet();

		for (NodeMetadata nodeMetadata : nodeMetadatas) {
			final Integer nodeId = Integer.valueOf(nodeMetadata.getProviderId());
			nodeIPAddressesToRemove.addAll(nodeMetadata.getPublicAddresses());
			nodeIPAddressesToRemove.addAll(nodeMetadata.getPrivateAddresses());
		}

		CONSOLE_LOG.info("Removing node {} from load balancer {}", ((Node) itemWhichIsGoingDown).getName(), this.getName());
		modifyNodesForLoadBalancer(executionContext, nodeAddressesToAdd, nodeIPAddressesToRemove);
	}
	
	private void modifyNodesForLoadBalancer(ExecutionContext executionContext,
			Set<String> nodeIPAddressesToAdd, Set<String> nodeIPAddressesToRemove) {
		RestContext<Object, Object> providerSpecificContext = executionContext.getLoadBalancerService().getContext()
				.getProviderSpecificContext();
		CloudLoadBalancersClient rsClient = CloudLoadBalancersClient.class.cast(providerSpecificContext.getApi());
		
		Set<LoadBalancerMetadata> existingLB = LoadBalancer.findExistingLoadBalancersMatching(this,
				executionContext.getLoadBalancerService());
		
		for (LoadBalancerMetadata loadBalancerMetadata : existingLB) {
			
			String region = loadBalancerMetadata.getId().split("/")[0];
			Integer providerId = Integer.valueOf(loadBalancerMetadata.getId().split("/")[1]);
			
			if (!nodeIPAddressesToAdd.isEmpty()) {
				CONSOLE_LOG.debug("Adding IP Addresses {} to load balancer {}", nodeIPAddressesToAdd);
				Set<NodeRequest> nodeRequests = Sets.newHashSet();
				
				NodeRequest.Builder builder = NodeRequest.builder();
				
				for (String address : nodeIPAddressesToAdd) {
					builder.address(address);
				}
				builder.port(this.getNodePort());
				nodeRequests.add(builder.build());
				
				rsClient.getNodeClient(region).createNodesInLoadBalancer(nodeRequests, providerId);
			}
			
			if (!nodeIPAddressesToRemove.isEmpty()) {
				CONSOLE_LOG.debug("Removing IP Addresses {} from load balancer {}", nodeIPAddressesToRemove);
				Set<org.jclouds.cloudloadbalancers.domain.Node> nodesInLB = rsClient.getNodeClient(region).listNodes(providerId);
				boolean atLeastOneNodeInLB = nodesInLB.size() > 1;
				
				
				for (org.jclouds.cloudloadbalancers.domain.Node nodeInLB : nodesInLB) {
					
					if (!atLeastOneNodeInLB) {
						CONSOLE_LOG.warn("At least one node must be assigned to a load balancer but the current action will leave {} without any active nodes. Please destroy the load balancer or raise other nodes and manually check the load balancer's mapping.", this.getName());
					}
					
					if (nodeIPAddressesToRemove.contains(nodeInLB.getAddress()) && atLeastOneNodeInLB) {
						rsClient.getNodeClient(region).removeNodeFromLoadBalancer(nodeInLB.getId(), providerId);
					}
				}
			}
		}
	}
}
