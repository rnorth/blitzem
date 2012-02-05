package org.blitzem.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.blitzem.TaggedAndNamedItem;
import org.blitzem.TaggedItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Model class for a server node.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class Node implements TaggedAndNamedItem {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(Node.class);

	private String name = null;
	private List<String> tags = Lists.newArrayList();
	private Size size = Size.DEFAULT;
	private Os os = Os.DEFAULT;
	private List<? extends Provisioning> provisioning = (List<? extends Provisioning>) Defaults.DEFAULTS.get("provisioning");

	public Node() {
		TaggedItemRegistry.getInstance().add(this);
	}

	public Node(String name, String[] tags) {
		this();
		this.name = name;
		this.tags.addAll(Arrays.asList(tags));
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [name=").append(name).append(", tags=").append(tags).append(", size=").append(size).append(", os=").append(os)
				.append("]");
		return builder.toString();
	}

	
	/** 
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

    /**
     * {@inheritDoc}
     */
    public Set<String> getNotificationSubjects() {
        return Sets.newHashSet();
    }

    /**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the size
	 */
	public Size getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Size size) {
		this.size = size;
	}

	/**
	 * @return the os
	 */
	public Os getOs() {
		return os;
	}

	/**
	 * @param os the os to set
	 */
	public void setOs(Os os) {
		this.os = os;
	}

	/**
	 * @return the provisioning
	 */
	public List<? extends Provisioning> getProvisioning() {
		return provisioning;
	}

	/**
	 * @param provisioning the provisioning to set
	 */
	public void setProvisioning(List<? extends Provisioning> provisioning) {
		this.provisioning = provisioning;
	}

    public Set<LoadBalancer> findLoadBalancersToNotifyOfChange() {
        Set<LoadBalancer> lbsToNotify = Sets.newHashSet();
        lbsToNotify.addAll( TaggedItemRegistry.getInstance().findReceivesNotificationsFor(this.getName(), LoadBalancer.class) );
        for (String tag : this.getTags()) {
            lbsToNotify.addAll( TaggedItemRegistry.getInstance().findReceivesNotificationsFor(tag, LoadBalancer.class) );
        }
        return lbsToNotify;
    }


}
