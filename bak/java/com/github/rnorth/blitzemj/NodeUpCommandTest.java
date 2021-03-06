package com.github.rnorth.blitzemj;

import org.blitzem.commands.UpCommand;
import org.blitzem.model.ExecutionContext;
import org.blitzem.model.Node;
import org.blitzem.model.ScriptExecution;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class NodeUpCommandTest {

	@Mock
    private ComputeService mockComputeService;
	@Mock
    private TemplateBuilder mockTemplateBuilder;

	@Before
	public void setup() {
		initMocks(this);

		when(mockComputeService.templateBuilder()).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.minRam(anyInt())).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.minCores(anyDouble())).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.osVersionMatches(anyString())).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.osFamily(any(OsFamily.class))).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.os64Bit(anyBoolean())).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.options(any(TemplateOptions.class))).thenReturn(mockTemplateBuilder);
		when(mockTemplateBuilder.imageNameMatches(anyString())).thenReturn(mockTemplateBuilder);
	}

	@Test
	public void canRaiseANodeWhenNoExistingNodeMatching() throws Exception {

		Node node = dummyNode("nodename", new String[] { "tag1, tag2" });

		final Set emptyNodeMetadataSet = Sets.newHashSet();
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(emptyNodeMetadataSet);

		new UpCommand().execute(node, new ExecutionContext(mockComputeService));

		verify(mockComputeService).createNodesInGroup(eq("nodename"), eq(1), any(Template.class));
	}

	@Test
	public void doesNotRaiseNodeWhenAlreadyExisting() throws Exception {

		Node node = dummyNode("nodename", new String[] { "tag1, tag2" });

		final Set existingNodeMetadataSet = Sets.newHashSet(mock(NodeMetadata.class));
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(existingNodeMetadataSet);

		new UpCommand().execute(node, new ExecutionContext(mockComputeService));

		verify(mockComputeService, times(0)).createNodesInGroup(eq("nodename"), eq(1), any(Template.class));
	}

	@Test
	public void alwaysInstallsSshPublicKey() throws Exception {

		Node node = dummyNode("nodename", new String[] { "tag1, tag2" });

		final Set emptyNodeMetadataSet = Sets.newHashSet();
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(emptyNodeMetadataSet);

		new UpCommand().execute(node, new ExecutionContext(mockComputeService));
		verify(mockTemplateBuilder, times(1)).options(argThat(new IsPublicSshKeyUpload()));
	}
	
	@Test
	public void canExecuteScriptAfterBuild() throws Exception {

		Node node = dummyNode("nodename", new String[] { "tag1, tag2" });
		node.setProvisioning(Lists.newArrayList( new ScriptExecution("hostname") ));

		final Set emptyNodeMetadataSet = Sets.newHashSet();
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn(emptyNodeMetadataSet);

		new UpCommand().execute(node, new ExecutionContext(mockComputeService));
		verify(mockTemplateBuilder, times(1)).options(argThat(new IsPublicSshKeyUpload()));
		verify(mockTemplateBuilder, times(1)).options(argThat(new IsScriptExecution("hostname")));
	}

	Node dummyNode(String nodeName, String[] tags) {
        return new Node(nodeName, tags);
	}

	private class IsPublicSshKeyUpload extends BaseMatcher<TemplateOptions> implements Matcher<TemplateOptions> {

		public void describeTo(Description description) {
			// TODO Auto-generated method stub

		}

		public boolean matches(Object item) {
			if (item instanceof TemplateOptions) {
				TemplateOptions options = (TemplateOptions) item;
				return options.getPublicKey() != null && !options.getPublicKey().isEmpty();
			}
			return false;
		}
	}
	
	public class IsScriptExecution extends BaseMatcher<TemplateOptions> implements Matcher<TemplateOptions> {

		private final String script;

		public IsScriptExecution(String script) {
			this.script = script;
		}

		public void describeTo(Description description) {
			// TODO Auto-generated method stub
			
		}

		public boolean matches(Object item) {
			if (item instanceof TemplateOptions) {
				TemplateOptions options = (TemplateOptions) item;
				try {
					return ("[statements=["+script+"{lf}]]").equals(options.getRunScript().toString());
				} catch (Exception e) {
					return false;
				}
				
			}
			return false;
		}


	}
}
