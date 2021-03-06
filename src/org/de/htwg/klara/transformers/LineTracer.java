package org.de.htwg.klara.transformers;

import org.de.htwg.klara.transformers.events.TransformationEvent;
import org.de.htwg.klara.transformers.events.TransformationEventListener;
import org.objectweb.asm.tree.InsnList;

/**
 * The Line trace will ensure that there is at least one print added per line, to have a complete log of the line order executed.
 * @author mrs
 *
 */
public class LineTracer implements TransformationEventListener {
	private final Transformer trans;
	private boolean linePrinted = true;
	
	public LineTracer(Transformer trans) {
		this.trans = trans;
		trans.addListener(this);
	}

	@Override
	public void handle(TransformationEvent event) {
		if (event.getType() == TransformationEvent.LINE_START) {
			linePrinted = false;
		} else if (event.getType() == TransformationEvent.PRINT_ADDED) {
			linePrinted = true;
		} else if (event.getType() == TransformationEvent.LINE_END) {
			if (!linePrinted)
				trans.printLine(event.getNode(), new InsnList());
		}
	}

}
