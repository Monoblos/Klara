package org.de.htwg.klara;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;

public class BytecodeUtils implements Opcodes {
	private BytecodeUtils() { }
	
	public static String[] typeListToJava(String s) {
		List<String> result = new LinkedList<>();
		int currentStartIndex = 0;
		int currentIndex = 0;

		while (currentStartIndex < s.length()) {
			while (s.charAt(currentIndex) == '[') {
				currentIndex++;
			}
			if (s.charAt(currentIndex) == 'L')
				currentIndex = s.indexOf(';', currentIndex);
			currentIndex++;
			result.add(typeToJava(s.substring(currentStartIndex, currentIndex)));
			currentStartIndex = currentIndex;
		}
		
		return result.toArray(new String[0]);
	}

	public static String typeToJava(String s) {
		return typeToJava(s, true);
	}
	
	/**
	 * Convert given type descriptor to "normal" look. Does not validate if the given descriptor is valid.
	 * @param s	The type descriptor.
	 * @return	The readable java-type.
	 */
	public static String typeToJava(String s, boolean fullQualifiedClasses) {
		int arrayDepth = 0;
		while (s.startsWith("[")) {
			arrayDepth++;
			s = s.replaceFirst("\\[", "");
		}
			
		switch (s) {
		case "Z":
			s = "boolean";
			break;
		case "C":
			s = "char";
			break;
		case "B":
			s = "byte";
			break;
		case "S":
			s = "short";
			break;
		case "I":
			s = "int";
			break;
		case "F":
			s = "float";
			break;
		case "J":
			s = "long";
			break;
		case "D":
			s = "double";
			break;
		case "V":
			s = "void";
			break;

		default:
			s = s.replaceFirst("L", "");
			s = s.replaceFirst(";", "");
			if (fullQualifiedClasses)
				s = s.replace("/", ".");
			else {
				String[] parts = s.split("/");
				s = parts[parts.length - 1];
			}
			break;
		}
		
		while(arrayDepth-- > 0) {
			s = s + "[]";
		}
		
		return s;
	}
	
	public static String accessBytesToString(int access) {
		StringBuilder sb = new StringBuilder("");
		if ((access & Opcodes.ACC_PUBLIC) > 0)
			sb.append("public ");
		else if ((access & Opcodes.ACC_PRIVATE) > 0)
			sb.append("private ");
		else if ((access & Opcodes.ACC_PROTECTED) > 0)
			sb.append("protected ");
		if ((access & Opcodes.ACC_STATIC) > 0)
			sb.append("static ");
		
		return sb.toString();
	}
	
	public static String opcodeToString(int opcode) {
		switch (opcode) {
		case INVOKEDYNAMIC:
			return "INVOKEDYNAMIC";
		case INVOKEINTERFACE:
			return "INVOKEINTERFACE";
		case INVOKESPECIAL:
			return "INVOKESPECIAL";
		case INVOKESTATIC:
			return "INVOKESTATIC";
		case INVOKEVIRTUAL:
			return "INVOKEVIRTUAL";
		case AALOAD:
			return "AALOAD";
		case AASTORE:
			return "AASTORE";
		case ACONST_NULL:
			return "ACONST_NULL";
		case ALOAD:
			return "ALOAD";
		case ANEWARRAY:
			return "ANEWARRAY";
		case ARETURN:
			return "ARETURN";
		case ARRAYLENGTH:
			return "ARRAYLENGTH";
		case ASTORE:
			return "ASTORE";
		case ATHROW:
			return "ATHROW";
		case BALOAD:
			return "BALOAD";
		case BASTORE:
			return "BASTORE";
		case BIPUSH:
			return "BIPUSH";
		case CALOAD:
			return "CALOAD";
		case CASTORE:
			return "CASTORE";
		case CHECKCAST:
			return "CHECKCAST";
		case D2F:
			return "D2F";
		case D2I:
			return "D2I";
		case D2L:
			return "D2L";
		case DADD:
			return "DADD";
		case DALOAD:
			return "DALOAD";
		case DASTORE:
			return "DASTORE";
		case DCMPG:
			return "DCMPG";
		case DCMPL:
			return "DCMPL";
		case DCONST_0:
			return "DCONST_0";
		case DCONST_1:
			return "DCONST_1";
		case DDIV:
			return "DDIV";
		case DLOAD:
			return "DLOAD";
		case DMUL:
			return "DMUL";
		case DNEG:
			return "DNEG";
		case DREM:
			return "DREM";
		case DRETURN:
			return "DRETURN";
		case DSTORE:
			return "DSTORE";
		case DSUB:
			return "DSUB";
		case DUP:
			return "DUP";
		case DUP2:
			return "DUP2";
		case DUP2_X1:
			return "DUP2_X1";
		case DUP2_X2:
			return "DUP2_X2";
		case DUP_X1:
			return "DUP_X1";
		case DUP_X2:
			return "DUP_X2";
		case F2D:
			return "F2D";
		case F2I:
			return "F2D";
		case F2L:
			return "F2D";
		case FADD:
			return "FADD";
		case FALOAD:
			return "FALOAD";
		case FASTORE:
			return "FASTORE";
		case FCMPG:
			return "FCMPG";
		case FCMPL:
			return "FCMPL";
		case FCONST_0:
			return "FCONST_0";
		case FCONST_1:
			return "FCONST_1";
		case FCONST_2:
			return "FCONST_2";
		case FDIV:
			return "FDIV";
		case FLOAD:
			return "FLOAD";
		case FMUL:
			return "FMUL";
		case FNEG:
			return "FNEG";
		case FREM:
			return "FREM";
		case FRETURN:
			return "FRETURN";
		case FSTORE:
			return "FSTORE";
		case FSUB:
			return "FSUB";
		case GETFIELD:
			return "GETFIELD";
		case GETSTATIC:
			return "GETSTATIC";
		case GOTO:
			return "GOTO";
		case I2B:
			return "I2B";
		case I2C:
			return "I2C";
		case I2D:
			return "I2D";
		case I2F:
			return "I2F";
		case I2L:
			return "I2L";
		case I2S:
			return "I2S";
		case IADD:
			return "IADD";
		case IALOAD:
			return "IALOAD";
		case IAND:
			return "IAND";
		case IASTORE:
			return "IASTORE";
		case ICONST_0:
			return "ICONST_0";
		case ICONST_1:
			return "ICONST_1";
		case ICONST_2:
			return "ICONST_2";
		case ICONST_3:
			return "ICONST_3";
		case ICONST_4:
			return "ICONST_4";
		case ICONST_5:
			return "ICONST_5";
		case ICONST_M1:
			return "ICONST_M1";
		case IDIV:
			return "IDIV";
		case IF_ACMPEQ:
			return "IF_ACMPEQ";
		case IF_ACMPNE:
			return "IF_ACMPNE";
		case IF_ICMPEQ:
			return "IF_ICMPEQ";
		case IF_ICMPGE:
			return "IF_ICMPGE";
		case IF_ICMPGT:
			return "IF_ICMPGT";
		case IF_ICMPLE:
			return "IF_ICMPLE";
		case IF_ICMPLT:
			return "IF_ICMPLT";
		case IF_ICMPNE:
			return "IF_ICMPNE";
		case IFEQ:
			return "IFEQ";
		case IFGE:
			return "IFGE";
		case IFGT:
			return "IFGT";
		case IFLE:
			return "IFLE";
		case IFLT:
			return "IFLT";
		case IFNE:
			return "IFNE";
		case IFNONNULL:
			return "IFNONNULL";
		case IFNULL:
			return "IFNULL";
		case IINC:
			return "IINC";
		case ILOAD:
			return "ILOAD";
		case IMUL:
			return "IMUL";
		case INEG:
			return "INEG";
		case INSTANCEOF:
			return "INSTANCEOF";
		case IOR:
			return "IOR";
		case IREM:
			return "IREM";
		case IRETURN:
			return "IRETURN";
		case ISHL:
			return "ISHL";
		case ISHR:
			return "ISHR";
		case ISTORE:
			return "ISTORE";
		case ISUB:
			return "ISUB";
		case IUSHR:
			return "IUSHR";
		case IXOR:
			return "IXOR";
		case JSR:
			return "JSR";
		case L2D:
			return "L2D";
		case L2F:
			return "L2F";
		case L2I:
			return "L2I";
		case LADD:
			return "LADD";
		case LALOAD:
			return "LALOAD";
		case LAND:
			return "LAND";
		case LASTORE:
			return "LASTORE";
		case LCMP:
			return "LCMP";
		case LCONST_0:
			return "LCONST_0";
		case LCONST_1:
			return "LCONST_1";
		case LDC:
			return "LDC";
		case LDIV:
			return "LDIV";
		case LLOAD:
			return "LLOAD";
		case LMUL:
			return "LMUL";
		case LNEG:
			return "LNEG";
		case LOOKUPSWITCH:
			return "LOOKUPSWITCH";
		case LOR:
			return "LOR";
		case LREM:
			return "LREM";
		case LRETURN:
			return "LRETURN";
		case LSHL:
			return "LSHL";
		case LSHR:
			return "LSHR";
		case LSTORE:
			return "LSTORE";
		case LSUB:
			return "LSUB";
		case LUSHR:
			return "LUSHR";
		case LXOR:
			return "LXOR";
		case MONITORENTER:
			return "MONITORENTER";
		case MONITOREXIT:
			return "MONITOREXIT";
		case MULTIANEWARRAY:
			return "MULTIANEWARRAY";
		case NEW:
			return "NEW";
		case NEWARRAY:
			return "NEWARRAY";
		case NOP:
			return "NOP";
		case POP:
			return "POP";
		case POP2:
			return "POP2";
		case PUTFIELD:
			return "PUTFIELD";
		case PUTSTATIC:
			return "PUTSTATIC";
		case RET:
			return "RET";
		case RETURN:
			return "RETURN";
		case SALOAD:
			return "SALOAD";
		case SASTORE:
			return "SASTORE";
		case SIPUSH:
			return "SIPUSH";
		case SWAP:
			return "SWAP";
		case TABLESWITCH:
			return "TABLESWITCH";

		default:
			return "ERROR("+opcode+")";
		}
	}
}
