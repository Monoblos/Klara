package org.de.htwg.klara.transformers.variable;

import org.objectweb.asm.tree.InsnList;

/**
 * Defines the general structure of a variable and provides a generic way of interacting with them.
 * <br>
 * Known Implementations are {@link ClassVariable} and {@link LocalVariable}
 * @author mrs
 */
public interface AbstractVariable {
	/**
	 * The 4 variable types that have special handling:
	 * <ul>
	 * <li>Object: Any non-String reference object</li>
	 * <li>String: A String</li>
	 * <li>Char: The simple type char</li>
	 * <li>Other: Any simple type except char</li>
	 * </ul>
	 * @author mrs
	 *
	 */
	public static enum VarType {
		OBJECT,
		STRING,
		CHAR,
		OTHER
	}
	
	/**
	 * Returns the a list of instructions needed to load this Variable
	 * @return	The instructions needed.
	 */
	public InsnList load();
	/**
	 * Get the display name of this variable
	 * @return	The name of this variable
	 */
	public String getName();
	/**
	 * Returns whether or not this is a array.
	 * @return	True if this is at least a 1-Dimensional array, false otherwise
	 */
	public boolean isArray();
	/**
	 * Returns the type of this variable. Types are described in {@link VarType}.
	 * @return	The type of this variable.
	 */
	public VarType getType();
	/**
	 * Get the raw description of this variable as used in Bytecode.
	 * @return	The description of this variable
	 */
	public String getDescription();
}
