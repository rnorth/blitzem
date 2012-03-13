package org.blitzem.provider.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jclouds.domain.Location;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;
import org.jclouds.domain.ResourceMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerType;

import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AWSWrappedLoadBalancerMetadata implements LoadBalancerMetadata {

	private final LoadBalancerDescription description;
	private List<String> protocols = Lists.newArrayList();
	private List<Integer> ports = Lists.newArrayList();
	private final Map<String, Location> locationsForNames;

	public AWSWrappedLoadBalancerMetadata(LoadBalancerDescription description, Map<String, Location> locationsForNames) {
		this.description = description;
		this.locationsForNames = locationsForNames;
		
		for (ListenerDescription listener: description.getListenerDescriptions()) {
			this.protocols.add(listener.getListener().getInstanceProtocol());
			this.ports.add(listener.getListener().getLoadBalancerPort());
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public Location getLocation() {
		
		String firstAvailabilityZone = description.getAvailabilityZones().get(0);
		return locationsForNames.get(firstAvailabilityZone);
	}

	/** 
	 * {@inheritDoc}
	 */
	public URI getUri() {
		try {
			return new URI(protocols.get(0).toLowerCase(), "", description.getDNSName(), ports.get(0), "", "", "");
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to construct URI", e);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public Map<String, String> getUserMetadata() {
		return Collections.emptyMap();
	}

	/** 
	 * {@inheritDoc}
	 */
	public int compareTo(ResourceMetadata<LoadBalancerType> o) {
		
		if (o instanceof LoadBalancerDescription) {
			return description.equals((LoadBalancerDescription) o) ? 0 : -1;
		}
		
		return 1;
	}

	/** 
	 * {@inheritDoc}
	 */
	public LoadBalancerType getType() {
		return LoadBalancerType.LB;
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getProviderId() {
		return description.getDNSName();
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return description.getLoadBalancerName();
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getId() {
		// TODO Auto-generated method stub
		return description.getDNSName();
	}

	/** 
	 * {@inheritDoc}
	 */
	public Set<String> getAddresses() {
		return Sets.newHashSet(description.getDNSName());
	}

}
