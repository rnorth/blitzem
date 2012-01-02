package com.github.rnorth.blitzemj.model;

import java.util.List;

import com.github.rnorth.blitzemj.TaggedAndNamedItem;
import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.google.common.collect.Lists;

/**
 * Model class for a Load Balancer.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class LoadBalancer implements TaggedAndNamedItem {

	private String name;
	private List<String> tags = Lists.newArrayList();
	
	public LoadBalancer() {
		TaggedItemRegistry.getInstance().add(this);
	}

	public List<String> getTags() {
		return tags;
	}

	public String getName() {
		return name;
	}
}
