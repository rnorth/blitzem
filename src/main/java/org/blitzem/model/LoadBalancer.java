package org.blitzem.model;

import java.util.List;
import java.util.Set;

import org.blitzem.TaggedAndNamedItem;
import org.blitzem.TaggedItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Model class for a Load Balancer.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class LoadBalancer implements TaggedAndNamedItem {

	protected static final Logger CONSOLE_LOG = LoggerFactory.getLogger(LoadBalancer.class);

	private String name;
	private List<String> tags = Lists.newArrayList();
	private String protocol;
	private int port;
	private int nodePort;
	private String appliesToTag;
	
	public LoadBalancer() {
		TaggedItemRegistry.getInstance().add(this);
	}

	public List<String> getTags() {
		return tags;
	}

	public String getName() {
		return name;
	}

    /**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the nodePort
	 */
	public int getNodePort() {
		return nodePort;
	}

	/**
	 * @param nodePort the nodePort to set
	 */
	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the appliesToTag
	 */
	public String getAppliesToTag() {
		return appliesToTag;
	}

	/**
	 * @param appliesToTag the appliesToTag to set
	 */
	public void setAppliesToTag(String appliesToTag) {
		this.appliesToTag = appliesToTag;
	}

	/** 
	 * {@inheritDoc}
	 */
	public Set<String> getNotificationSubjects() {
		return Sets.newHashSet(appliesToTag);
	}
}
