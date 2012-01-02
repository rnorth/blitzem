package com.github.rnorth.blitzemj.commands;

import static org.jclouds.compute.options.TemplateOptions.Builder.authorizePublicKey;

import java.io.File;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.io.Payloads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rnorth.blitzemj.model.Node;
import com.github.rnorth.blitzemj.model.Provisioning;
import com.google.common.collect.Sets;

/**
 * Command to create a {@link Node} if it does not already exist.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class UpCommand extends BaseCommand implements PerItemCommand {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger("blitzem");

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
		templateBuilder
				.minRam(node.getSize().getMinRam())
				.minCores(node.getSize().getMinCores())
				.osFamily(OsFamily.valueOf(node.getOs().getFamily()))
				.osVersionMatches(node.getOs().getVersion())
				.os64Bit(node.getOs().getOs64Bit())
				.options(authorizePublicKey(Payloads.newPayload(new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub"))));
		
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
}
