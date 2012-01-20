package org.blitzem.commands;

import ch.qos.logback.classic.Logger;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.blitzem.TaggedItemRegistry;
import org.blitzem.model.ExecutionContext;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Command to display status of the environment in tabular format.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class StatusCommand extends BaseCommand implements WholeEnvironmentCommand {

	private static final Logger CONSOLE_LOG = (Logger) LoggerFactory.getLogger(StatusCommand.class);
	
	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionContext executionContext) {

        ComputeService computeService = executionContext.getComputeService();
        LoadBalancerService loadBalancerService = executionContext.getLoadBalancerService();

        CONSOLE_LOG.info("Fetching status of nodes and load balancers");

		List<List<String>> table = Lists.newArrayList();
		table.add(Arrays.asList("Node name", "Status", "Public IP Address(es)", "Private IP Address(es)", "Tags", "Location"));

		for (Node node : TaggedItemRegistry.getInstance().findMatching(null, Node.class)) {
			Set<? extends NodeMetadata> liveNodes = Node.findExistingNodesMatching(node, computeService);
			List row = null;
			if (liveNodes.size() > 0) {
				for (NodeMetadata liveNode : liveNodes) {
					row = Lists.newArrayList( node.getName(), "UP", liveNode.getPublicAddresses().toString(), 
							liveNode.getPrivateAddresses().toString(), node.getTags().toString(), 
							"" + liveNode.getLocation().getIso3166Codes() );
				}
			} else {
				row = Lists.newArrayList( node.getName(), "DOWN", "n/a", "n/a", node.getTags().toString(), "n/a" );
			}
			table.add(row);
		}
		System.out.println("\n\nNode status");
		printTable(table, true);
		
		table = Lists.newArrayList();
		table.add(Arrays.asList("LB name", "Status", "IP Address", "Tags", "Applies to nodes tagged", "Type", "Location"));
		for (LoadBalancer loadBalancer : TaggedItemRegistry.getInstance().findMatching(null, LoadBalancer.class)) {
			Set<LoadBalancerMetadata> liveLBs = LoadBalancer.findExistingLoadBalancersMatching(loadBalancer, loadBalancerService);
			List row = null;
			if (liveLBs.size() > 0) {
				for (LoadBalancerMetadata liveLB : liveLBs) {
					row = Lists.newArrayList(loadBalancer.getName(), "UP", liveLB.getAddresses().toString(), loadBalancer.getTags().toString(), ""+loadBalancer.getAppliesToTag(), ""+liveLB.getType(), ""+liveLB.getLocation().getIso3166Codes());
				}
			} else {
				row = Lists.newArrayList(loadBalancer.getName(), "DOWN", "n/a", loadBalancer.getTags().toString(), loadBalancer.getAppliesToTag(), "n/a", "n/a");
			}
			table.add(row);
		}
		System.out.println("\n\nLoad Balancer status");
		printTable(table, true);
		
	}

	/**
	 * Print out information in tabular format. Adapted from {@see http://stackoverflow
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
