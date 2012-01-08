package org.blitzem;

import org.blitzem.model.ExecutionContext;

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

    /**
     * Called to notify this item that another item has come up.
     * @param itemWhichIsUp
     * @param executionContext
     */
    void notifyIsUp(TaggedAndNamedItem itemWhichIsUp, ExecutionContext executionContext);

    /**
     * Called to notify this item that another item is about to go down.
     * @param itemWhichIsGoingDown
     * @param executionContext
     */
    void notifyIsGoingDown(TaggedAndNamedItem itemWhichIsGoingDown, ExecutionContext executionContext);

    /**
     *
     * @return whether the item is live in the cloud
     */
    boolean isUp(ExecutionContext executionContext);
}
