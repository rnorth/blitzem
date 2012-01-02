package com.github.rnorth.blitzemj.model;

import java.util.List;

import com.github.rnorth.blitzemj.TaggedAndNamedItem;
import com.github.rnorth.blitzemj.TaggedItemRegistry;
import com.google.common.collect.Lists;

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
