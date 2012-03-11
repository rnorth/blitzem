package org.blitzem.provider.api;

import java.io.File;
import java.util.Set;

import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.jclouds.cloudloadbalancers.CloudLoadBalancersClient;
import org.jclouds.cloudloadbalancers.domain.NodeRequest;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.rest.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class RackspaceUKDriver extends GenericDriver implements Driver {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(RackspaceUKDriver.class);
	protected CloudLoadBalancersClient rsLBClient;

	/**
	 * @param cloudComputeAccessKeyId
	 * @param cloudComputeSecretKey
	 * @param cloudLBAccessKeyId
	 * @param cloudLBSecretKey
	 * @param cloudComputeProvider
	 * @param cloudLBProvider
	 * @param cloudConfigFile
	 */
	public RackspaceUKDriver(String cloudComputeAccessKeyId, String cloudComputeSecretKey, String cloudLBAccessKeyId,
			String cloudLBSecretKey, String cloudComputeProvider, String cloudLBProvider, File cloudConfigFile) {
		super(cloudComputeAccessKeyId, cloudComputeSecretKey, cloudLBAccessKeyId, cloudLBSecretKey, cloudComputeProvider, cloudLBProvider,
				cloudConfigFile);

		RestContext<Object, Object> providerSpecificContext = this.loadBalancerService.getContext().getProviderSpecificContext();
		this.rsLBClient = CloudLoadBalancersClient.class.cast(providerSpecificContext.getApi());
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void addNodeToLoadBalancer(Node node, LoadBalancer loadBalancer) {
		CONSOLE_LOG.debug("Adding node {} to load balancer {}", node, loadBalancer);
		Set<NodeRequest> nodeRequests = Sets.newHashSet();
		
		Set<? extends LoadBalancerMetadata> loadBalancerMetadatas = this.getLoadMetadataForLoadBalancersMatching(loadBalancer);
		for (LoadBalancerMetadata loadBalancerMetadata : loadBalancerMetadatas) {
			
			String region = getRegionForLB(loadBalancerMetadata);
			Integer providerId = getProviderIdForLB(loadBalancerMetadata);
			
			NodeRequest.Builder builder = NodeRequest.builder();
			
			Set<String> nodeIPAddressesToAdd = this.getPublicIPAddressesForNode(node);
			for (String address : nodeIPAddressesToAdd ) {
				builder.address(address);
				builder.port(loadBalancer.getNodePort());
				nodeRequests.add(builder.build());
			}
			
			rsLBClient.getNodeClient(region).createNodesInLoadBalancer(nodeRequests, providerId);
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void removeNodeFromLoadBalancer(Node node, LoadBalancer loadBalancer) {
		
		CONSOLE_LOG.debug("Removing node {} from load balancer {}", node, loadBalancer.getName());
		Set<? extends LoadBalancerMetadata> loadBalancerMetadatas = this.getLoadMetadataForLoadBalancersMatching(loadBalancer);
		for (LoadBalancerMetadata loadBalancerMetadata : loadBalancerMetadatas) {
			
			String region = getRegionForLB(loadBalancerMetadata);
			Integer providerId = getProviderIdForLB(loadBalancerMetadata);
		
			Set<org.jclouds.cloudloadbalancers.domain.Node> nodesInLB = rsLBClient.getNodeClient(region).listNodes(providerId);
			boolean atLeastOneNodeInLB = nodesInLB.size() > 1;
			
			Set<String> nodeIPAddressesToRemove = this.getPublicIPAddressesForNode(node);
			nodeIPAddressesToRemove.addAll(this.getPrivateIPAddressesForNode(node));
			for (org.jclouds.cloudloadbalancers.domain.Node nodeInLB : nodesInLB) {
				
				if (!atLeastOneNodeInLB) {
					CONSOLE_LOG.warn("At least one node must be assigned to a load balancer but the current action will leave {} without any active nodes. Please destroy the load balancer or raise other nodes and manually check the load balancer's mapping.", loadBalancer.getName());
				}
				
				if (nodeIPAddressesToRemove.contains(nodeInLB.getAddress()) && atLeastOneNodeInLB) {
					rsLBClient.getNodeClient(region).removeNodeFromLoadBalancer(nodeInLB.getId(), providerId);
				}
			}
		}
	}

	/**
	 * @param loadBalancerMetadata
	 * @return
	 */
	private Integer getProviderIdForLB(LoadBalancerMetadata loadBalancerMetadata) {
		return Integer.valueOf(loadBalancerMetadata.getId().split("/")[1]);
	}

	/**
	 * @param loadBalancerMetadata
	 * @return
	 */
	private String getRegionForLB(LoadBalancerMetadata loadBalancerMetadata) {
		return loadBalancerMetadata.getId().split("/")[0];
	}
}
