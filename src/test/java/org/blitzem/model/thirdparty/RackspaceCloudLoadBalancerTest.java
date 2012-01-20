package org.blitzem.model.thirdparty;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Set;

import org.blitzem.model.ExecutionContext;
import org.blitzem.model.LoadBalancer;
import org.blitzem.model.Node;
import org.blitzem.testsupport.TestingLogInterceptor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jclouds.cloudloadbalancers.CloudLoadBalancersClient;
import org.jclouds.cloudloadbalancers.domain.NodeRequest;
import org.jclouds.cloudloadbalancers.features.NodeClient;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.rest.RestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import ch.qos.logback.classic.Level;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import static org.mockito.Mockito.*;


public class RackspaceCloudLoadBalancerTest {

	private RackspaceCloudLoadBalancer lb;
	private ExecutionContext mockExecutionContext;
	@Mock private ComputeService mockComputeService;
	@Mock private LoadBalancerService mockLoadBalancerService;
	@Mock private CloudLoadBalancersClient mockCloudLoadBalancersClient;
	@Mock private LoadBalancerMetadata mockLBMetadata;
	@Mock private NodeMetadata mockNodeMetadata;
	
	// only for 'down' tests
	@Mock private NodeClient mockNodeClient;
	private Set<org.jclouds.cloudloadbalancers.domain.Node> mockNodesInLB = Sets.newHashSet();

	// We care about log output, so peek at log messages
	@Rule public TestingLogInterceptor testingLogInterceptor = new TestingLogInterceptor(LoadBalancer.class);
	
	@Before
	public void setup() {
		
		initMocks(this);
		
		lb = new RackspaceCloudLoadBalancer();
		lb.setName("mock-lb");
		mockExecutionContext = new ExecutionContext(mockComputeService, mockLoadBalancerService);
		
		// Set up a mock RS Client - a bit fiddly
		LoadBalancerServiceContext mockLoadBalancerServiceContext = mock(LoadBalancerServiceContext.class);
		RestContext<Object, Object> mockRestContext = mock(RestContext.class);
		when(mockLoadBalancerService.getContext()).thenReturn(mockLoadBalancerServiceContext);
		when(mockLoadBalancerServiceContext.getProviderSpecificContext()).thenReturn(mockRestContext);
		when(mockRestContext.getApi()).thenReturn(mockCloudLoadBalancersClient);
		
		// Set up mock LB service to return the right LoadBalancerMetadata
		when(mockLoadBalancerService.listLoadBalancers()).thenReturn((Set) Sets.newHashSet(mockLBMetadata));
		when(mockLBMetadata.getName()).thenReturn("mock-lb");
		when(mockLBMetadata.getId()).thenReturn("nowhere/555");
		
		// Set up the ComputeService to return the right NodeMetadata
		when(mockComputeService.listNodesDetailsMatching(any(Predicate.class))).thenReturn((Set) Sets.newHashSet(mockNodeMetadata));
		when(mockNodeMetadata.getProviderId()).thenReturn("888");
		when(mockNodeMetadata.getPublicAddresses()).thenReturn(Sets.newHashSet("1.2.3.4", "5.6.7.8"));
		when(mockNodeMetadata.getPrivateAddresses()).thenReturn(Sets.newHashSet("2.2.2.2"));
		
		// for implementations at need it provide a mock NodeClient
		when(mockCloudLoadBalancersClient.getNodeClient(eq("nowhere"))).thenReturn(mockNodeClient);
		when(mockNodeClient.listNodes(eq(555))).thenReturn(mockNodesInLB);
	}
	
	@Test
	public void shouldRemoveNodeThatIsGoingDownFromLoadBalancer() {
		
		Node node = new Node();
		
		// Context - this Node is up and load balanced, and another existing node is also up which should not be touched
		org.jclouds.cloudloadbalancers.domain.Node mockMatchingNodeWhichIsUp = mock(org.jclouds.cloudloadbalancers.domain.Node.class);
		org.jclouds.cloudloadbalancers.domain.Node mockOtherNodeWhichIsUp = mock(org.jclouds.cloudloadbalancers.domain.Node.class);
		when(mockMatchingNodeWhichIsUp.getAddress()).thenReturn("1.2.3.4");
		when(mockMatchingNodeWhichIsUp.getId()).thenReturn(666);
		when(mockOtherNodeWhichIsUp.getAddress()).thenReturn("9.9.9.9");
		when(mockOtherNodeWhichIsUp.getId()).thenReturn(999);
		
		mockNodesInLB.add(mockMatchingNodeWhichIsUp);
		mockNodesInLB.add(mockOtherNodeWhichIsUp);
		
		lb.notifyIsGoingDown(node, mockExecutionContext);
		
		verify(mockNodeClient, times(1)).removeNodeFromLoadBalancer(eq(666), eq(555));
		verify(mockNodeClient, times(0)).removeNodeFromLoadBalancer(eq(999), eq(555));
		
		assertTrue(testingLogInterceptor.didLogMessageMatching(Level.DEBUG, "Removing IP Addresses \\[.*\\] from load balancer mock-lb"));
	}
	
	@Test
	public void shouldNotRemoveLastNode() {
		
		Node node = new Node();
		
		// Context - this Node is up and load balanced, and is the only node. Hence can't remove it from the LB
		org.jclouds.cloudloadbalancers.domain.Node mockMatchingNodeWhichIsUp = mock(org.jclouds.cloudloadbalancers.domain.Node.class);
		when(mockMatchingNodeWhichIsUp.getAddress()).thenReturn("1.2.3.4");
		when(mockMatchingNodeWhichIsUp.getId()).thenReturn(666);
		
		mockNodesInLB.add(mockMatchingNodeWhichIsUp);
		
		lb.notifyIsGoingDown(node, mockExecutionContext);
		
		verify(mockNodeClient, times(0)).removeNodeFromLoadBalancer(eq(666), eq(555));
		
		assertTrue(testingLogInterceptor.didLogMessageMatching(Level.WARN, "At least one node must be assigned to a load balancer but the current action will leave mock-lb without any active nodes.*"));
	}
	
	@Test
	public void shouldAddNewNodeToLoadBalancer() {
		
		Node node = new Node();
		
		// Context - this Node is the only one, and has just come up
		
		lb.notifyIsUp(node, mockExecutionContext);
		
		verify(mockNodeClient, times(1)).createNodesInLoadBalancer(argThat(new IsNodeRequestForIPs("1.2.3.4", "5.6.7.8")), eq(555));
	}
	
	public class IsNodeRequestForIPs extends BaseMatcher<Set <NodeRequest>> implements Matcher<Set<NodeRequest>> {

		private final Set<String> ips;

		public IsNodeRequestForIPs(String... ips) {
			this.ips = Sets.newHashSet(ips);
		}

		public void describeTo(Description arg0) {
			arg0.appendValue(""+ips);
		}

		public boolean matches(Object item) {
			if (item instanceof Set) {
				Set<NodeRequest> providedNodeRequests = (Set<NodeRequest>) item;
				boolean result = true;
				for (NodeRequest request : providedNodeRequests) {
					result = result && ips.contains(request.getAddress());
				}
				return result;
			}
			return false;
		}

	}
}
