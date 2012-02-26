package org.blitzem.test.provider.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.blitzem.commands.CommandException;
import org.blitzem.model.Node;
import org.blitzem.model.Os;
import org.blitzem.model.Size;
import org.blitzem.provider.api.GenericDriver;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

public class GenericDriverTest {

	@Mock private ComputeService mockComputeService;
	@Mock private LoadBalancerService mockBalancerService;
	@InjectMocks private GenericDriver driver = new GenericDriver(null, null, null, null, null, null, null);
	private Node dummyNode;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		dummyNode = new Node();
		dummyNode.setName("nodeName");
		final Os os = new Os();
		os.setFamily("UBUNTU");
		os.setOs64Bit(true);
		os.setVersion("11.04");
		dummyNode.setOs(os);
		dummyNode.setSize(new Size(512, 8));
	}
	
	@Test
	public void testNodeUp() throws CommandException, RunNodesException {
		
		final TemplateBuilder mockTemplateBuilder = mock(TemplateBuilder.class);
		when(mockComputeService.templateBuilder()).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.minRam(eq(512))).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.minCores(eq(8.0))).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.osFamily(eq(OsFamily.UBUNTU))).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.osVersionMatches(eq("11.04"))).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.os64Bit(eq(true))).thenReturn(mockTemplateBuilder);
		Template mockTemplate = mock(Template.class);
		when(mockTemplateBuilder.build()).thenReturn(mockTemplate );
		
		driver.nodeUp(dummyNode);
		
		verify(mockComputeService).createNodesInGroup(eq("nodeName"), eq(1), eq(mockTemplate));
	}
	
	@Test
	public void testNodeDown() {
		
		String dummyNodeMetadataId = "foo123";
		NodeMetadata mockNodeMetadata = mock(NodeMetadata.class);
		when(mockNodeMetadata.getId()).thenReturn(dummyNodeMetadataId);
		Set allNodeMetadata = Sets.newHashSet(mockNodeMetadata);
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(allNodeMetadata);
		
		driver.nodeDown(dummyNode);
		
		verify(mockComputeService).destroyNode(eq(dummyNodeMetadataId));
	}
}
