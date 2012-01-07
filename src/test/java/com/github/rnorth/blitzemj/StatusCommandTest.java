package com.github.rnorth.blitzemj;

import com.github.rnorth.blitzemj.commands.StatusCommand;
import com.github.rnorth.blitzemj.model.ExecutionContext;
import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Location;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class StatusCommandTest {

	@Mock
    private ComputeService mockComputeService;
	@Mock
    private LoadBalancerService mockLoadBalancerService;
	
	@Before
	public void setup() {
		initMocks(this);
	}
	
	@Test
	public void listsStatuses() throws Exception {
		
		Node node1 = dummyNode("node1", new String[] {"tag1, tag2"});
		Node node2 = dummyNode("node2", new String[] {"tag1, tag2"});
		TaggedItemRegistry.getInstance().add(node1);
		TaggedItemRegistry.getInstance().add(node2);
		
		LoadBalancer lb1 = dummyLB("lb1");
		
		final NodeMetadata mockNodeMetadata = mock(NodeMetadata.class);
		when(mockNodeMetadata.getLocation()).thenReturn(mock(Location.class));
		when(mockNodeMetadata.getName()).thenReturn("node1");
		final Set existingNodeMetadataSet = Sets.newHashSet(mockNodeMetadata);
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(existingNodeMetadataSet);
		
		final LoadBalancerMetadata mockLBMetadata = mock(LoadBalancerMetadata.class);
		when(mockLBMetadata.getLocation()).thenReturn(mock(Location.class));
		when(mockLBMetadata.getName()).thenReturn("lb1");
		final Set existingLoadBalancerMetadataSet = Sets.newHashSet(mockLBMetadata);
		when(mockLoadBalancerService.listLoadBalancers()).thenReturn(existingLoadBalancerMetadataSet);
		
		new StatusCommand().execute(new ExecutionContext(mockComputeService, mockLoadBalancerService));
		
	}
	
	private LoadBalancer dummyLB(String string) {
		LoadBalancer lb = new LoadBalancer();
		lb.setName(string);
		lb.setTags(Lists.newArrayList("tag1"));
		return lb;
	}

	Node dummyNode(String nodeName, String[] tags) {
        return new Node(nodeName, tags);
	}
}
