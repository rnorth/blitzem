package org.blitzem.provider.api;

import static org.jclouds.compute.options.TemplateOptions.Builder.authorizePublicKey;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.blitzem.commands.CommandException;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.blitzem.model.Provisioning;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;
import org.jclouds.loadbalancer.LoadBalancerServiceContextFactory;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Module;

/**
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class GenericDriver implements Driver {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(GenericDriver.class);
	protected ComputeService computeService;
	protected LoadBalancerService loadBalancerService;
	
	public GenericDriver(String cloudComputeAccessKeyId, String cloudComputeSecretKey, String cloudLBAccessKeyId, String cloudLBSecretKey, String cloudComputeProvider, String cloudLBProvider, File cloudConfigFile) {

		ComputeServiceContext computeServiceContext = null;
		if (cloudComputeProvider == null || cloudComputeAccessKeyId == null || cloudComputeSecretKey == null) {
			CONSOLE_LOG.warn("Did not find expected compute-provider, compute-accesskeyid or compute-secretkey defined in " + cloudConfigFile);
			CONSOLE_LOG.warn("No Cloud Compute nodes will be managed!");
			
		} else {
			computeServiceContext = new ComputeServiceContextFactory().createContext(cloudComputeProvider, cloudComputeAccessKeyId, cloudComputeSecretKey,
					ImmutableSet.<Module> of(new JschSshClientModule(), new SLF4JLoggingModule()));
		}
		this.computeService = computeServiceContext!=null ? computeServiceContext.getComputeService() : null;
		
		LoadBalancerServiceContext loadBalancerServiceContext = null;
		if (cloudLBProvider == null || cloudLBAccessKeyId == null || cloudLBSecretKey == null) {
			CONSOLE_LOG.warn("Did not find expected loadbalancer-provider, loadbalancer-accesskeyid or loadbalancer-secretkey defined in " + cloudConfigFile);
			CONSOLE_LOG.warn("No Cloud Load Balancers will be managed!");
			
		} else {
			loadBalancerServiceContext = new LoadBalancerServiceContextFactory().createContext(cloudLBProvider, cloudLBAccessKeyId, cloudLBSecretKey,
					ImmutableSet.<Module> of(new JschSshClientModule(), new SLF4JLoggingModule()));
		}
		this.loadBalancerService = loadBalancerServiceContext!=null ? loadBalancerServiceContext.getLoadBalancerService() : null;
	}

	/** 
	 * {@inheritDoc}
	 */
	public void removeNodeFromLoadBalancer(Node node, LoadBalancer loadBalancer) {
		// TODO Auto-generated method stub

	}

	/** 
	 * {@inheritDoc}
	 * @throws CommandException 
	 */
	public void nodeUp(Node node) throws CommandException {

        final TemplateBuilder templateBuilder = computeService
				.templateBuilder();
        
		TemplateOptions options = new TemplateOptions();
		File sshKeyFile = null;
		try {
			sshKeyFile = new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub");
			options  = authorizePublicKey(Files.toString(sshKeyFile, Charsets.UTF_8));
		} catch (IOException e) {
			CONSOLE_LOG.error("Could not load public SSH key from file: {}. Please create an SSH public/private key pair (with `ssh-keygen -t rsa`) and try again.", sshKeyFile.getAbsoluteFile());
			throw new RuntimeException(e);
		}
		
		for (Provisioning provisioning : node.getProvisioning()) {
			provisioning.asTemplateOption().copyTo(options);
		}
		
		templateBuilder
			.minRam(node.getSize().getMinRam())
			.minCores(node.getSize().getMinCores())
			.osFamily(OsFamily.valueOf(node.getOs().getFamily()))
			.osVersionMatches(node.getOs().getVersion())
			.os64Bit(node.getOs().getOs64Bit())
			.options(options);
		
		Template template = templateBuilder.build();
		CONSOLE_LOG.info("Creating node with template: {}", template);
		try {
			Set<? extends NodeMetadata> nodesInGroup = computeService.createNodesInGroup(node.getName(), 1, template);
			for (NodeMetadata createdNode : nodesInGroup) {
				String loginUsername = createdNode.getCredentials().getUser();
				CONSOLE_LOG.info("Created node {} at {}. Standard login username is "+ loginUsername, node.getName(), createdNode.getPublicAddresses());
			}

		} catch (RunNodesException e) {
			throw new CommandException("Could not create node!", e);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void nodeDown(Node node) {

        Set<? extends NodeMetadata> existingNodes = this.getLoadMetadataForNodesMatching(node);
        
		for (NodeMetadata existingNode : existingNodes) {
			CONSOLE_LOG.info("Bringing down node {}", existingNode.getName());
			computeService.destroyNode(existingNode.getId());
			CONSOLE_LOG.info("Node destroyed");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void loadBalancerDown(LoadBalancer loadBalancer) {
		
		Set<? extends LoadBalancerMetadata> existingLBs = this.getLoadMetadataForLoadBalancersMatching(loadBalancer);
		
		for (LoadBalancerMetadata existingLB : existingLBs) {
			CONSOLE_LOG.info("Bringing down load balancer {}", existingLB.getName());
			loadBalancerService.destroyLoadBalancer(existingLB.getId());
			CONSOLE_LOG.info("Load balancer destroyed");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public Set<? extends NodeMetadata> getLoadMetadataForNodesMatching(final Node node) {
        
		return computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {

            public boolean apply(ComputeMetadata arg0) {
                
            	final NodeState state = computeService.getNodeMetadata(arg0.getId()).getState();
				boolean isUp = (state == NodeState.RUNNING) || (state == NodeState.PENDING);
            	String name = arg0.getName();
            	if (name==null) {
            		name = computeService.getNodeMetadata(arg0.getId()).getGroup();
            	}

                if (name!=null && name.contains("-")) {
                    String trimmedName = name.substring(0, name.lastIndexOf('-'));
                    return trimmedName.equals(node.getName()) && isUp;
                } else {
                    return name.equals(node.getName()) && isUp;
                }

            }

        });
	}

	/** 
	 * {@inheritDoc}
	 */
	public Set<? extends LoadBalancerMetadata> getLoadMetadataForLoadBalancersMatching(LoadBalancer loadBalancer) {
		
		if (loadBalancerService==null) {
			CONSOLE_LOG.warn("Load balancer services have not been configured - cannot list available load balancers");
			return Sets.newHashSet();
		}
		
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
	 * {@inheritDoc}
	 */
	public void addNodeToLoadBalancer(Node node, LoadBalancer loadBalancer) {
		// TODO Auto-generated method stub

	}

	public boolean isUp(LoadBalancer loadBalancer) {
		return ! this.getLoadMetadataForLoadBalancersMatching(loadBalancer).isEmpty();

	}

	public boolean isUp(Node node) {
		return ! this.getLoadMetadataForNodesMatching(node).isEmpty();
	}

	public void loadBalancerUp(LoadBalancer loadBalancer, Iterable<Node> associatedNodes) {

        Set<NodeMetadata> associatedNodeMetadata = Sets.newHashSet();
		for (Node node : associatedNodes) {
			associatedNodeMetadata.addAll(this.getLoadMetadataForNodesMatching(node));
        }

        loadBalancerService.createLoadBalancerInLocation(
                null,
                loadBalancer.getName(),
                loadBalancer.getProtocol(),
                loadBalancer.getPort(),
                loadBalancer.getNodePort(),
                associatedNodeMetadata);
	}

	protected Set<String> getPublicIPAddressesForNode(Node node) {
		Set<? extends NodeMetadata> nodeMetadatas = this.getLoadMetadataForNodesMatching(node);
		Set<String> publicAddresses = Sets.newHashSet();
	
		for (NodeMetadata nodeMetadata : nodeMetadatas) {
			publicAddresses.addAll(nodeMetadata.getPublicAddresses());
		}
		return publicAddresses;
	}

	protected Set<String> getPrivateIPAddressesForNode(Node node) {
		Set<? extends NodeMetadata> nodeMetadatas = this.getLoadMetadataForNodesMatching(node);
		Set<String> privateAddresses = Sets.newHashSet();
	
		for (NodeMetadata nodeMetadata : nodeMetadatas) {
			privateAddresses.addAll(nodeMetadata.getPrivateAddresses());
		}
		return privateAddresses;
	}

	public void close() {
		if (null!=computeService && null!=computeService.getContext()) {
			computeService.getContext().close();
		}
		if (null!=loadBalancerService && null!=loadBalancerService.getContext()) {
			loadBalancerService.getContext().close();
		}
	}
}
