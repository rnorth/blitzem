package com.github.rnorth.blitzemj;

import org.blitzem.commands.DownCommand;
import org.blitzem.model.ExecutionContext;
import org.blitzem.model.Node;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class DownCommandTest {

	@Mock
    private ComputeService mockComputeService;
    private ExecutionContext mockExecutionContext;

    @Before
	public void setup() {
		initMocks(this);
        mockExecutionContext = new ExecutionContext(mockComputeService);
	}

    @Test
	public void doesNotDestroyNodeThatDoesNotExist() throws Exception {
		
		Node node = dummyNode("nodename", new String[] {"tag1, tag2"});
		
		final Set emptyNodeMetadataSet = Sets.newHashSet();
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(emptyNodeMetadataSet);
		
		new DownCommand().execute(node, mockExecutionContext);
		
		verify(mockComputeService, times(0)).destroyNode(anyString());
	}
	
	@Test
	public void destroysExistingNode() throws Exception {
		
		Node node = dummyNode("nodename", new String[] {"tag1, tag2"});
		
		final NodeMetadata mockNodeMetadata = mock(NodeMetadata.class);
		when(mockNodeMetadata.getId()).thenReturn("nodeID");
		final Set existingNodeMetadataSet = Sets.newHashSet(mockNodeMetadata);
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(existingNodeMetadataSet);
		
		new DownCommand().execute(node, mockExecutionContext);
		
		verify(mockComputeService, times(1)).destroyNode(eq("nodeID"));
	}
	
	Node dummyNode(String nodeName, String[] tags) {
        return new Node(nodeName, tags);
	}
}
