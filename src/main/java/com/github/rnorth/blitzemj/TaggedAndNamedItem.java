package com.github.rnorth.blitzemj;

import java.util.List;

/**
 * Interface defining objects which may be stored in the
 * {@link TaggedItemRegistry}.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public interface TaggedAndNamedItem {

	/**
	 * @return the list of tags applicable to this item.
	 */
	List<String> getTags();

	/**
	 * @return the name of this item.
	 */
	String getName();
}
