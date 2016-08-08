package org.de.htwg.klara.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * A set of methods useful when creating new bytecode.
 * @author mrs
 *
 */
public final class TransformUtils {
	private TransformUtils() { };
	
	/**
	 * Create a location string from the given location.
	 * @param className	The name of the current class
	 * @param line	The current line in the class
	 * @return	A formated location string
	 */
	public static String formatLocation(String className, int line) {
		return "#" + className + "@L" + line + ": ";
	}
	
	/**
	 * Generate the Instruction List to print the given text with the current location
	 * @param className	The name of the current class
	 * @param line	The current line in the class
	 * @param text	The text to print
	 * @return	A set of Instruction to print this.
	 */
	public static InsnList generatePrintWithLocation(String className, int line, String text) {
		return generatePrint(formatLocation(className, line) + text);
	}

	/**
	 * Generate the Instruction List to print the given text.
	 * @param text	The text to print
	 * @return	A set of Instruction to print this.
	 */
	public static InsnList generatePrint(String text) {
		InsnList il = new InsnList();
		il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
		il.add(new LdcInsnNode(text));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
		return il;
	}
	
	/**
	 * Will create to correct VarInsnNode to load the given Variable.
	 * @param var	The Variable to load
	 * @return	The instruction that will load this value.
	 */
	public static VarInsnNode load(LocalVariableNode var) {
		switch (var.desc) {
		case "Z":
		case "C":
		case "B":
		case "S":
		case "I":
			return new VarInsnNode(Opcodes.ILOAD, var.index);
		case "F":
			return new VarInsnNode(Opcodes.FLOAD, var.index);
		case "J":
			return new VarInsnNode(Opcodes.LLOAD, var.index);
		case "D":
			return new VarInsnNode(Opcodes.DLOAD, var.index);

		default:
			return new VarInsnNode(Opcodes.ALOAD, var.index);
		}
	}
}
