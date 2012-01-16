package org.blitzem.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.blitzem.TaggedAndNamedItem;
import org.blitzem.TaggedItemRegistry;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Model class for a Load Balancer.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class LoadBalancer implements TaggedAndNamedItem {

	protected static final Logger CONSOLE_LOG = LoggerFactory.getLogger(LoadBalancer.class);

	private String name;
	private List<String> tags = Lists.newArrayList();
	private String protocol;
	private int port;
	private int nodePort;
	private String appliesToTag;
	
	public LoadBalancer() {
		TaggedItemRegistry.getInstance().add(this);
	}

	public List<String> getTags() {
		return tags;
	}

	public String getName() {
		return name;
	}

    /**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the nodePort
	 */
	public int getNodePort() {
		return nodePort;
	}

	/**
	 * @param nodePort the nodePort to set
	 */
	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the appliesToTag
	 */
	public String getAppliesToTag() {
		return appliesToTag;
	}

	/**
	 * @param appliesToTag the appliesToTag to set
	 */
	public void setAppliesToTag(String appliesToTag) {
		this.appliesToTag = appliesToTag;
	}

	/**
	 * @param executionContext
	 * @param associatedNodes
	 */
	public void preUp(ExecutionContext executionContext, Iterable<Node> associatedNodes) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param executionContext
	 * @param associatedNodes
	 */
	public void up(ExecutionContext executionContext, Iterable<Node> associatedNodes) {

        ComputeService computeService = executionContext.getComputeService();
        LoadBalancerService loadBalancerService = executionContext.getLoadBalancerService();

        Set<NodeMetadata> associatedNodeMetadata = Sets.newHashSet();
		for (Node node : associatedNodes) {
			associatedNodeMetadata.addAll(Node.findExistingNodesMatching(node, computeService));
        }

        loadBalancerService.createLoadBalancerInLocation(
                null,
                this.getName(),
                this.getProtocol(),
                this.getPort(),
                this.getNodePort(),
                associatedNodeMetadata);
	}

	/**
	 * @param executionContext
	 * @param associatedNodes
	 */
	public void postUp(ExecutionContext executionContext, Iterable<Node> associatedNodes) {
		// TODO Auto-generated method stub
		
	}
	

	

	/**
	 * @param loadBalancer
	 * @param loadBalancerService
	 * @return
	 */
	public static Set<LoadBalancerMetadata> findExistingLoadBalancersMatching(LoadBalancer loadBalancer,
			LoadBalancerService loadBalancerService) {
		
		Set<? extends LoadBalancerMetadata> loadBalancers = loadBalancerService.listLoadBalancers();
		Set<LoadBalancerMetadata> matchingLoadBalancers = Sets.newHashSet();
		
		for (LoadBalancerMetadata lbInstance : loadBalancers) {
			final String name = lbInstance.getName();
			if (name.equals(loadBalancer.getName())) {
				matchingLoadBalancers.add(lbInstance);
			}
		}
		
		return matchingLoadBalancers;
	}

	/**
	 * @param loadBalancerService
	 * @param computeService
	 */
	public void preDown(LoadBalancerService loadBalancerService, ComputeService computeService) {
		// no action
	}

	/**
	 * @param loadBalancerService
	 * @param computeService
	 */
	public void down(LoadBalancerService loadBalancerService, ComputeService computeService) {
		
		Set<LoadBalancerMetadata> existingLBs = LoadBalancer.findExistingLoadBalancersMatching(this, loadBalancerService);
		
		for (LoadBalancerMetadata existingLB : existingLBs) {
			CONSOLE_LOG.info("Bringing down load balancer {}", existingLB.getName());
			loadBalancerService.destroyLoadBalancer(existingLB.getId());
			CONSOLE_LOG.info("Load balancer destroyed");
		}
	}

	/**
	 * @param loadBalancerService
	 * @param computeService
	 */
	public void postDown(LoadBalancerService loadBalancerService, ComputeService computeService) {
		// no action
	}

	/** 
	 * {@inheritDoc}
	 */
	public Set<String> getNotificationSubjects() {
		return Sets.newHashSet(appliesToTag);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void notifyIsUp(TaggedAndNamedItem itemWhichIsUp, ExecutionContext executionContext) {
		if (isUp(executionContext)) {
			CONSOLE_LOG.info("Load balancer {} being notified that {} is up", this, itemWhichIsUp);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void notifyIsGoingDown(TaggedAndNamedItem itemWhichIsGoingDown, ExecutionContext executionContext) {
		if (isUp(executionContext)) {
			CONSOLE_LOG.info("Load balancer {} being notified that {} is going down", this, itemWhichIsGoingDown);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public boolean isUp(ExecutionContext executionContext) {
		return ! LoadBalancer.findExistingLoadBalancersMatching(this, executionContext.getLoadBalancerService()).isEmpty();
	}
}
