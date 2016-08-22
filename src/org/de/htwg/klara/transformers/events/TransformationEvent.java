package org.de.htwg.klara.transformers.events;

import org.de.htwg.klara.transformers.Transformer;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Event Interface for Events created by the {@link Transformer}
 * @author mrs
 *
 */
public interface TransformationEvent {
	/**
	 * Return value for {@link #getType()} of the class {@link VarInsnEvent}
	 */
	public static final int VAR_INSN = 1;
	/**
	 * Return value for {@link #getType()} of the class {@link IincInsnEvent}
	 */
	public static final int IINC_INSN = 2;
	/**
	 * Return value for {@link #getType()} of the class {@link ScopeReachedEvent}
	 */
	public static final int SCOPE_REACHED = 3;
	/**
	 * Return value for {@link #getType()} of a no longer existing class.
	 */
	public static final int SCOPE_ENDED = 4;
	/**
	 * Return value for {@link #getType()} of the class {@link LineStartEvent}
	 */
	public static final int LINE_START = 5;
	/**
	 * Return value for {@link #getType()} of the class {@link LineEndEvent}
	 */
	public static final int LINE_END = 6;
	/**
	 * Return value for {@link #getType()} of the class {@link PrintAddedEvent}
	 */
	public static final int PRINT_ADDED = 7;
	/**
	 * Return value for {@link #getType()} of the class {@link ClassVarChangedEvent}
	 */
	public static final int CLS_VAR_CHANGED = 8;
	
	/**
	 * Get the node to which this event is tied.
	 * @return	Any kind of Instruction-Node that can be seen as the cause for this event.
	 */
	public AbstractInsnNode getNode();
	/**
	 * Get the type of this event. Used for easy identification without using "typeof".
	 * @return	A number identifying the type of event. See constants of {@link TransformationEvent} for possible values.
	 */
	public int getType();
}
