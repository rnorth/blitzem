package org.blitzem.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.blitzem.TaggedItemRegistry;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.blitzem.provider.api.Driver;
import org.blitzem.util.Table;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.google.common.collect.Lists;

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
	public void execute(Driver driver) {

        CONSOLE_LOG.info("Fetching status of nodes and load balancers");

        	List<List<String>> table = Lists.newArrayList();
        	table.add(Arrays.asList("Node name", "Status", "Public IP Address(es)", "Private IP Address(es)", "Tags", "Location"));
        	
        	for (Node node : TaggedItemRegistry.getInstance().findMatching(null, Node.class)) {
        		Set<? extends NodeMetadata> liveNodes = driver.getNodeMetadataForNodesMatching(node);
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
        	Table.printTable(table, true);
		
        	table.clear();
        	table.add(Arrays.asList("LB name", "Status", "IP Address", "Tags", "Applies to nodes tagged", "Type", "Location"));
        	for (LoadBalancer loadBalancer : TaggedItemRegistry.getInstance().findMatching(null, LoadBalancer.class)) {
        		Set<? extends LoadBalancerMetadata> liveLBs = driver.getLoadMetadataForLoadBalancersMatching(loadBalancer);
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
        	Table.printTable(table, true);
		
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getHelpSummary() {
		return "Displays the status of all nodes and load balancers in the environment";
	}
}
