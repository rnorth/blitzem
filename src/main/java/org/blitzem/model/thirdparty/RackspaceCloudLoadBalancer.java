package org.blitzem.model.thirdparty;

import org.blitzem.TaggedAndNamedItem;
import org.blitzem.model.ExecutionContext;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import com.google.common.collect.Sets;
import org.jclouds.cloudloadbalancers.CloudLoadBalancersClient;
import org.jclouds.cloudloadbalancers.domain.NodeRequest;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.rest.RestContext;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: 07/01/2012
 * Time: 17:57
 * To change this template use File | Settings | File Templates.
 */
public class RackspaceCloudLoadBalancer extends LoadBalancer {

    @Override
    public void notifyIsUp(TaggedAndNamedItem itemWhichIsUp, ExecutionContext executionContext) {
        super.notifyIsUp(itemWhichIsUp, executionContext);    //To change body of overridden methods use File | Settings | File Templates.

        RestContext<Object, Object> providerSpecificContext = executionContext.getLoadBalancerService().getContext().getProviderSpecificContext();
        CloudLoadBalancersClient rsClient = CloudLoadBalancersClient.class.cast(providerSpecificContext.getApi());

        Set<LoadBalancerMetadata> existingLB = LoadBalancer.findExistingLoadBalancersMatching(this, executionContext.getLoadBalancerService());

        for (LoadBalancerMetadata loadBalancerMetadata : existingLB) {
            
            String region = loadBalancerMetadata.getId().split("/")[0];
            Integer providerId = Integer.valueOf(loadBalancerMetadata.getId().split("/")[1]);

            Set<NodeRequest> nodeRequests = Sets.newHashSet();
            
            Set<? extends NodeMetadata> nodeMetadatas = Node.findExistingNodesMatching((Node) itemWhichIsUp, executionContext.getComputeService());
            
            for (NodeMetadata nodeMetadata : nodeMetadatas) {
                NodeRequest.Builder builder = NodeRequest.builder();
                for (String address : nodeMetadata.getPublicAddresses()) {
                    builder.address(address);
                }
                builder.port(this.getNodePort());
                nodeRequests.add(builder.build());
            }

            CONSOLE_LOG.info("Adding node {} to load balancer {}", ((Node) itemWhichIsUp).getName(), this.getName());
            rsClient.getNodeClient(region).createNodesInLoadBalancer(nodeRequests, providerId);
        }

    }

    @Override
    public void notifyIsGoingDown(TaggedAndNamedItem itemWhichIsGoingDown, ExecutionContext executionContext) {
        super.notifyIsGoingDown(itemWhichIsGoingDown, executionContext);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
