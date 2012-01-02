/**
 * 
 */
package com.github.rnorth.blitzemj;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Acts as a registry for items which have names or tags.
 * 
 * @author richardnorth
 *
 */
public class TaggedItemRegistry {

	private static TaggedItemRegistry instance;
	private List<TaggedAndNamedItem> items = Lists.newArrayList();

	private TaggedItemRegistry() {}
	
	public static synchronized TaggedItemRegistry getInstance() {
		if (instance==null) {
			instance = new TaggedItemRegistry();
		}
		return instance;
	}
	
	public void add(TaggedAndNamedItem item) {
		this.items.add(item);
	}
	
	public <T> List<T> findMatching(String tagOrName, Class<T> clazz) {
		
		List<T> found = Lists.newArrayList();
		for (TaggedAndNamedItem item : items) {
			if (clazz.isAssignableFrom(item.getClass()) && (tagOrName==null || item.getName().equals(tagOrName) || item.getTags().contains(tagOrName))) {
				found.add((T) item);
			}
		}
		
		return found;
	}
}
