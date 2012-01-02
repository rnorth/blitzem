package com.github.rnorth.blitzemj.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;

import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Command to display status of the environment in tabular format.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class StatusCommand extends BaseCommand implements WholeEnvironmentCommand {

	/**
	 * {@inheritDoc}
	 */
	public void execute(ComputeService computeService) {

		List<List<String>> table = Lists.newArrayList();
		table.add(Arrays.asList(new String[] { "Node name", "Status", "IP Address", "Tags" }));

		for (Node node : TaggedItemRegistry.getInstance().findMatching(null, Node.class)) {
			Set<? extends NodeMetadata> liveNodes = findExistingNodesMatching(node, computeService);
			if (liveNodes.size() > 0) {
				for (NodeMetadata liveNode : liveNodes) {
					table.add(Arrays.asList(new String[] { node.getName(), "UP", liveNode.getPublicAddresses().toString(),
							node.getTags().toString() }));
				}
			} else {
				table.add(Arrays.asList(new String[] { node.getName(), "DOWN", "n/a", node.getTags().toString() }));
			}
		}
		printTable(table, true);
	}

	/**
	 * Print out information in tabular format. Adapted from {@link http
	 * ://stackoverflow
	 * .com/questions/275338/java-print-a-2d-string-array-as-a-right
	 * -justified-table/275438#275438}
	 * 
	 * @param table
	 *            a list of table rows, each of which should be a list of
	 *            strings (for each entry in the row).
	 * @param headerUnderline
	 *            whether to underline the header.
	 */
	private void printTable(List<List<String>> table, boolean headerUnderline) {
		// Find out what the maximum number of columns is in any row
		int maxColumns = 0;
		for (List<String> row : table) {
			maxColumns = Math.max(row.size(), maxColumns);
		}

		// Find the maximum length of a string in each column
		int[] lengths = new int[maxColumns];
		for (List<String> row : table) {
			for (int j = 0; j < row.size(); j++) {
				lengths[j] = Math.max(row.get(j).length(), lengths[j]);
			}
		}

		if (headerUnderline) {
			List<String> underlineRow = Lists.newArrayList();
			for (int j = 0; j < maxColumns; j++) {
				underlineRow.add(Strings.repeat("=", lengths[j]));
			}
			table.add(1, underlineRow);
		}

		// Generate a format string for each column
		String[] formats = new String[lengths.length];
		for (int i = 0; i < lengths.length; i++) {
			formats[i] = "%1$" + lengths[i] + "s" + (i + 1 == lengths.length ? "\n" : " ");
		}

		// Print 'em out
		for (List<String> row : table) {
			for (int j = 0; j < row.size(); j++) {
				System.out.printf(formats[j], row.get(j));
			}
		}
	}
}
