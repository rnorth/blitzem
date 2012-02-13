package org.blitzem.provider.api;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Location;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class AWSDriver extends GenericDriver implements Driver {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(AWSDriver.class);

	private AmazonElasticLoadBalancingClient awsLoadBalancerClient;
	private AmazonEC2Client ec2Client;

	private Map<String,Location> locationsForNames = Maps.newHashMap();

	private final String cloudComputeAccessKeyId;

	public AWSDriver(String cloudComputeAccessKeyId, String cloudComputeSecretKey, String cloudLBAccessKeyId, String cloudLBSecretKey,
			String cloudComputeProvider, String cloudLBProvider, File cloudConfigFile) {
		super(cloudComputeAccessKeyId, cloudComputeSecretKey, null, null, cloudComputeProvider, null, cloudConfigFile);
		this.cloudComputeAccessKeyId = cloudComputeAccessKeyId;
	
		AWSCredentials awsCredentials = new BasicAWSCredentials(cloudLBAccessKeyId, cloudLBSecretKey);
		this.awsLoadBalancerClient = new AmazonElasticLoadBalancingClient(awsCredentials);
		this.ec2Client = new AmazonEC2Client(awsCredentials);
		
		Set<? extends Location> assignableLocations = this.computeService.listAssignableLocations();
		for (Location location : assignableLocations) {
			locationsForNames.put(location.getId(), location);
		}
	}

	@Override
	public void loadBalancerUp(LoadBalancer loadBalancer, Iterable<Node> associatedNodes) {
		
		// fetch required information about the instances (nodes) this LB maps to
		List<Instance> instances = Lists.newArrayList();
		Set<String> locations = Sets.newHashSet();
		for (Node node : associatedNodes) {
			Set<? extends NodeMetadata> nodesMatching = this.getLoadMetadataForNodesMatching(node);
			for (NodeMetadata nodeMetadata : nodesMatching) {
				instances.add(new Instance(nodeMetadata.getProviderId()));
				locations.add(nodeMetadata.getLocation().getId());
			}
		}
		
		CreateLoadBalancerRequest request = new CreateLoadBalancerRequest(loadBalancer.getName());
		request.withListeners(new Listener(loadBalancer.getProtocol(), loadBalancer.getPort(), loadBalancer.getNodePort()));
		request.withAvailabilityZones(locations);
		
		CreateLoadBalancerResult balancer = awsLoadBalancerClient.createLoadBalancer(request);
		CONSOLE_LOG.info("Created load balancer {}", balancer);
		
		for (Node node : associatedNodes) {
			this.addNodeToLoadBalancer(node, loadBalancer);
		}
	}
	
	@Override
	public void loadBalancerDown(LoadBalancer loadBalancer) {
		DeleteLoadBalancerRequest deleteLoadBalancerRequest = new DeleteLoadBalancerRequest(loadBalancer.getName());
		CONSOLE_LOG.info("Bringing down load balancer {}", loadBalancer.getName());
		awsLoadBalancerClient.deleteLoadBalancer(deleteLoadBalancerRequest);
	}
	
	@Override
	public void addNodeToLoadBalancer(Node node, LoadBalancer loadBalancer) {
		
		// Open up the firewall for requests from this loadbalancer to this node
		IpPermission ipPermissions = new IpPermission();
		ipPermissions.setIpProtocol("tcp");
		final UserIdGroupPair userIdGroupPair = new UserIdGroupPair();
		userIdGroupPair.setUserId("amazon-elb");
		userIdGroupPair.setGroupName("amazon-elb-sg");
		Collection<UserIdGroupPair> userIdGroupPairs = Lists.newArrayList(userIdGroupPair);
		ipPermissions.setUserIdGroupPairs(userIdGroupPairs );
		ipPermissions.setFromPort(loadBalancer.getNodePort());
		ipPermissions.setToPort(loadBalancer.getNodePort());
		String instanceGroupId = getInstanceSecurityGroupId(node);
		AuthorizeSecurityGroupIngressRequest ingressRequest = new AuthorizeSecurityGroupIngressRequest()
																	.withIpPermissions(ipPermissions)
																	.withGroupId(instanceGroupId );
		ec2Client.authorizeSecurityGroupIngress(ingressRequest);
		
		// Add the node to the load balancer
		List<Instance> instances = Lists.newArrayList();
		Set<? extends NodeMetadata> nodesMatching = this.getLoadMetadataForNodesMatching(node);
		for (NodeMetadata nodeMetadata : nodesMatching) {
			instances.add(new Instance(nodeMetadata.getProviderId()));
		}
		
		RegisterInstancesWithLoadBalancerRequest registerRequest = new RegisterInstancesWithLoadBalancerRequest()
				.withLoadBalancerName(loadBalancer.getName())
				.withInstances(instances);
		CONSOLE_LOG.info("About to register node instance {} with load balancer; request is {}", instances, registerRequest);
		
		awsLoadBalancerClient.registerInstancesWithLoadBalancer(registerRequest);
		
		CONSOLE_LOG.info("Registered node {} with load balancer", node.getName(), loadBalancer.getName());
		
		
	}

	private String getInstanceSecurityGroupId(Node node) {
		Set<? extends NodeMetadata> nodeMetadatas = getLoadMetadataForNodesMatching(node);
		for (NodeMetadata nodeMetadata : nodeMetadatas) {
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
																		.withInstanceIds(nodeMetadata.getProviderId());
			for (Reservation reservation: ec2Client.describeInstances(describeInstancesRequest).getReservations()) {
				for (GroupIdentifier group : reservation.getGroups()) {
					return group.getGroupId();
				}
			}
		}
		return null;
	}

	@Override
	public Set<? extends LoadBalancerMetadata> getLoadMetadataForLoadBalancersMatching(LoadBalancer loadBalancer) {
		
		Set<AWSWrappedLoadBalancerMetadata> results = Sets.newHashSet();
		
		DescribeLoadBalancersResult balancers = awsLoadBalancerClient.describeLoadBalancers();
		for (LoadBalancerDescription description : balancers.getLoadBalancerDescriptions()) {
			results.add(new AWSWrappedLoadBalancerMetadata(description, locationsForNames));
		}
		
		return results;
	}
}