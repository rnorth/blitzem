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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.github.rnorth.blitzemj.commands.DownCommand;
import com.github.rnorth.blitzemj.commands.UpCommand;
import com.github.rnorth.blitzemj.model.Defaults;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class DownCommandTest {

	@Mock ComputeService mockComputeService;
	
	@Before
	public void setup() {
		initMocks(this);
		Defaults.load();
	}
	
	@Test
	public void doesNotDestroyNodeThatDoesNotExist() throws Exception {
		
		Node node = dummyNode("nodename", new String[] {"tag1, tag2"});
		
		final Set emptyNodeMetadataSet = Sets.newHashSet();
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(emptyNodeMetadataSet);
		
		new DownCommand().execute(node, mockComputeService);
		
		verify(mockComputeService, times(0)).destroyNode(anyString());
	}
	
	@Test
	public void destroysExistingNode() throws Exception {
		
		Node node = dummyNode("nodename", new String[] {"tag1, tag2"});
		
		final NodeMetadata mockNodeMetadata = mock(NodeMetadata.class);
		when(mockNodeMetadata.getId()).thenReturn("nodeID");
		final Set existingNodeMetadataSet = Sets.newHashSet(mockNodeMetadata);
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(existingNodeMetadataSet);
		
		new DownCommand().execute(node, mockComputeService);
		
		verify(mockComputeService, times(1)).destroyNode(eq("nodeID"));
	}
	
	protected Node dummyNode(String nodeName, String[] tags) {
		Node node = new Node(nodeName, tags);
		return node;
	}
}
