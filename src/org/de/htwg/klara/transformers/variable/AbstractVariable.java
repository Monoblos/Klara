package org.de.htwg.klara.transformers.variable;

import org.objectweb.asm.tree.InsnList;

/**
 * Defines the general structure of a variable and provides a generic way of interacting with them.
 * <br>
 * Known Implementations are {@link ClassVariable} and {@link LocalVariable}
 * @author mrs
 */
public abstract class AbstractVariable {
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

	private VarType type = null;
	private VarType arrayType = null;
	
	/**
	 * Returns the a list of instructions needed to load this Variable
	 * @return	The instructions needed.
	 */
	public abstract InsnList load();
	/**
	 * Get the display name of this variable
	 * @return	The name of this variable
	 */
	public abstract String getName();
	/**
	 * Get the raw description of this variable as used in Bytecode.
	 * @return	The description of this variable
	 */
	public abstract String getDescription();

	/**
	 * Returns whether or not this is a array.
	 * @return	True if this is at least a 1-Dimensional array, false otherwise
	 */
	public boolean isArray() {
		return getDescription().startsWith("[");
	}

	/**
	 * Returns whether or not this is a primitive type.
	 * @return	True if this is a primitive type, false otherwise
	 */
	public boolean isPrimitive() {
		return getDescription().length() == 1;
	}

	/**
	 * Returns the type of this variable. Types are described in {@link VarType}.
	 * @return	The type of this variable.
	 */
	public VarType getType() {
		if (type == null)
			type = getTypeFromDesc(getDescription());
		return type;
	}

	/**
	 * Returns the type contained in this array. Types are described in {@link VarType}.
	 * @return	The type encapsulated in this array or {@code null} if not a array.
	 */
	public VarType getArrayType() {
		if (!isArray())
			return null;
		if (arrayType == null)
			arrayType = getTypeFromDesc(getDescription().substring(1));
		return arrayType;
	}
	
	/**
	 * Convert the description to the according VarType
	 * @param desc	Variable description to convert
	 * @return	The fitting {@link VarType} for this description
	 */
	public static VarType getTypeFromDesc(String desc) {
		if (desc.equals("Ljava/lang/String;")) {
			return VarType.STRING;
		} else if (desc.equals("C")) {
			return VarType.CHAR;
		} else if (desc.length() == 1) {
			return VarType.OTHER;
		} else {
			return VarType.OBJECT;
		}
	}
}
