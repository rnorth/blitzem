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

import com.github.rnorth.blitzemj.commands.StatusCommand;
import com.github.rnorth.blitzemj.commands.UpCommand;
import com.github.rnorth.blitzemj.model.Defaults;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class StatusCommandTest {

	@Mock ComputeService mockComputeService;
	
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
		
		final Set existingNodeMetadataSet = Sets.newHashSet(mock(NodeMetadata.class));
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(existingNodeMetadataSet);
		
		new StatusCommand().execute(mockComputeService);
		
	}
	
	protected Node dummyNode(String nodeName, String[] tags) {
		Node node = new Node(nodeName, tags);
		return node;
	}
}
