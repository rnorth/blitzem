package com.github.rnorth.blitzemj;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Set;

import com.github.rnorth.blitzemj.model.ExecutionContext;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Location;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.github.rnorth.blitzemj.commands.UpCommand;
import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;


public class LoadBalancerUpCommandTest {

	@Mock
    private LoadBalancerService mockLoadBalancerService;
	@Mock
    private ComputeService mockComputeService;
	
	@Before
	public void setup() {
		initMocks(this);
	}
	
	@Test
	public void canRaiseNewLoadBalancerWhenDoesNotAlreadyExist() throws Exception {
		Set emptyLoadBalancerSet = Sets.newHashSet();
		when(mockLoadBalancerService.listLoadBalancers()).thenReturn(emptyLoadBalancerSet);
		
		NodeMetadata node1Metadata = mock(NodeMetadata.class);
		NodeMetadata node2Metadata = mock(NodeMetadata.class);
		
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn((Set) Sets.newHashSet(node1Metadata), (Set) Sets.newHashSet(node2Metadata));
		
		Node node1 = new Node("web1", new String[] {"web"});
		Node node2 = new Node("web2", new String[] {"web", "notweb"});
		Node node3 = new Node("app2", new String[] {"notweb"});
		
		LoadBalancer loadBalancer = new LoadBalancer();
		loadBalancer.setName("web-lb1");
		loadBalancer.setProtocol("http");
		loadBalancer.setPort(80);
		loadBalancer.setNodePort(8080);
		loadBalancer.setAppliesToTag("web");
		
		new UpCommand().execute(loadBalancer, new ExecutionContext(mockComputeService, mockLoadBalancerService));
		
		Set<NodeMetadata> nodeSet = Sets.newHashSet(node1Metadata, node2Metadata);
		verify(mockLoadBalancerService, times(1)).createLoadBalancerInLocation((Location) eq(null), eq("web-lb1"), eq("http"), eq(80), eq(8080), eq(nodeSet));
	}
}
