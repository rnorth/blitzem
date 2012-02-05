package org.blitzem;

import java.util.List;
import java.util.Set;

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

    /**
     * @return any names or tags which this item is interested in receiving a notification for.
     */
    Set<String> getNotificationSubjects();

}
