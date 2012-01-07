package com.github.rnorth.blitzemj;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import static org.hamcrest.Matcher.*;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.Location;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.github.rnorth.blitzemj.commands.CommandException;
import com.github.rnorth.blitzemj.commands.UpCommand;
import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.github.rnorth.blitzemj.model.ScriptExecution;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
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
		
		new UpCommand().execute(loadBalancer, mockLoadBalancerService, mockComputeService);
		
		Set<NodeMetadata> nodeSet = Sets.newHashSet(node1Metadata, node2Metadata);
		verify(mockLoadBalancerService, times(1)).createLoadBalancerInLocation((Location) eq(null), eq("web-lb1"), eq("http"), eq(80), eq(8080), eq(nodeSet));
	}
}
