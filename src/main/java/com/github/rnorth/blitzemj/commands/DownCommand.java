package com.github.rnorth.blitzemj.commands;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rnorth.blitzemj.model.Node;

public class DownCommand extends BaseCommand implements PerItemCommand {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger("blitzem");

	public void execute(final Node node, ComputeService computeService) throws CommandException {

		Set<? extends NodeMetadata> existingNodes = findExistingNodesMatching(node, computeService);

		if (existingNodes.isEmpty()) {
			CONSOLE_LOG.info("Node does not exist");
			return;
		}

		for (NodeMetadata existingNode : existingNodes) {
			CONSOLE_LOG.info("Bringing down node {}", existingNode.getName());
			computeService.destroyNode(existingNode.getId());
			CONSOLE_LOG.info("Node destroyed");
		}
	}
}
