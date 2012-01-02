/**
 * 
 */
package com.github.rnorth.blitzemj.model;

import java.util.Arrays;
import java.util.List;

import com.github.rnorth.blitzemj.TaggedAndNamedItem;
import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.google.common.collect.Lists;

/**
 * @author richardnorth
 *
 */
public class Node implements TaggedAndNamedItem {
	
	private String name = null;
	private List<String> tags = Lists.newArrayList();
	private Size size = Size.DEFAULT;
	private Os os = Os.DEFAULT;
	private List<? extends Provisioning> provisioning = Provisioning.DEFAULT;

	public Node() {
		TaggedItemRegistry.getInstance().add(this);
	}

	public Node(String name, String[] tags) {
		this();
		this.name = name;
		this.tags.addAll(Arrays.asList(tags));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [name=").append(name).append(", tags=").append(tags).append(", size=").append(size).append(", os=").append(os)
				.append("]");
		return builder.toString();
	}

	public String getName() {
		return name;
	}

	public List<String> getTags() {
		return tags;
	}

	public Size getSize() {
		return size;
	}

	public Os getOs() {
		return os;
	}

	public List<? extends Provisioning> getProvisioning() {
		return provisioning;
	}

	public void setProvisioning(List<? extends Provisioning> provisioning) {
		this.provisioning = provisioning;
	}

	
	
}
