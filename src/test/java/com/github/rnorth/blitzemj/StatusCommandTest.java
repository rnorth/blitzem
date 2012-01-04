package com.github.rnorth.blitzemj;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.domain.internal.TemplateBuilderImpl;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.Location;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.github.rnorth.blitzemj.commands.StatusCommand;
import com.github.rnorth.blitzemj.commands.UpCommand;
import com.github.rnorth.blitzemj.model.Defaults;
import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class StatusCommandTest {

	@Mock ComputeService mockComputeService;
	@Mock LoadBalancerService mockLoadBalancerService;
	
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
		
		new StatusCommand().execute(mockComputeService, mockLoadBalancerService);
		
	}
	
	private LoadBalancer dummyLB(String string) {
		LoadBalancer lb = new LoadBalancer();
		lb.setName(string);
		lb.setTags(Lists.newArrayList("tag1"));
		return lb;
	}

	protected Node dummyNode(String nodeName, String[] tags) {
		Node node = new Node(nodeName, tags);
		return node;
	}
}
