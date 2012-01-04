package com.github.rnorth.blitzemj.commands;

import static org.jclouds.compute.options.TemplateOptions.Builder.authorizePublicKey;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.io.Payloads;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.github.rnorth.blitzemj.model.Provisioning;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

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

		Set<? extends NodeMetadata> existingNodes = findExistingNodesMatching(node, computeService);

		if (!existingNodes.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (NodeMetadata thisExistingNode : existingNodes) {
				publicAddresses.addAll(thisExistingNode.getPublicAddresses());
			}
			CONSOLE_LOG.info("Node already exists - IP Address(es): {}", publicAddresses);
			return;
		}
		
		final TemplateBuilder templateBuilder = computeService
				.templateBuilder();
		try {
			templateBuilder
					.minRam(node.getSize().getMinRam())
					.minCores(node.getSize().getMinCores())
					.osFamily(OsFamily.valueOf(node.getOs().getFamily()))
					.osVersionMatches(node.getOs().getVersion())
					.os64Bit(node.getOs().getOs64Bit())
					.options(authorizePublicKey(Files.toString(new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub"), Charsets.UTF_8)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (Provisioning provisioning : node.getProvisioning()) {
			templateBuilder.options(provisioning.asTemplateOption());
		}
		
		Template template = templateBuilder.build();
		CONSOLE_LOG.info("Creating node with template: {}", template);
		try {
			Set<? extends NodeMetadata> nodesInGroup = computeService.createNodesInGroup(node.getName(), 1, template);
			for (NodeMetadata createdNode : nodesInGroup) {
				CONSOLE_LOG.info("Created node {} at {}", node.getName(), createdNode.getPublicAddresses());
			}

		} catch (RunNodesException e) {
			throw new CommandException("Could not create node!", e);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public void execute(LoadBalancer loadBalancer, LoadBalancerService loadBalancerService, ComputeService computeService) throws CommandException {
	
		Set<? extends LoadBalancerMetadata> existingLBs = findExistingLoadBalancersMatching(loadBalancer, loadBalancerService);
		if (!existingLBs.isEmpty()) {

			Set<String> publicAddresses = Sets.newHashSet();
			for (LoadBalancerMetadata thisExistingLB : existingLBs) {
				publicAddresses.addAll(thisExistingLB.getAddresses());
			}
			CONSOLE_LOG.info("Load Balancer already exists - IP Address(es): {}", publicAddresses);
			return;
		}
		
		Iterable<Node> associatedNodes = TaggedItemRegistry.getInstance().findMatching(loadBalancer.getAppliesToTag(), Node.class);
		
		Set<NodeMetadata> associatedNodeMetadata = Sets.newHashSet();
		for (Node node : associatedNodes) {
			associatedNodeMetadata.addAll(findExistingNodesMatching(node, computeService));
		}
		
		LoadBalancerMetadata loadBalancerMetadata = loadBalancerService.createLoadBalancerInLocation(
				null, 
				loadBalancer.getName(), 
				loadBalancer.getProtocol(), 
				loadBalancer.getPort(), 
				loadBalancer.getNodePort(), 
				associatedNodeMetadata);
		
	}
}
