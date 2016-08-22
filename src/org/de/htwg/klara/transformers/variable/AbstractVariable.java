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
	
	public InsnList load();
	public String getName();
	public boolean isArray();
	public VarType getType();
	public String getDescription();
}
