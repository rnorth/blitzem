package org.blitzem.commands;

import org.blitzem.model.ExecutionContext;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.blitzem.provider.api.Driver;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * A command to bring down (destroy) a {@link Node}.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class DownCommand extends BaseCommand implements PerNodeCommand, PerLoadBalancerCommand {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(DownCommand.class);

	/** 
	 * {@inheritDoc}
	 */
	public void execute(final Node node, Driver driver) throws CommandException {

        Set<LoadBalancer> lbsToNotify = node.findLoadBalancersToNotifyOfChange();

        for (LoadBalancer lb : lbsToNotify) {
        	if (driver.isUp(lb)) {
    			CONSOLE_LOG.info("Load balancer {} being notified that {} is up", lb, node);
    			driver.removeNodeFromLoadBalancer(node, lb);
        	}
        }
		
		if (! driver.isUp(node)) {
			CONSOLE_LOG.info("Node does not exist");
        } else {
        	driver.nodeDown(node);
		}

	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void execute(LoadBalancer loadBalancer, Driver driver) throws CommandException {

		if (! driver.isUp(loadBalancer)) {
			CONSOLE_LOG.info("Load balancer does not exist");
        } else {
			driver.loadBalancerDown(loadBalancer);
		}
	}
}
