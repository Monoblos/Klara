package org.de.htwg.klara.transformers.events;

import org.de.htwg.klara.transformers.Transformer;

/**
 * Interface for transformation modules that need events from the {@link Transformer}.
 * @author mrs
 *
 */
public interface TransformationEventListener {
	/**
	 * Handles a transformation Event.
	 * @param event	The Event to handle
	 */
	public void handle(TransformationEvent event);
}
